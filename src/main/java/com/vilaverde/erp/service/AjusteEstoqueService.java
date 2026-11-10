package com.vilaverde.erp.service;
import com.vilaverde.erp.dto.ajuste.*;
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
public class AjusteEstoqueService {
 private final AjusteEstoqueRepository repository;
 private final LojaRepository lojas;
 private final ProdutoSkuRepository produtos;
 private final MovimentacaoEstoqueRepository movimentos;
 private final AccessScope scope;
 private final JdbcTemplate jdbc;
 private void permission() {
   if(scope.principal().cargo()==CargoUsuario.FUNC) throw new org.springframework.security.access.AccessDeniedException("Ajustes exigem ADMIN/GER");
 }
 private boolean permits(AjusteEstoque t) {return scope.permits(t.getLoja().getId());}
 @Transactional(readOnly=true)
 public List<AjusteResponseDTO> listar(UUID loja,StatusDocumento status) {
   permission();var p=scope.principal();
   return repository.findAll((org.springframework.data.jpa.domain.Specification<AjusteEstoque>)(r,q,c)-> {
     var restrictions=new ArrayList<jakarta.persistence.criteria.Predicate>();
     if(p.cargo()!=CargoUsuario.ADMIN) {
       if(p.lojas().isEmpty()) return c.disjunction();
       restrictions.add(r.get("loja").get("id").in(p.lojas()));
     }
     if(loja!=null) restrictions.add(c.equal(r.get("loja").get("id"),loja));
     if(status!=null) restrictions.add(c.equal(r.get("status"),status));
     return c.and(restrictions.toArray(jakarta.persistence.criteria.Predicate[]::new));
   }).stream().map(AjusteResponseDTO::fromEntity).toList();
 }
 private AjusteEstoque find(UUID id,boolean lock) {
   permission(); return (lock ? repository.lockById(id) : repository.findById(id)).filter(this::permits).orElseThrow(()->new ResourceNotFoundException("Ajuste não encontrada"));
 }
 @Transactional(readOnly=true) public AjusteResponseDTO buscar(UUID id) {return AjusteResponseDTO.fromEntity(find(id,false));}
 @Transactional public AjusteResponseDTO criar(UUID key,AjusteRequestDTO d) {
   permission();validate(d);scope.actor(d.idUsuario());scope.store(d.idLoja());
   if(key==null) throw new InvalidInputException("Informe Idempotency-Key UUID");
   jdbc.query("select pg_advisory_xact_lock(hashtextextended(?,0))",rs->{},"transfer:"+key);
   String hash=hash(d);
   var previous=jdbc.queryForList("select * from ajuste_idempotencia where chave=?",key);
   if(!previous.isEmpty()) {
     var row=previous.getFirst();
     if(!scope.principal().id().equals(row.get("actor_id"))) throw new ResourceNotFoundException("Ajuste não encontrada");
     if(!hash.equals(row.get("request_hash"))) throw new ConflictException("Chave reutilizada com outro conteúdo");
     return AjusteResponseDTO.fromEntity(find((UUID)row.get("ajuste_id"),false));
   }
   if(d.versao()!=null) throw new InvalidInputException("Criação não aceita versão");
   AjusteEstoque t=new AjusteEstoque();t.setUsuario(scope.actor());apply(t,d);repository.saveAndFlush(t);
   jdbc.update("insert into ajuste_idempotencia(chave,request_hash,ajuste_id,actor_id) values (?,?,?,?)",key,hash,t.getId(),scope.principal().id());
   return AjusteResponseDTO.fromEntity(t);
 }
 @Transactional public AjusteResponseDTO atualizar(UUID id,AjusteRequestDTO d) {
   var t=find(id,true);open(t,d.versao());validate(d);apply(t,d);repository.flush();return AjusteResponseDTO.fromEntity(t);
 }
 private void operational(AjusteEstoque t) {if(t.isLegado())throw new ConflictException("Ajuste histórica somente para consulta; reconciliação necessária");}
 private void open(AjusteEstoque t,Integer version) {
   operational(t);
   if(version==null || version<0) throw new InvalidInputException("Informe a versão");
   if(t.getStatus()!=StatusDocumento.RASCUNHO)throw new ConflictException("Apenas rascunhos podem ser alterados");
   if(!version.equals(t.getVersao()))throw new ConflictException("Versão desatualizada; recarregue e revise a ajuste");
 }
 @Transactional public AjusteResponseDTO concluir(UUID id,Integer version) {
   var t=find(id,true);operational(t);
   if(t.getStatus()==StatusDocumento.CONCLUIDO)return AjusteResponseDTO.fromEntity(t);
   open(t,version);
   if(t.getItens().isEmpty()) throw new ConflictException("Ajuste sem itens");
   var items=t.getItens().stream().sorted(Comparator.comparing(i->i.getProdutoSku().getId().toString())).toList();
   var store=t.getLoja().getId();
   Map<UUID,BigDecimal> balances=new HashMap<>();
   var problems=new ArrayList<String>();
   for(var i:items) {
     jdbc.update("insert into estoque_produto(id,id_produto_sku,id_loja,quantidade_atual,atualizado_em) values (?,?,?,0,now()) on conflict(id_produto_sku,id_loja) do nothing",UUID.randomUUID(),i.getProdutoSku().getId(),store);
     var before=jdbc.queryForObject("select quantidade_atual from estoque_produto where id_produto_sku=? and id_loja=? for update",BigDecimal.class,i.getProdutoSku().getId(),store);
     balances.put(i.getProdutoSku().getId(),before);
     var after=before.add(i.getTipo()==TipoMovimentacao.AJUSTE_ENTRADA ? i.getQuantidade():i.getQuantidade().negate());
     if(after.signum()<0 || after.compareTo(new BigDecimal("100000000000"))>=0)problems.add(i.getProdutoSku().getDisplayName()+": saldo "+before+", resultado inválido "+after);
   }
   if(!problems.isEmpty())throw new ConflictException("Ajuste bloqueada: "+String.join("; ",problems));
   var now=now();var actor=scope.actor();
   for(var i:items) {
     var delta=i.getTipo()==TipoMovimentacao.AJUSTE_ENTRADA ? i.getQuantidade():i.getQuantidade().negate();
     i.setSaldoAntes(balances.get(i.getProdutoSku().getId()));i.setSaldoDepois(i.getSaldoAntes().add(delta));
     jdbc.update("update estoque_produto set quantidade_atual=quantidade_atual+?,atualizado_em=? where id_produto_sku=? and id_loja=?",delta,now,i.getProdutoSku().getId(),store);
     var m=new MovimentacaoEstoque();m.setAjusteEstoque(t);m.setItemAjusteEstoque(i);m.setProdutoSku(i.getProdutoSku()); m.snapshotFrom(i);m.setLoja(t.getLoja());m.setTipo(i.getTipo());m.setQuantidade(i.getQuantidade());m.setObservacao(i.getObservacao());m.setUsuario(actor);m.setDataMovimentacao(now);movimentos.save(m);
   }

   t.setStatus(StatusDocumento.CONCLUIDO);t.setConfirmador(actor);t.setModificador(actor);t.setConcluidoEm(now);t.setAtualizadoEm(now);
   repository.flush();return AjusteResponseDTO.fromEntity(t);
 }
 @Transactional public AjusteResponseDTO cancelar(UUID id,Integer version,String reason) {
   var t=find(id,true);operational(t);
   if(reason==null || reason.isBlank())throw new InvalidInputException("Informe o motivo do cancelamento");
   if(t.getStatus()==StatusDocumento.CANCELADO)return AjusteResponseDTO.fromEntity(t);
   open(t,version);t.setStatus(StatusDocumento.CANCELADO);t.setCancelador(scope.actor());t.setModificador(scope.actor());
   t.setCanceladoEm(now());t.setAtualizadoEm(t.getCanceladoEm());t.setMotivoCancelamento(reason.trim());repository.flush();return AjusteResponseDTO.fromEntity(t);
 }
 private OffsetDateTime now() {return OffsetDateTime.now(java.time.ZoneOffset.UTC).truncatedTo(java.time.temporal.ChronoUnit.MICROS);}
 private void validate(AjusteRequestDTO d) {
   if(d.status()!=null)throw new InvalidInputException("Use concluir/cancelar para alterar status");
   if(d.idLoja()==null || d.justificativa()==null || d.justificativa().isBlank())throw new InvalidInputException("Loja e justificativa obrigatórias");
   if(d.justificativa().length()>255)throw new InvalidInputException("Justificativa deve ter até 255 caracteres");
   if(d.itens()==null || d.itens().isEmpty())throw new InvalidInputException("Informe itens");
   var products=new HashSet<UUID>();
   for(var i:d.itens()) {
     if(i==null || i.idProdutoSku()==null || !products.add(i.idProdutoSku()))throw new InvalidInputException("Produtos obrigatórios e únicos");
     if(i.tipo()!=TipoMovimentacao.AJUSTE_ENTRADA && i.tipo()!=TipoMovimentacao.AJUSTE_SAIDA)throw new InvalidInputException("Tipo de ajuste inválido");
     if(i.observacao()!=null && i.observacao().length()>255)throw new InvalidInputException("Observação deve ter até 255 caracteres");
     var q=i.quantidade();if(q==null || q.signum()<=0 || q.scale()>3 || q.compareTo(new BigDecimal("100000000000"))>=0)throw new InvalidInputException("Quantidade positiva numeric(14,3) obrigatória");
   }
 }
 private void apply(AjusteEstoque t,AjusteRequestDTO d) {
   scope.store(d.idLoja());var actor=scope.actor(d.idUsuario());
   t.setLoja(lojas.findById(d.idLoja()).orElseThrow(()->new ResourceNotFoundException("Loja não encontrada")));
   t.setModificador(actor);t.setAtualizadoEm(now());t.setJustificativa(d.justificativa().trim());
   Map<UUID,ItemAjusteEstoque> old=new HashMap<>();t.getItens().forEach(i->old.put(i.getId(),i));var retained=new HashSet<UUID>();var added=new ArrayList<ItemAjusteEstoque>();
   for(var r:d.itens()) {
     var i=r.id()==null ? new ItemAjusteEstoque():old.get(r.id());
     if(i==null || (r.id()!=null && !retained.add(r.id())))throw new InvalidInputException("ID de item estrangeiro ou duplicado");
     boolean same=i.getProdutoSku()!=null && i.getProdutoSku().getId().equals(r.idProdutoSku());
     i.setAjusteEstoque(t);i.setProdutoSku(produtos.findById(r.idProdutoSku()).orElseThrow(()->new ResourceNotFoundException("SKU não encontrado")));i.setQuantidade(r.quantidade());i.setTipo(r.tipo());i.setObservacao(r.observacao());
     if(!same)i.setUnidadeSiglaSnapshot(i.getProdutoSku().getUnidadeMedida().getSigla());
     if(r.id()==null)added.add(i);
   }
   t.getItens().removeIf(i->!retained.contains(i.getId()));t.getItens().addAll(added);
 }
 private String hash(AjusteRequestDTO d) {
   var s=new StringBuilder();
   for(Object v:new Object[]{d.idLoja(),d.idUsuario(),d.versao(),d.status(),d.justificativa()})append(s,v);
   for(var i:d.itens().stream().sorted(Comparator.comparing(r->r.idProdutoSku().toString())).toList()){append(s,i.id());append(s,i.idProdutoSku());append(s,i.quantidade().stripTrailingZeros().toPlainString());append(s,i.tipo());append(s,i.observacao());}
   try{return HexFormat.of().formatHex(java.security.MessageDigest.getInstance("SHA-256").digest(s.toString().getBytes(java.nio.charset.StandardCharsets.UTF_8)));}catch(java.security.NoSuchAlgorithmException e){throw new IllegalStateException(e);}
 }
 private void append(StringBuilder s,Object v) {if(v==null)s.append("-1:");else {String a=v.toString();s.append(a.length()).append(':').append(a);}}
}
