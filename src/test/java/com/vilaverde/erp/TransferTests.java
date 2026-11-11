package com.vilaverde.erp;

import com.vilaverde.erp.dto.recebimento.*;
import com.vilaverde.erp.dto.venda.*;
import com.vilaverde.erp.dto.transferencia.*;
import com.vilaverde.erp.entity.enums.StatusDocumento;
import com.vilaverde.erp.exception.*;
import com.vilaverde.erp.service.*;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.*;
import java.util.concurrent.*;
import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

@SpringBootTest
@AutoConfigureMockMvc
class TransferTests {
  @DynamicPropertySource static void db(DynamicPropertyRegistry r) {
    r.add("spring.datasource.url", () -> Objects.requireNonNull(System.getenv("TEST_DATABASE_URL"),"Use scripts/test-postgres.sh"));
    r.add("spring.datasource.username", () -> "stage7");
    r.add("spring.datasource.password", () -> "local-stage7-only");
  }
  @Autowired JdbcTemplate sql;
  @Autowired RecebimentoCompraService service;
  @Autowired CompraService compras;
  @Autowired MockMvc mvc;
  @Autowired org.springframework.web.context.WebApplicationContext web;
  @Autowired com.vilaverde.erp.repository.UsuarioRepository users;
  org.springframework.security.core.Authentication authentication;
  @AfterEach void clearSecurity() { org.springframework.security.core.context.SecurityContextHolder.clearContext(); }

