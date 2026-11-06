package com.vilaverde.erp.service;
import com.vilaverde.erp.dto.devolucao.*;
import com.vilaverde.erp.entity.*;
import com.vilaverde.erp.entity.enums.*;
import com.vilaverde.erp.exception.*;
import com.vilaverde.erp.repository.*;
import com.vilaverde.erp.security.AccessScope;
import java.util.*;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service @RequiredArgsConstructor
public class DevolucaoClienteService {
 private final DevolucaoClienteRepository repository;
 private final VendaRepository sources;
 private final ProdutoVendaRepository sourceItems;
 private final MovimentacaoEstoqueRepository movimentos;
 private final AccessScope scope;
 private final JdbcTemplate jdbc;
 private static final BigDecimal LIMIT=new BigDecimal("100000000000");
 private void permission() {if(scope.principal().cargo()==CargoUsuario.FUNC)throw new org.springframework.security.access.AccessDeniedException("Devoluções exigem ADMIN/GER");}
 private Venda source(UUID id,boolean lock) {
  permission();if(id==null)throw new InvalidInputException("Informe o documento de origem");
  var e=(lock?sources.lockById(id):sources.findById(id)).filter(v->scope.permits(v.getLoja().getId())).orElseThrow(()->new ResourceNotFoundException("Origem não encontrada"));
  return e;
 }
 private void eligible(Venda e) {
  if(e.isLegado() || e.getStatus()!=StatusVenda.CONCLUIDA)throw new ConflictException("Exige venda concluída não legada");
  if(Boolean.TRUE.equals(jdbc.queryForObject("select exists(select 1 from devolucao_cliente where id_venda=? and legado)",Boolean.class,e.getId())))throw new ConflictException("Origem possui devolução legada; reconcilie a quantidade devolvível antes de continuar");
 }
 private DevolucaoCliente find(UUID id,boolean lock) {
  permission();
  // Read only the fixed source identity before locking; never load a stale document first.
  if(lock) {
   var ids=jdbc.queryForList("select id_venda from devolucao_cliente where id=?",UUID.class,id);
   if(ids.isEmpty())throw new ResourceNotFoundException("Devolução não encontrada");
   source(ids.getFirst(),true);
  }
  return (lock?repository.lockById(id):repository.findById(id)).filter(e->scope.permits(e.getVenda().getLoja().getId())).orElseThrow(()->new ResourceNotFoundException("Devolução não encontrada"));
 }
 @Transactional(readOnly=true) public List<DevolucaoClienteResponseDTO> listar(UUID sourceId) {
  permission();return scope.rows(repository,"venda.loja.id").stream().filter(e->sourceId==null || e.getVenda().getId().equals(sourceId)).map(DevolucaoClienteResponseDTO::fromEntity).toList();
 }
 @Transactional(readOnly=true) public DevolucaoClienteResponseDTO buscar(UUID id) {return DevolucaoClienteResponseDTO.fromEntity(find(id,false));}
 @Transactional public DevolucaoClienteResponseDTO criar(UUID key,DevolucaoClienteRequestDTO d) {
  permission();validate(d);scope.actor(d.idUsuario());
  if(key==null)throw new InvalidInputException("Informe Idempotency-Key UUID");
  jdbc.query("select pg_advisory_xact_lock(hashtextextended(?,0))",rs->{},"devolucao_cliente:"+key);
  var hash=hash(d);var previous=jdbc.queryForList("select * from devolucao_cliente_idempotencia where chave=?",key);
  if(!previous.isEmpty()) {
   var row=previous.getFirst();
   if(!scope.principal().id().equals(row.get("actor_id")))throw new ResourceNotFoundException("Devolução não encontrada");
   var e=find((UUID)row.get("devolucao_id"),false);
   if(!hash.equals(row.get("request_hash")))throw new ConflictException("Chave reutilizada com outro conteúdo");
   return DevolucaoClienteResponseDTO.fromEntity(e);
  }
  if(d.versao()!=null || d.itens().stream().anyMatch(i->i.id()!=null))throw new InvalidInputException("Criação não aceita versão ou IDs de item");
  var original=source(d.idVenda(),true);eligible(original);
  var e=new DevolucaoCliente();e.setVenda(original);e.setUsuario(scope.actor());apply(e,d);repository.saveAndFlush(e);
  jdbc.update("insert into devolucao_cliente_idempotencia(chave,request_hash,devolucao_id,actor_id) values (?,?,?,?)",key,hash,e.getId(),scope.principal().id());
  return DevolucaoClienteResponseDTO.fromEntity(e);
 }
 @Transactional public DevolucaoClienteResponseDTO atualizar(UUID id,DevolucaoClienteRequestDTO d) {
  var e=find(id,true);open(e,d.versao());validate(d);
  if(!e.getVenda().getId().equals(d.idVenda()))throw new InvalidInputException("A origem não pode mudar");
  eligible(e.getVenda());apply(e,d);repository.flush();return DevolucaoClienteResponseDTO.fromEntity(e);
 }
 private void operational(DevolucaoCliente e) {if(e.isLegado())throw new ConflictException("Devolução legada somente para consulta");}
 private void open(DevolucaoCliente e,Integer version) {
  operational(e);if(version==null || version<0)throw new InvalidInputException("Informe a versão");
  if(e.getStatus()!=StatusDocumento.RASCUNHO)throw new ConflictException("Somente rascunhos podem ser alterados");
  if(!version.equals(e.getVersao()))throw new ConflictException("Versão desatualizada; recarregue e revise");
 }
 @Transactional public DevolucaoClienteResponseDTO concluir(UUID id,Integer version) {
  var e=find(id,true);operational(e);
  if(e.getStatus()==StatusDocumento.CONCLUIDA)return DevolucaoClienteResponseDTO.fromEntity(e);
  open(e,version);eligible(e.getVenda());
  if(e.getItens().isEmpty())throw new ConflictException("Devolução sem itens");
  Map<UUID,BigDecimal> bySource=new HashMap<>();Map<UUID,BigDecimal> deltas=new TreeMap<>(Comparator.comparing(UUID::toString));
  for(var i:e.getItens()) {
   bySource.merge(i.getItemVenda().getId(),i.getQuantidade(),BigDecimal::add);
   if(Boolean.TRUE.equals(i.getRetornaAoEstoque())) deltas.merge(i.getProduto().getId(),i.getQuantidade(),BigDecimal::add);
  }
  for(var entry:bySource.entrySet()) {
   var received=jdbc.queryForObject("select quantidade from produto_venda where id=?",BigDecimal.class,entry.getKey());
   var returned=jdbc.queryForObject("select coalesce(sum(i.quantidade),0) from item_devolucao_cliente i join devolucao_cliente d on d.id=i.id_devolucao_cliente where i.id_item_venda=? and d.status='CONCLUIDA' and not d.legado",BigDecimal.class,entry.getKey());
   if(entry.getValue().compareTo(received.subtract(returned))>0)throw new ConflictException("Quantidade excede o restante devolvível do item "+entry.getKey());
  }
  var store=e.getVenda().getLoja().getId();
  // Identical product/store ordering and row locks to receiving, sales, transfers, adjustments.
  for(var entry:deltas.entrySet()) {
   jdbc.update("insert into estoque_produto(id,id_produto,id_loja,quantidade_atual,atualizado_em) values (?,?,?,0,now()) on conflict(id_produto,id_loja) do nothing",UUID.randomUUID(),entry.getKey(),store);
   var balance=jdbc.queryForObject("select quantidade_atual from estoque_produto where id_produto=? and id_loja=? for update",BigDecimal.class,entry.getKey(),store);
   var after=balance.add(entry.getValue());if(after.signum()<0 || after.compareTo(LIMIT)>=0)throw new ConflictException("Saldo insuficiente ou overflow para produto "+entry.getKey()+"; disponível: "+balance);
  }
  var now=now();var actor=scope.actor();
  for(var entry:deltas.entrySet())jdbc.update("update estoque_produto set quantidade_atual=quantidade_atual+?,atualizado_em=? where id_produto=? and id_loja=?",entry.getValue(),now,entry.getKey(),store);
  for(var i:e.getItens())if(Boolean.TRUE.equals(i.getRetornaAoEstoque())) {
   var m=new MovimentacaoEstoque();m.setDevolucaoCliente(e);m.setItemDevolucaoCliente(i);m.setVenda(e.getVenda());m.setItemVenda(i.getItemVenda());
   m.setProduto(i.getProduto());m.setLoja(e.getVenda().getLoja());m.setTipo(TipoMovimentacao.DEVOLUCAO);m.setQuantidade(i.getQuantidade());m.setObservacao(i.getObservacao());m.setUsuario(actor);m.setDataMovimentacao(now);movimentos.save(m);
  }
  e.setStatus(StatusDocumento.CONCLUIDA);e.setDataDevolucao(now);e.setConcluidoEm(now);e.setAtualizadoEm(now);e.setConfirmador(actor);e.setModificador(actor);
  repository.flush();return DevolucaoClienteResponseDTO.fromEntity(e);
 }
 @Transactional public DevolucaoClienteResponseDTO cancelar(UUID id,Integer version,String reason) {
  var e=find(id,true);open(e,version);
  if(reason==null || reason.isBlank())throw new InvalidInputException("Informe o motivo do cancelamento");
  e.setStatus(StatusDocumento.CANCELADA);e.setCancelador(scope.actor());e.setModificador(scope.actor());e.setCanceladoEm(now());e.setAtualizadoEm(e.getCanceladoEm());e.setMotivoCancelamento(reason.trim());repository.flush();return DevolucaoClienteResponseDTO.fromEntity(e);
 }
 private void validate(DevolucaoClienteRequestDTO d) {
  if(d.status()!=null || d.dataDevolucao()!=null)throw new InvalidInputException("Datas e status de confirmação são definidos pelo servidor");
  if(d.idVenda()==null || d.motivo()==null || d.motivo().isBlank() || d.motivo().length()>255)throw new InvalidInputException("Origem e motivo de até 255 caracteres obrigatórios");
  if(d.observacao()!=null && d.observacao().length()>255)throw new InvalidInputException("Observação deve ter até 255 caracteres");
  if(d.itens()==null || d.itens().isEmpty())throw new InvalidInputException("Informe itens");
  var unique=new HashSet<String>();
  for(var i:d.itens()) {
   if(i==null || i.idItemVenda()==null || i.idProduto()==null)throw new InvalidInputException("Item e produto de origem obrigatórios");
   if(i.retornaAoEstoque()==null || (!i.retornaAoEstoque() && (i.observacao()==null || i.observacao().isBlank())))throw new InvalidInputException("Informe disposição e observação para item sem retorno ao estoque");
   if(!unique.add(i.idItemVenda().toString()+i.retornaAoEstoque()))throw new InvalidInputException("Item de origem duplicado");
   if(i.observacao()!=null && i.observacao().length()>255)throw new InvalidInputException("Observação deve ter até 255 caracteres");
   var q=i.quantidade();if(q==null || q.signum()<=0 || q.scale()>3 || q.compareTo(LIMIT)>=0)throw new InvalidInputException("Quantidade positiva numeric(14,3) obrigatória");
  }
 }
 private void apply(DevolucaoCliente e,DevolucaoClienteRequestDTO d) {
  e.setMotivo(d.motivo().trim());e.setObservacao(d.observacao());e.setModificador(scope.actor(d.idUsuario()));e.setAtualizadoEm(now());
  Map<UUID,ItemDevolucaoCliente> old=new HashMap<>();e.getItens().forEach(i->old.put(i.getId(),i));var retained=new HashSet<UUID>();var added=new ArrayList<ItemDevolucaoCliente>();
  for(var r:d.itens()) {
   var original=sourceItems.findById(r.idItemVenda()).orElseThrow(()->new InvalidInputException("Item de origem inválido"));
   if(!original.getVenda().getId().equals(e.getVenda().getId()) || !original.getProduto().getId().equals(r.idProduto()))throw new InvalidInputException("Item/produto não pertence à origem");
   var i=r.id()==null?new ItemDevolucaoCliente():old.get(r.id());
   if(i==null || (r.id()!=null && !retained.add(r.id())))throw new InvalidInputException("ID de item estrangeiro ou duplicado");
   boolean same=i.getItemVenda()!=null && i.getItemVenda().getId().equals(original.getId());
   i.setDevolucaoCliente(e);i.setItemVenda(original);i.setProduto(original.getProduto());i.setQuantidade(r.quantidade());i.setObservacao(r.observacao());i.setRetornaAoEstoque(r.retornaAoEstoque());
   if(!same) {i.setNomeProdutoSnapshot(original.getProduto().getNome());var unit=original.getUnidadeSiglaSnapshot();i.setUnidadeSiglaSnapshot(unit==null?original.getProduto().getUnidadeMedida().getSigla():unit);i.setUnidadeOrigem(unit==null?"UNIDADE_NA_CRIACAO_DEVOLUCAO":"VENDA");}
   if(r.id()==null)added.add(i);
  }
  e.getItens().removeIf(i->!retained.contains(i.getId()));e.getItens().addAll(added);
 }
 private OffsetDateTime now() {return OffsetDateTime.now(java.time.ZoneOffset.UTC).truncatedTo(java.time.temporal.ChronoUnit.MICROS);}
 private String hash(DevolucaoClienteRequestDTO d) {
  var text=new StringBuilder();
  for(Object v:new Object[]{d.idVenda(),d.idUsuario(),d.dataDevolucao(),d.status(),d.motivo(),d.observacao(),d.versao()})append(text,v);
  for(var i:d.itens()){append(text,i.id());append(text,i.idItemVenda());append(text,i.idProduto());append(text,i.quantidade().stripTrailingZeros().toPlainString());append(text,i.observacao());append(text,i.retornaAoEstoque());}
  try{return HexFormat.of().formatHex(java.security.MessageDigest.getInstance("SHA-256").digest(text.toString().getBytes(java.nio.charset.StandardCharsets.UTF_8)));}catch(java.security.NoSuchAlgorithmException ex){throw new IllegalStateException(ex);}
 }
 private void append(StringBuilder text,Object value) {if(value==null)text.append("-1:");else {var s=value.toString();text.append(s.length()).append(':').append(s);}}
}
