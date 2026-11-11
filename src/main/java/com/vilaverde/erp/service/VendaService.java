package com.vilaverde.erp.service;

import com.vilaverde.erp.dto.venda.ProdutoVendaRequestDTO;
import com.vilaverde.erp.dto.venda.VendaRequestDTO;
import com.vilaverde.erp.dto.venda.VendaResponseDTO;
import com.vilaverde.erp.entity.Cliente;
import com.vilaverde.erp.entity.Loja;
import com.vilaverde.erp.entity.ProdutoSku;
import com.vilaverde.erp.entity.ProdutoVenda;
import com.vilaverde.erp.entity.Usuario;
import com.vilaverde.erp.entity.Venda;
import com.vilaverde.erp.entity.enums.StatusVenda;
import com.vilaverde.erp.exception.ResourceNotFoundException;
import com.vilaverde.erp.repository.ClienteRepository;
import com.vilaverde.erp.repository.LojaRepository;
import com.vilaverde.erp.repository.ProdutoSkuRepository;
import com.vilaverde.erp.repository.UsuarioRepository;
import com.vilaverde.erp.repository.VendaRepository;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import com.vilaverde.erp.exception.*;
import com.vilaverde.erp.entity.MovimentacaoEstoque;
import com.vilaverde.erp.entity.enums.TipoMovimentacao;
import org.springframework.jdbc.core.JdbcTemplate;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class VendaService {
  private final com.vilaverde.erp.security.AccessScope scope;

  private final JdbcTemplate jdbc;
  private final com.vilaverde.erp.repository.MovimentacaoEstoqueRepository movimentos;
  private final VendaRepository vendaRepository;
  private final ClienteRepository clienteRepository;
  private final UsuarioRepository usuarioRepository;
  private final LojaRepository lojaRepository;
  private final ProdutoSkuRepository produtoRepository;

  @Transactional(readOnly = true)
  public List<VendaResponseDTO> listar(
    UUID idCliente,
    UUID idUsuario,
    UUID idLoja
  ) {
    return scope.rows(vendaRepository,"loja.id").stream().filter(e -> (idCliente==null || (e.getCliente()!=null && e.getCliente().getId().equals(idCliente))) && (idUsuario==null || (e.getUsuario()!=null && e.getUsuario().getId().equals(idUsuario))) && (idLoja==null || e.getLoja().getId().equals(idLoja))).map(VendaResponseDTO::fromEntity).toList();
  }

  @Transactional(readOnly = true)
  public VendaResponseDTO buscarPorId(UUID id) {
    return VendaResponseDTO.fromEntity(buscar(id));
  }

  @Transactional
  public VendaResponseDTO criar(UUID chave, VendaRequestDTO dto) {
    scope.actor(dto.idUsuario()); scope.store(dto.idLoja());
    if(chave==null) throw new InvalidInputException("Informe Idempotency-Key UUID");
    validate(dto);
    jdbc.query("select pg_advisory_xact_lock(hashtextextended(?, 0))", rs -> {}, "sale:"+chave);
    String hash=hash(dto);
    var previous=jdbc.queryForList("select request_hash,venda_id,actor_id from venda_idempotencia where chave=?",chave);
    if(!previous.isEmpty()) {
      var row=previous.getFirst();
      if(!scope.principal().id().equals(row.get("actor_id"))) throw new ResourceNotFoundException("Venda não encontrada");
      if(!hash.equals(row.get("request_hash"))) throw new ConflictException("Chave reutilizada com outro conteúdo");
      return VendaResponseDTO.fromEntity(buscar((UUID)row.get("venda_id")));
    }
    if(dto.versao()!=null) throw new InvalidInputException("Criação não aceita versão");
    Venda venda=new Venda();
    aplicarCabecalho(venda,dto); aplicarItens(venda,dto.itens()); recalcularTotais(venda);
    vendaRepository.saveAndFlush(venda);
    jdbc.update("insert into venda_idempotencia(chave,request_hash,venda_id,actor_id) values (?,?,?,?)",chave,hash,venda.getId(),scope.principal().id());
    return VendaResponseDTO.fromEntity(venda);
  }

  @Transactional
  public VendaResponseDTO atualizar(UUID id,VendaRequestDTO dto) {
    Venda venda=lock(id); open(venda,dto.versao()); validate(dto);
    aplicarCabecalho(venda,dto); aplicarItens(venda,dto.itens()); recalcularTotais(venda);
    // Explicit revision advances even for edits limited to child rows.
    venda.setAtualizadoEm(java.time.OffsetDateTime.now());
    vendaRepository.flush();
    return VendaResponseDTO.fromEntity(venda);
  }

  @Transactional
  public VendaResponseDTO concluir(UUID id,Integer versao) {
    Venda venda=lock(id);
    if(venda.isLegado()) throw new ConflictException("Venda histórica somente para consulta; reconciliação necessária");
    if(venda.getStatus()==StatusVenda.CONCLUIDA) return VendaResponseDTO.fromEntity(venda);
    open(venda,versao);
    if(venda.getCliente()==null || venda.getItens().isEmpty()) throw new ConflictException("Revise cliente e itens antes de concluir");
    Map<UUID,BigDecimal> required=new TreeMap<>(Comparator.comparing(UUID::toString));
    venda.getItens().forEach(i -> required.merge(i.getProdutoSku().getId(),i.getQuantidade(),BigDecimal::add));
    List<String> shortages=new ArrayList<>();
    for(var entry:required.entrySet()) {
      var balances=jdbc.queryForList("select quantidade_atual from estoque_produto where id_produto_sku=? and id_loja=? for update",BigDecimal.class,entry.getKey(),venda.getLoja().getId());
      BigDecimal available=balances.isEmpty() ? BigDecimal.ZERO : balances.getFirst();
      if(available.compareTo(entry.getValue())<0) {
        String name=venda.getItens().stream().filter(i->i.getProdutoSku().getId().equals(entry.getKey())).findFirst().orElseThrow().getProdutoSku().getDisplayName();
        shortages.add(name+" ("+entry.getKey()+"): disponível "+available+", necessário "+entry.getValue());
      }
    }
    if(!shortages.isEmpty()) throw new ConflictException("Estoque insuficiente: "+String.join("; ",shortages));
    var now=java.time.OffsetDateTime.now().withOffsetSameInstant(java.time.ZoneOffset.UTC).truncatedTo(java.time.temporal.ChronoUnit.MICROS);
    for(var entry:required.entrySet()) {
      int count=jdbc.update("update estoque_produto set quantidade_atual=quantidade_atual-?,atualizado_em=? where id_produto_sku=? and id_loja=? and quantidade_atual>=?",entry.getValue(),now,entry.getKey(),venda.getLoja().getId(),entry.getValue());
      if(count!=1) throw new ConflictException("Saldo alterado; revise o estoque");
    }
    for(var item:venda.getItens()) {
      MovimentacaoEstoque m=new MovimentacaoEstoque();
      m.setVenda(venda); m.setItemVenda(item); m.setLoja(venda.getLoja()); m.setProdutoSku(item.getProdutoSku()); m.snapshotFrom(item);
      m.setTipo(TipoMovimentacao.SAIDA_VENDA); m.getProdutoSku().requireActive();
      m.setQuantidade(item.getQuantidade());
      m.setUsuario(scope.actor()); m.setDataMovimentacao(now); movimentos.save(m);
    }
    venda.setConfirmador(scope.actor()); venda.setModificador(scope.actor());
    venda.setConcluidoEm(now); venda.setStatus(StatusVenda.CONCLUIDA);
    vendaRepository.flush(); return VendaResponseDTO.fromEntity(venda);
  }

  @Transactional
  public VendaResponseDTO cancelar(UUID id,Integer versao,String motivo) {
    Venda venda=lock(id);
    if(venda.isLegado()) throw new ConflictException("Venda histórica somente para consulta");
    if(motivo==null || motivo.isBlank()) throw new InvalidInputException("Informe o motivo do cancelamento");
    if(venda.getStatus()==StatusVenda.CANCELADA) return VendaResponseDTO.fromEntity(venda);
    open(venda,versao);
    venda.setStatus(StatusVenda.CANCELADA); venda.setCanceladoEm(java.time.OffsetDateTime.now().withOffsetSameInstant(java.time.ZoneOffset.UTC).truncatedTo(java.time.temporal.ChronoUnit.MICROS));
    venda.setCancelador(scope.actor()); venda.setModificador(scope.actor()); venda.setMotivoCancelamento(motivo.trim());
    vendaRepository.flush(); return VendaResponseDTO.fromEntity(venda);
  }

  public void excluir(UUID id) { throw new org.springframework.security.access.AccessDeniedException("Use cancelamento para preservar o histórico"); }
  private Venda lock(UUID id) {
    return vendaRepository.lockById(id).filter(e->scope.permits(e.getLoja().getId())).orElseThrow(()->new ResourceNotFoundException("Venda não encontrada"));
  }
  private void open(Venda venda,Integer versao) {
    if(versao==null || versao<0) throw new InvalidInputException("Informe a versão da venda");
    if(venda.getStatus()!=StatusVenda.ABERTA || venda.isLegado()) throw new ConflictException("Apenas vendas abertas podem ser alteradas");
    if(!versao.equals(venda.getVersao())) throw new ConflictException("Versão desatualizada; recarregue e revise a venda");
  }
  private void number(BigDecimal n,int precision,int scale,boolean positive) {
    if(n==null || n.signum()<0 || (positive && n.signum()==0) || n.scale()>scale || n.compareTo(BigDecimal.TEN.pow(precision-scale))>=0)
      throw new InvalidInputException("Valor, precisão ou limite numérico inválido");
  }
  private void validate(VendaRequestDTO dto) {
    if(dto.status()!=null) throw new InvalidInputException("Use as ações concluir/cancelar para alterar status");
    if(dto.idCliente()==null || dto.idLoja()==null || dto.itens()==null || dto.itens().isEmpty()) throw new InvalidInputException("Cliente, loja e itens obrigatórios");
    if(dto.observacao()!=null && dto.observacao().length()>255) throw new InvalidInputException("Observação deve ter até 255 caracteres");
    for(var i:dto.itens()) {
      if(i==null) throw new InvalidInputException("Item obrigatório");
      if(i.idProdutoSku()==null) throw new InvalidInputException("ProdutoSku obrigatório");
      number(i.quantidade(),14,3,true); number(i.precoUnitario(),12,2,false);
      BigDecimal discount=i.desconto()==null ? BigDecimal.ZERO : i.desconto(); number(discount,14,2,false);
      if(discount.compareTo(i.quantidade().multiply(i.precoUnitario()))>0) throw new InvalidInputException("Desconto supera o valor bruto");
      number(i.quantidade().multiply(i.precoUnitario()).subtract(discount).setScale(2,RoundingMode.HALF_UP),14,2,false);
    }
  }
  private String hash(VendaRequestDTO d) {
    StringBuilder s=new StringBuilder();
    append(s,d.idCliente()); append(s,d.idUsuario()); append(s,d.idLoja()); append(s,d.versao()); append(s,d.status());
    append(s,d.dataVenda()==null ? null : d.dataVenda().toInstant()); append(s,d.observacao());
    // Row order is meaningful and duplicate products remain distinct sale items.
    for(var i:d.itens()) { append(s,i.id()); append(s,i.idProdutoSku()); append(s,i.quantidade().stripTrailingZeros().toPlainString()); append(s,i.precoUnitario().stripTrailingZeros().toPlainString()); append(s,(i.desconto()==null ? BigDecimal.ZERO : i.desconto()).stripTrailingZeros().toPlainString()); }
    try { return HexFormat.of().formatHex(java.security.MessageDigest.getInstance("SHA-256").digest(s.toString().getBytes(java.nio.charset.StandardCharsets.UTF_8))); }
    catch(java.security.NoSuchAlgorithmException e) {throw new IllegalStateException(e);}
  }
  private void append(StringBuilder s,Object value) {if(value==null)s.append("-1:");else {String v=value.toString();s.append(v.length()).append(':').append(v);}}

  private void aplicarCabecalho(Venda venda, VendaRequestDTO dto) {
    scope.store(dto.idLoja());
    var actor=scope.actor(dto.idUsuario());
    if(venda.getId()==null)venda.setUsuario(actor);
    venda.setModificador(actor);
    venda.setDataVenda(dto.dataVenda());
    venda.setCliente(
      dto.idCliente() != null ? buscarCliente(dto.idCliente()) : null
    );
    venda.setLoja(buscarLoja(dto.idLoja()));
    venda.setObservacao(dto.observacao());
    if (venda.getDataVenda() == null) venda.setDataVenda(
      java.time.OffsetDateTime.now()
    );
    if (venda.getStatus() == null) venda.setStatus(StatusVenda.ABERTA);
  }

  private void aplicarItens(Venda venda, List<ProdutoVendaRequestDTO> itens) {
    Map<UUID,ProdutoVenda> old=new HashMap<>(); venda.getItens().forEach(i->old.put(i.getId(),i));
    Set<UUID> retained=new HashSet<>();
    List<ProdutoVenda> additions=new ArrayList<>();
    for (ProdutoVendaRequestDTO itemDto : itens) {
      ProdutoVenda item=itemDto.id()==null ? new ProdutoVenda() : old.get(itemDto.id());
      if(item==null || (itemDto.id()!=null && !retained.add(itemDto.id()))) throw new InvalidInputException("ID de item não pertence à venda ou está duplicado");
      boolean sameProduct=item.getProdutoSku()!=null && item.getProdutoSku().getId().equals(itemDto.idProdutoSku());
      item.setVenda(venda);
      item.setProdutoSku(buscarProduto(itemDto.idProdutoSku()));
      item.getProdutoSku().requireActive();
      item.setQuantidade(itemDto.quantidade());
      item.setPrecoUnitario(itemDto.precoUnitario());
      item.setDesconto(
        itemDto.desconto() != null ? itemDto.desconto() : BigDecimal.ZERO
      );
      item.setTotalItem(
        item
          .getQuantidade()
          .multiply(item.getPrecoUnitario())
          .subtract(item.getDesconto())
          .setScale(2, RoundingMode.HALF_UP)
      );
      if(!sameProduct) item.setUnidadeSiglaSnapshot(item.getProdutoSku().getUnidadeMedida().getSigla());
      if(itemDto.id()==null) additions.add(item);
    }
    venda.getItens().removeIf(i->!retained.contains(i.getId()));
    venda.getItens().addAll(additions);
  }

  private void recalcularTotais(Venda venda) {
    BigDecimal subtotal = venda
      .getItens()
      .stream()
      .map(item -> item.getQuantidade().multiply(item.getPrecoUnitario()))
      .reduce(BigDecimal.ZERO, BigDecimal::add)
      .setScale(2, RoundingMode.HALF_UP);
    BigDecimal desconto = venda
      .getItens()
      .stream()
      .map(item ->
        item.getDesconto() != null ? item.getDesconto() : BigDecimal.ZERO
      )
      .reduce(BigDecimal.ZERO, BigDecimal::add)
      .setScale(2, RoundingMode.HALF_UP);
    number(subtotal,14,2,false); number(desconto,14,2,false); number(subtotal.subtract(desconto),14,2,false);
    venda.setSubtotal(subtotal);
    venda.setDescontoTotal(desconto);
    venda.setValorTotal(
      subtotal.subtract(desconto).setScale(2, RoundingMode.HALF_UP)
    );
  }

  private Venda buscar(UUID id) {
    return vendaRepository
      .findById(id)
      .filter(e -> scope.permits(e.getLoja().getId()))
      .orElseThrow(() -> new ResourceNotFoundException("Venda não encontrada"));
  }

  private Cliente buscarCliente(UUID id) {
    return clienteRepository
      .findById(id)
      .orElseThrow(() ->
        new ResourceNotFoundException("Cliente não encontrado")
      );
  }

  private Usuario buscarUsuario(UUID id) {
    return usuarioRepository
      .findById(id)
      .orElseThrow(() ->
        new ResourceNotFoundException("Usuário não encontrado")
      );
  }

  private Loja buscarLoja(UUID id) {
    return lojaRepository
      .findById(id)
      .orElseThrow(() -> new ResourceNotFoundException("Loja não encontrada"));
  }

  private ProdutoSku buscarProduto(UUID id) {
    return produtoRepository
      .findById(id)
      .orElseThrow(() ->
        new ResourceNotFoundException("SKU não encontrado")
      );
  }
}