  UUID loja,usuario,produto,compra,item;
  @BeforeEach void seed() {
    sql.execute("TRUNCATE loja,usuario,categoria,unidade_medida,fornecedor,recebimento_idempotencia CASCADE");
    loja=UUID.randomUUID(); usuario=UUID.randomUUID(); produto=UUID.randomUUID();
    UUID categoria=UUID.randomUUID(), unidade=UUID.randomUUID(), fornecedor=UUID.randomUUID();
    sql.update("insert into loja(id,cnpj,nome_fantasia,razao_social,bairro,cidade,estado,numero,status,criado_em) values (?,'12345678901234','Loja','Loja','B','C','SC','1','ATIVA',now())",loja);
    sql.update("insert into usuario(id,nome,email,usuario,cargo,senha_hash,status,criado_em) values (?,'Operador','op@example.test','operador','ADMIN','test-only','ATIVO',now())",usuario);
    sql.update("insert into categoria values (?,'Categoria')",categoria);
    sql.update("insert into unidade_medida(id,nome,sigla) values (?,'Unidade','UN')",unidade);
    SkuFixtures.create(sql,produto,categoria,unidade,"Produto");
    sql.update("insert into fornecedor(id,nome_fantasia,razao_social,estado,cidade,bairro,status) values (?,'Fornecedor','F','SC','C','B','ATIVO')",fornecedor);
    sql.update("update usuario set revisado=true,alterar_senha=false where id=?",usuario);
    var principal=com.vilaverde.erp.security.AuthPrincipal.of(users.findById(usuario).orElseThrow(),false);
    authentication=org.springframework.security.authentication.UsernamePasswordAuthenticationToken.authenticated(principal,null,principal.getAuthorities());
    org.springframework.security.core.context.SecurityContextHolder.getContext().setAuthentication(authentication);
    var session=new org.springframework.mock.web.MockHttpSession();session.setAttribute("authenticatedAt",System.currentTimeMillis());
    mvc=org.springframework.test.web.servlet.setup.MockMvcBuilders.webAppContextSetup(web)
      .apply(org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity())
      .defaultRequest(get("/").session(session).with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication(authentication)).with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf())).build();
    compra=purchase(fornecedor); item=purchaseItem(compra,produto,"10"); destination();
  }
  UUID purchase(UUID fornecedor) {
    UUID id=UUID.randomUUID();
    sql.update("insert into compra(id,id_fornecedor,id_loja,status,valor_total,data_compra,criado_em) values (?,?,?,'RASCUNHO',100,now(),now())",id,fornecedor,loja); return id;
  }
  UUID purchaseItem(UUID compra,UUID produto,String q) {
    UUID id=UUID.randomUUID();
    sql.update("insert into item_compra(id,id_compra,id_produto_sku,quantidade,quantidade_recebida,custo_unitario,desconto,total_item) values (?,?,?,?,0,10,0,100)",id,compra,produto,new BigDecimal(q)); return id;
  }

 @Autowired TransferenciaEstoqueService transfers;
 @Autowired VendaService sales;
 UUID destino;
 void destination() {destino=UUID.randomUUID();sql.update("insert into loja(id,cnpj,nome_fantasia,razao_social,bairro,cidade,estado,numero,status,criado_em) values (?,'22345678901234','Destino','Destino','B','C','SC','1','ATIVA',now())",destino);}
 ItemTransferenciaRequestDTO row(UUID id,UUID p,String q) {return new ItemTransferenciaRequestDTO(id,p,new BigDecimal(q));}
 TransferenciaRequestDTO request(UUID a,UUID b,Integer v,List<ItemTransferenciaRequestDTO> rows) {return new TransferenciaRequestDTO(a,b,null,null,null,"Obs",v,rows);}
 TransferenciaResponseDTO draft(UUID a,UUID b,String q) {return transfers.criar(UUID.randomUUID(),request(a,b,null,List.of(row(null,produto,q))));}
 void stock(UUID p,UUID l,String q) {sql.update("insert into estoque_produto(id,id_produto_sku,id_loja,quantidade_atual,atualizado_em) values (?,?,?,?,now()) on conflict(id_produto_sku,id_loja) do update set quantidade_atual=excluded.quantidade_atual",UUID.randomUUID(),p,l,new BigDecimal(q));}
 void balance(UUID p,UUID l,String n) {assertEquals(0,new BigDecimal(n).compareTo(sql.queryForObject("select coalesce(sum(quantidade_atual),0) from estoque_produto where id_produto_sku=? and id_loja=?",BigDecimal.class,p,l)));}
 long movements() {return sql.queryForObject("select count(*) from movimentacao_estoque",Long.class);}
 @Test void lifecycleIdentityAuditSnapshotsVersionsAndReplay() {
   stock(produto,loja,"10");var a=draft(loja,destino,"2");assertEquals(usuario,a.idUsuario());assertEquals("UN",a.itens().getFirst().unidadeSiglaSnapshot());
   var b=transfers.atualizar(a.id(),request(loja,destino,a.versao(),List.of(row(a.itens().getFirst().id(),produto,"3"))));
   assertEquals(a.itens().getFirst().id(),b.itens().getFirst().id());assertTrue(b.versao()>a.versao());
   assertThrows(ConflictException.class,()->transfers.atualizar(b.id(),request(loja,destino,a.versao(),List.of(row(null,produto,"1")))));
   sql.update("update unidade_medida set sigla='NEW'");
   var done=transfers.concluir(b.id(),b.versao());assertEquals("CONCLUIDA",done.status().name());assertEquals(usuario,done.idConfirmador());assertNotNull(done.concluidoEm());assertEquals("UN",done.itens().getFirst().unidadeSiglaSnapshot());
   balance(produto,loja,"7");balance(produto,destino,"3");assertEquals(2,movements());
   assertEquals(2,sql.queryForObject("select count(*) from movimentacao_estoque where id_transferencia=? and id_item_transferencia=? and id_usuario=? and quantidade=3 and data_movimentacao=?",Long.class,done.id(),done.itens().getFirst().id(),usuario,done.concluidoEm()));
   transfers.concluir(done.id(),0);assertEquals(2,movements());assertThrows(ConflictException.class,()->transfers.cancelar(done.id(),done.versao(),"No"));
   var c=draft(loja,destino,"1");var cancelled=transfers.cancelar(c.id(),c.versao(),"Desistência");assertEquals(usuario,cancelled.idCancelador());assertEquals(cancelled,transfers.cancelar(c.id(),0,"Desistência"));balance(produto,loja,"7");assertEquals(2,movements());
   assertThrows(ConflictException.class,()->transfers.concluir(c.id(),cancelled.versao()));
 }
 @Test void validationItemOwnershipAndPersistedCreationIdempotency() throws Exception {
   for(String q:List.of("0","-1","1.0001","100000000000"))assertThrows(InvalidInputException.class,()->draft(loja,destino,q));
   assertThrows(InvalidInputException.class,()->draft(loja,loja,"1"));
   assertThrows(InvalidInputException.class,()->transfers.criar(UUID.randomUUID(),request(loja,destino,null,List.of(row(null,produto,"1"),row(null,produto,"1")))));
   var a=draft(loja,destino,"1");var b=draft(loja,destino,"1");
   assertThrows(InvalidInputException.class,()->transfers.atualizar(a.id(),request(loja,destino,a.versao(),List.of(row(b.itens().getFirst().id(),produto,"1")))));
   UUID key=UUID.randomUUID();var d=request(loja,destino,null,List.of(row(null,produto,"1")));
   var results=parallel(()->transfers.criar(key,d),()->transfers.criar(key,d));assertEquals(((TransferenciaResponseDTO)results.getFirst()).id(),((TransferenciaResponseDTO)results.get(1)).id());
   assertThrows(ConflictException.class,()->transfers.criar(key,request(loja,destino,null,List.of(row(null,produto,"2")))));
   assertThrows(InvalidInputException.class,()->transfers.criar(UUID.randomUUID(),new TransferenciaRequestDTO(loja,destino,null,null,StatusDocumento.CONCLUIDA,null,null,d.itens())));
   assertThrows(InvalidInputException.class,()->transfers.cancelar(a.id(),a.versao()," "));
 }
 @Test void shortagesAndOverflowRollbackEveryBalanceIncludingInsertedZeroRows() {
   UUID second=UUID.randomUUID();SkuFixtures.cloneSku(sql,second,produto,"Second");
   stock(produto,loja,"10");var a=transfers.criar(UUID.randomUUID(),request(loja,destino,null,List.of(row(null,produto,"1"),row(null,second,"1"))));
   assertThrows(ConflictException.class,()->transfers.concluir(a.id(),a.versao()));balance(produto,loja,"10");assertEquals(1,sql.queryForObject("select count(*) from estoque_produto",Long.class));assertEquals(0,movements());assertEquals(a.versao(),transfers.buscar(a.id()).versao());
   stock(second,loja,"10");stock(second,destino,"99999999999.999");
   assertThrows(ConflictException.class,()->transfers.concluir(a.id(),a.versao()));balance(produto,loja,"10");balance(second,destino,"99999999999.999");assertEquals(0,movements());assertEquals("RASCUNHO",transfers.buscar(a.id()).status().name());
 }
 @Test void oppositeDirectionsAndCompetingOriginsNeverDeadlockOrLoseStock() throws Exception {
   stock(produto,loja,"10");stock(produto,destino,"10");var a=draft(loja,destino,"3");var b=draft(destino,loja,"2");
   parallel(()->transfers.concluir(a.id(),0),()->transfers.concluir(b.id(),0));balance(produto,loja,"9");balance(produto,destino,"11");assertEquals(4,movements());
   var c=draft(loja,destino,"6");var d=draft(loja,destino,"6");var results=parallel(()->transfers.concluir(c.id(),0),()->transfers.concluir(d.id(),0));assertEquals(1,results.stream().filter(ConflictException.class::isInstance).count());balance(produto,loja,"3");balance(produto,destino,"17");assertEquals(6,movements());
 }
 @Test void receivingIntoEitherStoreAndConcurrentSaleAreAtomic() throws Exception {
   stock(produto,loja,"10");
   var receipt=service.criar(UUID.randomUUID(),new RecebimentoCompraRequestDTO(compra,null,null,null,StatusDocumento.RASCUNHO,null,List.of(new ItemRecebimentoRequestDTO(null,item,produto,new BigDecimal("4"),null))));
   var a=draft(loja,destino,"3");parallel(()->transfers.concluir(a.id(),0),()->service.concluir(receipt.id(),0));balance(produto,loja,"11");balance(produto,destino,"3");
   sql.update("update compra set id_loja=? where id=?",destino,compra);
   var receipt2=service.criar(UUID.randomUUID(),new RecebimentoCompraRequestDTO(compra,null,null,null,StatusDocumento.RASCUNHO,null,List.of(new ItemRecebimentoRequestDTO(null,item,produto,new BigDecimal("2"),null))));
   var b=draft(loja,destino,"1");parallel(()->transfers.concluir(b.id(),0),()->service.concluir(receipt2.id(),0));balance(produto,loja,"10");balance(produto,destino,"6");
   UUID customer=UUID.randomUUID();sql.update("insert into cliente(id,nome,criado_em) values (?,'C',now())",customer);
   var sale=sales.criar(UUID.randomUUID(),new VendaRequestDTO(null,null,null,customer,null,loja,null,List.of(new ProdutoVendaRequestDTO(null,produto,new BigDecimal("7"),BigDecimal.ONE,BigDecimal.ZERO))));
   var c=draft(loja,destino,"7");var results=parallel(()->transfers.concluir(c.id(),0),()->sales.concluir(sale.id(),0));assertEquals(1,results.stream().filter(ConflictException.class::isInstance).count());balance(produto,loja,"3");
 }
 void principal(String cargo,Set<UUID> stores) {
   var p=new com.vilaverde.erp.security.AuthPrincipal(usuario,"op@example.test","operador","Op",com.vilaverde.erp.entity.enums.CargoUsuario.valueOf(cargo),stores,0,false,true,null);
   org.springframework.security.core.context.SecurityContextHolder.getContext().setAuthentication(org.springframework.security.authentication.UsernamePasswordAuthenticationToken.authenticated(p,null,p.getAuthorities()));
 }
 @Test void bothOldAndNewStoresAndReplayRequireScopeAndLegacyIsReadOnly() {
   UUID key=UUID.randomUUID();var d=request(loja,destino,null,List.of(row(null,produto,"1")));var a=transfers.criar(key,d);
   principal("GER",Set.of(loja));assertTrue(transfers.listar(null,null,null).isEmpty());assertThrows(ResourceNotFoundException.class,()->transfers.buscar(a.id()));assertThrows(ResourceNotFoundException.class,()->transfers.criar(key,d));assertThrows(ResourceNotFoundException.class,()->transfers.concluir(a.id(),0));
   principal("GER",Set.of(loja,destino));assertEquals(a.id(),transfers.buscar(a.id()).id());
   UUID third=UUID.randomUUID();assertThrows(ResourceNotFoundException.class,()->transfers.atualizar(a.id(),request(loja,third,0,d.itens())));
   principal("FUNC",Set.of(loja,destino));assertThrows(org.springframework.security.access.AccessDeniedException.class,()->transfers.buscar(a.id()));
   principal("ADMIN",Set.of());sql.update("update transferencia_estoque set legado=true where id=?",a.id());assertThrows(ConflictException.class,()->transfers.concluir(a.id(),0));assertThrows(ConflictException.class,()->transfers.cancelar(a.id(),0,"No"));
 }
 @Test void csrfAndApiContract() throws Exception {
   var deleteId=draft(loja,destino,"1").id();
   var d="{\"idLojaOrigem\":\""+loja+"\",\"idLojaDestino\":\""+destino+"\",\"itens\":[{\"idProdutoSku\":\""+produto+"\",\"quantidade\":1}]}";
   mvc.perform(post("/api/transferencias-estoque").with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf().useInvalidToken()).header("Idempotency-Key",UUID.randomUUID()).contentType("application/json").content(d)).andExpect(status().isForbidden());
   mvc.perform(post("/api/transferencias-estoque").header("Idempotency-Key",UUID.randomUUID()).contentType("application/json").content(d)).andExpect(status().isCreated()).andExpect(jsonPath("$.itens[0].id").exists()).andExpect(jsonPath("$.versao").value(0));
   mvc.perform(delete("/api/transferencias-estoque/"+deleteId)).andExpect(status().isForbidden());
 }
  List<Object> parallel(Callable<?> a,Callable<?> b) throws Exception {
    var start=new CountDownLatch(1); var ready=new CountDownLatch(2);
    try(var pool=Executors.newFixedThreadPool(2)) {
      var futures=List.of(a,b).stream().map(task->pool.submit(()->{org.springframework.security.core.context.SecurityContextHolder.getContext().setAuthentication(authentication); ready.countDown(); start.await(); try {return task.call();} catch (ConflictException e) {return e;} })).toList();
      assertTrue(ready.await(5,TimeUnit.SECONDS)); start.countDown();
      List<Object> result=new ArrayList<>(); for(var f:futures) result.add(f.get(20,TimeUnit.SECONDS)); return result;
    }
  }


 @Test void modificationConfirmationAndCreationReplayHaveIndependentActors() {
   stock(produto,loja,"5");UUID key=UUID.randomUUID();var d=request(loja,destino,null,List.of(row(null,produto,"1")));var a=transfers.criar(key,d);
   UUID modifier=UUID.randomUUID();sql.update("insert into usuario(id,nome,email,usuario,cargo,senha_hash,status,criado_em,revisado,alterar_senha) values (?,'Modifier','modifier@example.test','modifier','ADMIN','test','ATIVO',now(),true,false)",modifier);
   var p=com.vilaverde.erp.security.AuthPrincipal.of(users.findById(modifier).orElseThrow(),false);org.springframework.security.core.context.SecurityContextHolder.getContext().setAuthentication(org.springframework.security.authentication.UsernamePasswordAuthenticationToken.authenticated(p,null,p.getAuthorities()));
   assertThrows(ResourceNotFoundException.class,()->transfers.criar(key,d));
   var b=transfers.atualizar(a.id(),request(loja,destino,0,List.of(row(a.itens().getFirst().id(),produto,"2"))));assertEquals(usuario,b.idUsuario());assertEquals(modifier,b.idModificador());
   var done=transfers.concluir(b.id(),b.versao());assertEquals(usuario,done.idUsuario());assertEquals(modifier,done.idConfirmador());assertEquals(2,sql.queryForObject("select count(*) from movimentacao_estoque where id_usuario=?",Long.class,modifier));
 }
 @Test void childOmissionAndMovementFailureRollbackTheWholeDocument() {
   stock(produto,loja,"5");UUID second=UUID.randomUUID();SkuFixtures.cloneSku(sql,second,produto,"Second");
   var a=transfers.criar(UUID.randomUUID(),request(loja,destino,null,List.of(row(null,produto,"1"),row(null,second,"1"))));
   var b=transfers.atualizar(a.id(),request(loja,destino,0,List.of(row(a.itens().getFirst().id(),produto,"2"))));assertEquals(1,b.itens().size());assertEquals(1,sql.queryForObject("select count(*) from item_transferencia",Long.class));
   sql.execute("create function fail_transfer_movement() returns trigger language plpgsql as $$ begin raise exception 'forced transfer rollback'; end $$");sql.execute("create trigger fail_transfer_movement before insert on movimentacao_estoque for each row execute function fail_transfer_movement()");
   try {assertThrows(Exception.class,()->transfers.concluir(b.id(),b.versao()));balance(produto,loja,"5");balance(produto,destino,"0");assertEquals(0,movements());assertEquals(b.versao(),transfers.buscar(b.id()).versao());assertNull(transfers.buscar(b.id()).concluidoEm());assertEquals(1,sql.queryForObject("select count(*) from estoque_produto",Long.class));}
   finally {sql.execute("drop trigger fail_transfer_movement on movimentacao_estoque");sql.execute("drop function fail_transfer_movement()");}
 }
}
