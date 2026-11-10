package com.vilaverde.erp.service;

import com.vilaverde.erp.dto.recebimento.*;
import com.vilaverde.erp.entity.*;
import com.vilaverde.erp.entity.enums.*;
import com.vilaverde.erp.exception.*;
import com.vilaverde.erp.repository.*;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.OffsetDateTime;
import java.util.*;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class RecebimentoCompraService {
  private final com.vilaverde.erp.security.AccessScope scope;
  private final RecebimentoCompraRepository repository;
  private final ItemRecebimentoCompraRepository itemRepository;
  private final CompraRepository compraRepository;
  private final ItemCompraRepository itemCompraRepository;
  private final UsuarioRepository usuarioRepository;
  private final MovimentacaoEstoqueRepository movimentos;
  private final JdbcTemplate jdbc;

  @Transactional(readOnly=true)
  public List<RecebimentoCompraResponseDTO> listar(UUID idCompra) {
    return scope.rows(repository,"compra.loja.id").stream().filter(e -> idCompra==null || e.getCompra().getId().equals(idCompra)).map(e -> RecebimentoCompraResponseDTO.fromEntity(e,null)).toList();
  }

  @Transactional(readOnly=true)
  public RecebimentoCompraResponseDTO buscar(UUID id) { return response(find(id)); }

  @Transactional
  public RecebimentoCompraResponseDTO criar(UUID chave, RecebimentoCompraRequestDTO d) {
    scope.actor(d.idUsuario());
    scope.store(lockCompra(d.idCompra()).getLoja().getId());
    // Serialize even different purchases sharing a key; the persisted key survives restarts/deletion.
    jdbc.query("select pg_advisory_xact_lock(hashtextextended(?, 0))", rs -> {}, chave.toString());
    String hash = hash(d);
    var previous = jdbc.queryForList("select request_hash, receipt_id, deleted, actor_id from recebimento_idempotencia where chave = ?", chave);
    if (!previous.isEmpty()) {
      var row = previous.getFirst();
      if(!scope.principal().id().equals(row.get("actor_id")))throw new ResourceNotFoundException("Recurso não encontrado");
      if (!hash.equals(row.get("request_hash"))) throw new ConflictException("Chave reutilizada com outro conteúdo");
      if (Boolean.TRUE.equals(row.get("deleted"))) throw new ConflictException("O rascunho desta chave foi excluído");
      return response(find((UUID)row.get("receipt_id")));
    }
    if (d.versao() != null) throw new InvalidInputException("Criação não aceita versão");
    Compra compra = lockCompra(d.idCompra());
    eligible(compra);
    RecebimentoCompra e = new RecebimentoCompra();
    e.setCompra(compra);
    aplicar(e, d);
    e.setCriadoEm(OffsetDateTime.now());
    repository.saveAndFlush(e);
    replaceItems(e,d.itens());
    jdbc.update("insert into recebimento_idempotencia(chave, request_hash, receipt_id, actor_id) values (?,?,?,?)", chave, hash, e.getId(), scope.principal().id());
    return response(e);
  }

  @Transactional
  public RecebimentoCompraResponseDTO atualizar(UUID id, RecebimentoCompraRequestDTO d) {
    RecebimentoCompra e = lock(id);
    draft(e, d.versao());
    if (!e.getCompra().getId().equals(d.idCompra())) throw new InvalidInputException("A compra do recebimento não pode mudar");
    eligible(e.getCompra());
    Integer previousVersion = e.getVersao();
    aplicar(e,d);
    replaceItems(e,d.itens());
    repository.flush();
    // Item-only replacements must also advance the optimistic revision.
    if (previousVersion.equals(e.getVersao()))
      jdbc.update("update recebimento_compra set versao = versao + 1 where id = ?", id);
    return responseWithCurrentVersion(e);
  }

  @Transactional
  public void excluir(UUID id, Integer versao) {
    RecebimentoCompra e = lock(id);
    draft(e, versao);
    itemRepository.deleteAll(itemRepository.findByRecebimentoCompraId(id));
    itemRepository.flush();
    jdbc.update("update recebimento_idempotencia set deleted = true where receipt_id = ?", id);
    repository.delete(e);
    repository.flush();
  }

  @Transactional
  public RecebimentoCompraResponseDTO concluir(UUID id, Integer versao) {
    RecebimentoCompra e = lock(id);
    if (e.getStatus() == StatusDocumento.CONCLUIDO) return response(e);
    draft(e,versao);
    eligible(e.getCompra());
    List<ItemRecebimentoCompra> itens = itemRepository.findByRecebimentoCompraId(id);
    if (itens.isEmpty()) throw new ConflictException("Recebimento sem itens");
    // Validate the whole receipt before any effects. The purchase lock serializes its receipts.
    for (var i : itens) remaining(i.getItemCompra(),i.getQuantidade());
    // PostgreSQL ON CONFLICT increments the latest committed balance, also across purchases.
    // A global product ordering avoids deadlocks when receipts contain multiple products.
    var ordered = new ArrayList<>(itens);
    ordered.sort(Comparator.comparing(i -> i.getProdutoSku().getId().toString()));
    for (var i : ordered) {
      jdbc.update("""
        insert into estoque_produto(id,id_produto_sku,id_loja,quantidade_atual,atualizado_em)
        values (?,?,?,?,?) on conflict (id_produto_sku,id_loja) do update
        set quantidade_atual = estoque_produto.quantidade_atual + excluded.quantidade_atual,
            atualizado_em = excluded.atualizado_em
        """, UUID.randomUUID(),i.getProdutoSku().getId(),e.getCompra().getLoja().getId(),i.getQuantidade(),OffsetDateTime.now());
      var item = i.getItemCompra();
      item.setQuantidadeRecebida(item.getQuantidadeRecebida().add(i.getQuantidade()));
      MovimentacaoEstoque m = new MovimentacaoEstoque();
      m.setProdutoSku(i.getProdutoSku()); m.snapshotFrom(i); m.setLoja(e.getCompra().getLoja());
      m.setTipo(TipoMovimentacao.ENTRADA_COMPRA); m.setQuantidade(i.getQuantidade());
      m.setDataMovimentacao(e.getDataRecebimento()); m.setObservacao(i.getObservacao());
      m.setUsuario(scope.actor()); m.setCompra(e.getCompra()); m.setItemRecebimentoCompra(i);
      movimentos.save(m);
    }
    boolean complete = itemCompraRepository.findByCompraId(e.getCompra().getId()).stream()
      .allMatch(i -> i.getQuantidadeRecebida().compareTo(i.getQuantidade()) == 0);
    e.getCompra().setStatus(complete ? StatusCompra.RECEBIDA : StatusCompra.PARCIALMENTE_RECEBIDA);
    e.setConfirmador(scope.actor());
    e.setModificador(scope.actor());
    e.setStatus(StatusDocumento.CONCLUIDO); e.setConcluidoEm(OffsetDateTime.now());
    repository.flush();
    return response(e);
  }

  private RecebimentoCompra find(UUID id) {
    return repository.findById(id).filter(e -> scope.permits(e.getCompra().getLoja().getId())).orElseThrow(() -> new ResourceNotFoundException("Recebimento não encontrado"));
  }
  private Compra lockCompra(UUID id) {
    return compraRepository.lockById(id).filter(e -> scope.permits(e.getLoja().getId())).orElseThrow(() -> new ResourceNotFoundException("Compra não encontrada"));
  }
  private RecebimentoCompra lock(UUID id) {
    UUID compra = repository.findCompraIdById(id).orElseThrow(() -> new ResourceNotFoundException("Recebimento não encontrado"));
    lockCompra(compra);
    return repository.lockById(id).orElseThrow(() -> new ResourceNotFoundException("Recebimento não encontrado"));
  }
  private void eligible(Compra c) {
    if (c.getStatus() != StatusCompra.RASCUNHO && c.getStatus() != StatusCompra.PARCIALMENTE_RECEBIDA)
      throw new ConflictException("Compra cancelada ou totalmente recebida");
  }
  private void draft(RecebimentoCompra e, Integer versao) {
    if (versao == null || versao < 0) throw new InvalidInputException("Informe a versão do recebimento");
    if (e.getStatus() != StatusDocumento.RASCUNHO) throw new ConflictException("Recebimento confirmado é imutável");
    if (!versao.equals(e.getVersao())) throw new ConflictException("Versão desatualizada; recarregue o recebimento");
  }
  private void aplicar(RecebimentoCompra e, RecebimentoCompraRequestDTO d) {
    if (d.status() != StatusDocumento.RASCUNHO) throw new InvalidInputException("Use RASCUNHO; confirmação exige a operação concluir");
    var actor=scope.actor(d.idUsuario());
    if(e.getId()==null)e.setUsuario(actor);
    e.setModificador(actor);
    e.setDataRecebimento(d.dataRecebimento() == null ? OffsetDateTime.now() : d.dataRecebimento());
    e.setObservacao(d.observacao());
  }
  private void remaining(ItemCompra i, BigDecimal q) {
    if (q.compareTo(i.getQuantidade().subtract(i.getQuantidadeRecebida())) > 0)
      throw new ConflictException("Quantidade excede o pendente do item " + i.getId());
  }
  private void replaceItems(RecebimentoCompra e, List<ItemRecebimentoRequestDTO> itens) {
    var old = itemRepository.findByRecebimentoCompraId(e.getId());
    Map<UUID,ItemRecebimentoCompra> byId = new HashMap<>();
    Map<UUID,ItemRecebimentoCompra> byPurchaseItem = new HashMap<>();
    old.forEach(i -> { byId.put(i.getId(),i); byPurchaseItem.put(i.getItemCompra().getId(),i); });
    Set<UUID> seen = new HashSet<>(), retained = new HashSet<>();
    for (var d : itens) {
      if (!seen.add(d.idItemCompra())) throw new InvalidInputException("Item de compra duplicado");
      var purchaseItem = itemCompraRepository.findById(d.idItemCompra()).orElseThrow(() -> new ResourceNotFoundException("Item de compra não encontrado"));
      if (!purchaseItem.getCompra().getId().equals(e.getCompra().getId()) || !purchaseItem.getProdutoSku().getId().equals(d.idProdutoSku()))
        throw new InvalidInputException("Item/produto não pertence à compra");
      remaining(purchaseItem,d.quantidade());
      ItemRecebimentoCompra i = d.id() == null ? byPurchaseItem.get(d.idItemCompra()) : byId.get(d.id());
      if (d.id() != null && (i == null || !i.getItemCompra().getId().equals(d.idItemCompra())))
        throw new InvalidInputException("ID de item não pertence a este recebimento/item de compra");
      if (i == null) i = new ItemRecebimentoCompra();
      i.setRecebimentoCompra(e); i.setItemCompra(purchaseItem); i.setProdutoSku(purchaseItem.getProdutoSku()); i.snapshotFrom(purchaseItem);
      i.setQuantidade(d.quantidade()); i.setObservacao(d.observacao());
      itemRepository.save(i); retained.add(i.getId());
    }
    itemRepository.deleteAll(old.stream().filter(i -> !retained.contains(i.getId())).toList());
    itemRepository.flush();
  }
  private RecebimentoCompraResponseDTO response(RecebimentoCompra e) {
    return RecebimentoCompraResponseDTO.fromEntity(e,itemRepository.findByRecebimentoCompraId(e.getId()).stream()
      .sorted(Comparator.comparing(i -> i.getItemCompra().getId().toString())).map(ItemRecebimentoResponseDTO::fromEntity).toList());
  }
  private RecebimentoCompraResponseDTO responseWithCurrentVersion(RecebimentoCompra e) {
    var r = response(e);
    Integer version = jdbc.queryForObject("select versao from recebimento_compra where id = ?",Integer.class,e.getId());
    return new RecebimentoCompraResponseDTO(r.id(),r.idCompra(),r.idUsuario(),r.nomeUsuario(),r.idModificador(),r.nomeModificador(),r.idConfirmador(),r.nomeConfirmador(),r.status(),r.dataRecebimento(),r.observacao(),version,r.concluidoEm(),r.itens());
  }
  private String hash(RecebimentoCompraRequestDTO d) {
    // Length framing, normalized quantities/instants and stable item order form a canonical payload.
    StringBuilder s = new StringBuilder();
    append(s,d.idCompra()); append(s,d.idUsuario()); append(s,d.versao()); append(s,d.status());
    append(s,d.dataRecebimento() == null ? null : d.dataRecebimento().toInstant()); append(s,d.observacao());
    d.itens().stream().sorted(Comparator.comparing(i -> i.idItemCompra().toString())).forEach(i -> {
      append(s,i.id()); append(s,i.idItemCompra()); append(s,i.idProdutoSku());
      append(s,i.quantidade().stripTrailingZeros().toPlainString()); append(s,i.observacao());
    });
    try { return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(s.toString().getBytes(StandardCharsets.UTF_8))); }
    catch (java.security.NoSuchAlgorithmException ex) { throw new IllegalStateException(ex); }
  }
  private void append(StringBuilder s, Object value) {
    if (value == null) s.append("-1:"); else { String v = value.toString(); s.append(v.length()).append(':').append(v); }
  }
}
