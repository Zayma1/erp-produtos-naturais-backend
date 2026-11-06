package com.vilaverde.erp.service;
import com.vilaverde.erp.dto.transferencia.*;
import com.vilaverde.erp.entity.*;
import com.vilaverde.erp.entity.enums.*;
import com.vilaverde.erp.exception.*;
import com.vilaverde.erp.repository.*;
import com.vilaverde.erp.security.AccessScope;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.*;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service @RequiredArgsConstructor
public class TransferenciaEstoqueService {
 private final TransferenciaEstoqueRepository repository;
 private final LojaRepository lojas;
 private final ProdutoRepository produtos;
 private final MovimentacaoEstoqueRepository movimentos;
 private final AccessScope scope;
 private final JdbcTemplate jdbc;
 private void permission() {
   if(scope.principal().cargo()==CargoUsuario.FUNC) throw new org.springframework.security.access.AccessDeniedException("Transferências exigem ADMIN/GER");
 }
 private boolean permits(TransferenciaEstoque t) {return scope.permits(t.getLojaOrigem().getId()) && scope.permits(t.getLojaDestino().getId());}
 @Transactional(readOnly=true)
 public List<TransferenciaResponseDTO> listar(UUID origem,UUID destino,StatusDocumento status) {
   permission();var p=scope.principal();
   return repository.findAll((org.springframework.data.jpa.domain.Specification<TransferenciaEstoque>)(r,q,c)-> {
     var restrictions=new ArrayList<jakarta.persistence.criteria.Predicate>();
     if(p.cargo()!=CargoUsuario.ADMIN) {
       if(p.lojas().isEmpty()) return c.disjunction();
       restrictions.add(r.get("lojaOrigem").get("id").in(p.lojas())); restrictions.add(r.get("lojaDestino").get("id").in(p.lojas()));
     }
     if(origem!=null) restrictions.add(c.equal(r.get("lojaOrigem").get("id"),origem));
     if(destino!=null) restrictions.add(c.equal(r.get("lojaDestino").get("id"),destino));
     if(status!=null) restrictions.add(c.equal(r.get("status"),status));
     return c.and(restrictions.toArray(jakarta.persistence.criteria.Predicate[]::new));
   }).stream().map(TransferenciaResponseDTO::summary).toList();
 }
 private TransferenciaEstoque find(UUID id,boolean lock) {
   permission(); return (lock ? repository.lockById(id) : repository.findById(id)).filter(this::permits).orElseThrow(()->new ResourceNotFoundException("Transferência não encontrada"));
 }
 @Transactional(readOnly=true) public TransferenciaResponseDTO buscar(UUID id) {return TransferenciaResponseDTO.fromEntity(find(id,false));}
 @Transactional public TransferenciaResponseDTO criar(UUID key,TransferenciaRequestDTO d) {
   permission();validate(d);scope.actor(d.idUsuario());scope.store(d.idLojaOrigem());scope.store(d.idLojaDestino());
   if(key==null) throw new InvalidInputException("Informe Idempotency-Key UUID");
   jdbc.query("select pg_advisory_xact_lock(hashtextextended(?,0))",rs->{},"transfer:"+key);
   String hash=hash(d);
   var previous=jdbc.queryForList("select * from transferencia_idempotencia where chave=?",key);
   if(!previous.isEmpty()) {
     var row=previous.getFirst();
     if(!scope.principal().id().equals(row.get("actor_id"))) throw new ResourceNotFoundException("Transferência não encontrada");
     if(!hash.equals(row.get("request_hash"))) throw new ConflictException("Chave reutilizada com outro conteúdo");
     return TransferenciaResponseDTO.fromEntity(find((UUID)row.get("transferencia_id"),false));
   }
   if(d.versao()!=null) throw new InvalidInputException("Criação não aceita versão");
   TransferenciaEstoque t=new TransferenciaEstoque();t.setUsuario(scope.actor());apply(t,d);repository.saveAndFlush(t);
   jdbc.update("insert into transferencia_idempotencia(chave,request_hash,transferencia_id,actor_id) values (?,?,?,?)",key,hash,t.getId(),scope.principal().id());
   return TransferenciaResponseDTO.fromEntity(t);
 }
 @Transactional public TransferenciaResponseDTO atualizar(UUID id,TransferenciaRequestDTO d) {
   var t=find(id,true);open(t,d.versao());validate(d);apply(t,d);repository.flush();return TransferenciaResponseDTO.fromEntity(t);
 }
 private void operational(TransferenciaEstoque t) {if(t.isLegado())throw new ConflictException("Transferência histórica somente para consulta; reconciliação necessária");}
 private void open(TransferenciaEstoque t,Integer version) {
   operational(t);
   if(version==null || version<0) throw new InvalidInputException("Informe a versão");
   if(t.getStatus()!=StatusDocumento.RASCUNHO)throw new ConflictException("Apenas rascunhos podem ser alterados");
   if(!version.equals(t.getVersao()))throw new ConflictException("Versão desatualizada; recarregue e revise a transferência");
 }
 @Transactional public TransferenciaResponseDTO concluir(UUID id,Integer version) {
   var t=find(id,true);operational(t);
   if(t.getStatus()==StatusDocumento.CONCLUIDA)return TransferenciaResponseDTO.fromEntity(t);
   open(t,version);
   if(t.getItens().isEmpty()) throw new ConflictException("Transferência sem itens");
   var items=t.getItens().stream().sorted(Comparator.comparing(i->i.getProduto().getId().toString())).toList();
   var stores=List.of(t.getLojaOrigem().getId(),t.getLojaDestino().getId()).stream().sorted(Comparator.comparing(UUID::toString)).toList();
   Map<String,BigDecimal> balances=new HashMap<>();
   // Inserts and locks use the same product -> store order as stock writers. Inserts roll back on any failure.
   for(var i:items) for(var store:stores) {
     jdbc.update("insert into estoque_produto(id,id_produto,id_loja,quantidade_atual,atualizado_em) values (?,?,?,0,now()) on conflict(id_produto,id_loja) do nothing",UUID.randomUUID(),i.getProduto().getId(),store);
     balances.put(i.getProduto().getId()+":"+store,jdbc.queryForObject("select quantidade_atual from estoque_produto where id_produto=? and id_loja=? for update",BigDecimal.class,i.getProduto().getId(),store));
   }
   var problems=new ArrayList<String>();
   for(var i:items) {
     var available=balances.get(i.getProduto().getId()+":"+t.getLojaOrigem().getId());
     var destination=balances.get(i.getProduto().getId()+":"+t.getLojaDestino().getId());
     if(available.compareTo(i.getQuantidade())<0)problems.add(i.getProduto().getNome()+": disponível "+available+", necessário "+i.getQuantidade());
     if(destination.add(i.getQuantidade()).compareTo(new BigDecimal("100000000000"))>=0)problems.add(i.getProduto().getNome()+": limite numérico do destino excedido");
   }
   if(!problems.isEmpty())throw new ConflictException("Transferência bloqueada: "+String.join("; ",problems));
   var now=now();var actor=scope.actor();
   for(var i:items) {
     jdbc.update("update estoque_produto set quantidade_atual=quantidade_atual-?,atualizado_em=? where id_produto=? and id_loja=?",i.getQuantidade(),now,i.getProduto().getId(),t.getLojaOrigem().getId());
     jdbc.update("update estoque_produto set quantidade_atual=quantidade_atual+?,atualizado_em=? where id_produto=? and id_loja=?",i.getQuantidade(),now,i.getProduto().getId(),t.getLojaDestino().getId());
     for(boolean entrada:List.of(false,true)) {
       var m=new MovimentacaoEstoque();m.setTransferencia(t);m.setItemTransferencia(i);m.setProduto(i.getProduto());
       m.setLoja(entrada ? t.getLojaDestino():t.getLojaOrigem());m.setTipo(entrada ? TipoMovimentacao.TRANSFERENCIA_ENTRADA:TipoMovimentacao.TRANSFERENCIA_SAIDA);
       m.setQuantidade(i.getQuantidade());m.setUsuario(actor);m.setDataMovimentacao(now);movimentos.save(m);
     }
   }
   t.setStatus(StatusDocumento.CONCLUIDA);t.setConfirmador(actor);t.setModificador(actor);t.setConcluidoEm(now);t.setAtualizadoEm(now);
   repository.flush();return TransferenciaResponseDTO.fromEntity(t);
 }
 @Transactional public TransferenciaResponseDTO cancelar(UUID id,Integer version,String reason) {
   var t=find(id,true);operational(t);
   if(reason==null || reason.isBlank())throw new InvalidInputException("Informe o motivo do cancelamento");
   if(t.getStatus()==StatusDocumento.CANCELADA)return TransferenciaResponseDTO.fromEntity(t);
   open(t,version);t.setStatus(StatusDocumento.CANCELADA);t.setCancelador(scope.actor());t.setModificador(scope.actor());
   t.setCanceladoEm(now());t.setAtualizadoEm(t.getCanceladoEm());t.setMotivoCancelamento(reason.trim());repository.flush();return TransferenciaResponseDTO.fromEntity(t);
 }
 private OffsetDateTime now() {return OffsetDateTime.now(java.time.ZoneOffset.UTC).truncatedTo(java.time.temporal.ChronoUnit.MICROS);}
 private void validate(TransferenciaRequestDTO d) {
   if(d.status()!=null)throw new InvalidInputException("Use concluir/cancelar para alterar status");
   if(d.idLojaOrigem()==null || d.idLojaDestino()==null || d.idLojaOrigem().equals(d.idLojaDestino()))throw new InvalidInputException("Informe duas lojas distintas");
   if(d.observacao()!=null && d.observacao().length()>255)throw new InvalidInputException("Observação deve ter até 255 caracteres");
   if(d.itens()==null || d.itens().isEmpty())throw new InvalidInputException("Informe itens");
   var products=new HashSet<UUID>();
   for(var i:d.itens()) {
     if(i==null || i.idProduto()==null || !products.add(i.idProduto()))throw new InvalidInputException("Produtos obrigatórios e únicos");
     var q=i.quantidade();if(q==null || q.signum()<=0 || q.scale()>3 || q.compareTo(new BigDecimal("100000000000"))>=0)throw new InvalidInputException("Quantidade positiva numeric(14,3) obrigatória");
   }
 }
 private void apply(TransferenciaEstoque t,TransferenciaRequestDTO d) {
   scope.store(d.idLojaOrigem());scope.store(d.idLojaDestino());var actor=scope.actor(d.idUsuario());
   t.setLojaOrigem(lojas.findById(d.idLojaOrigem()).orElseThrow(()->new ResourceNotFoundException("Loja não encontrada")));
   t.setLojaDestino(lojas.findById(d.idLojaDestino()).orElseThrow(()->new ResourceNotFoundException("Loja não encontrada")));
   t.setModificador(actor);t.setAtualizadoEm(now());t.setDataTransferencia(d.dataTransferencia()==null ? now():d.dataTransferencia());t.setObservacao(d.observacao());
   Map<UUID,ItemTransferencia> old=new HashMap<>();t.getItens().forEach(i->old.put(i.getId(),i));var retained=new HashSet<UUID>();var added=new ArrayList<ItemTransferencia>();
   for(var r:d.itens()) {
     var i=r.id()==null ? new ItemTransferencia():old.get(r.id());
     if(i==null || (r.id()!=null && !retained.add(r.id())))throw new InvalidInputException("ID de item estrangeiro ou duplicado");
     boolean same=i.getProduto()!=null && i.getProduto().getId().equals(r.idProduto());
     i.setTransferencia(t);i.setProduto(produtos.findById(r.idProduto()).orElseThrow(()->new ResourceNotFoundException("Produto não encontrado")));i.setQuantidade(r.quantidade());
     if(!same)i.setUnidadeSiglaSnapshot(i.getProduto().getUnidadeMedida().getSigla());
     if(r.id()==null)added.add(i);
   }
   t.getItens().removeIf(i->!retained.contains(i.getId()));t.getItens().addAll(added);
 }
 private String hash(TransferenciaRequestDTO d) {
   var s=new StringBuilder();
   for(Object v:new Object[]{d.idLojaOrigem(),d.idLojaDestino(),d.idUsuario(),d.versao(),d.status(),d.dataTransferencia()==null?null:d.dataTransferencia().toInstant(),d.observacao()})append(s,v);
   for(var i:d.itens().stream().sorted(Comparator.comparing(r->r.idProduto().toString())).toList()){append(s,i.id());append(s,i.idProduto());append(s,i.quantidade().stripTrailingZeros().toPlainString());}
   try{return HexFormat.of().formatHex(java.security.MessageDigest.getInstance("SHA-256").digest(s.toString().getBytes(java.nio.charset.StandardCharsets.UTF_8)));}catch(java.security.NoSuchAlgorithmException e){throw new IllegalStateException(e);}
 }
 private void append(StringBuilder s,Object v) {if(v==null)s.append("-1:");else {String a=v.toString();s.append(a.length()).append(':').append(a);}}
}
