package com.vilaverde.erp;

import com.vilaverde.erp.dto.recebimento.*;
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
class ReceivingTests {
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
    sql.update("insert into usuario(id,nome,email,cargo,senha_hash,status,criado_em) values (?,'Operador','op@example.test','ADMIN','test-only','ATIVO',now())",usuario);
    sql.update("insert into categoria values (?,'Categoria')",categoria);
    sql.update("insert into unidade_medida(id,nome,sigla) values (?,'Unidade','UN')",unidade);
    sql.update("insert into produto(id,nome,id_categoria,id_unidade_medida,tipo_venda,status,criado_em) values (?,'Produto',?,?,'UNIDADE','ATIVO',now())",produto,categoria,unidade);
    sql.update("insert into fornecedor(id,nome_fantasia,razao_social,estado,cidade,bairro,status) values (?,'Fornecedor','F','SC','C','B','ATIVO')",fornecedor);
    sql.update("update usuario set revisado=true,alterar_senha=false where id=?",usuario);
    var principal=com.vilaverde.erp.security.AuthPrincipal.of(users.findById(usuario).orElseThrow(),false);
    authentication=org.springframework.security.authentication.UsernamePasswordAuthenticationToken.authenticated(principal,null,principal.getAuthorities());
    org.springframework.security.core.context.SecurityContextHolder.getContext().setAuthentication(authentication);
    var session=new org.springframework.mock.web.MockHttpSession();session.setAttribute("authenticatedAt",System.currentTimeMillis());
    mvc=org.springframework.test.web.servlet.setup.MockMvcBuilders.webAppContextSetup(web)
      .apply(org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity())
      .defaultRequest(get("/").session(session).with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication(authentication)).with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf())).build();
    compra=purchase(fornecedor); item=purchaseItem(compra,produto,"10");
  }
  UUID purchase(UUID fornecedor) {
    UUID id=UUID.randomUUID();
    sql.update("insert into compra(id,id_fornecedor,id_loja,status,valor_total,data_compra,criado_em) values (?,?,?,'RASCUNHO',100,now(),now())",id,fornecedor,loja); return id;
  }
  UUID purchaseItem(UUID compra,UUID produto,String q) {
    UUID id=UUID.randomUUID();
    sql.update("insert into item_compra(id,id_compra,id_produto,quantidade,quantidade_recebida,custo_unitario,desconto,total_item) values (?,?,?,?,0,10,0,100)",id,compra,produto,new BigDecimal(q)); return id;
  }
  RecebimentoCompraRequestDTO request(UUID c,UUID i,String q,Integer v) {
    return new RecebimentoCompraRequestDTO(c,usuario,v,OffsetDateTime.parse("2026-11-05T09:30:00-03:00"),StatusDocumento.RASCUNHO,"Header",List.of(new ItemRecebimentoRequestDTO(null,i,produto,new BigDecimal(q),"Item")));
  }
  RecebimentoCompraResponseDTO draft(String q) { return service.criar(UUID.randomUUID(),request(compra,item,q,null)); }
  BigDecimal balance() { return sql.queryForObject("select coalesce(sum(quantidade_atual),0) from estoque_produto",BigDecimal.class); }
  long movements() { return sql.queryForObject("select count(*) from movimentacao_estoque",Long.class); }
  void amount(String expected,BigDecimal actual) { assertEquals(0,new BigDecimal(expected).compareTo(actual)); }
  @Test void draftReplacementPreservesIdentityAndHasNoStockEffects() {
    var a=draft("2");
    var b=service.atualizar(a.id(),request(compra,item,"3",a.versao()));
    var c=service.atualizar(a.id(),request(compra,item,"4",b.versao()));
    assertEquals(1,c.itens().size()); assertEquals(a.itens().getFirst().id(),c.itens().getFirst().id());
    assertTrue(c.versao()>b.versao()); amount("0",balance()); assertEquals(0,movements());
    service.excluir(c.id(),c.versao()); assertThrows(ResourceNotFoundException.class,()->service.buscar(c.id()));
    assertEquals(0,sql.queryForObject("select count(*) from item_recebimento_compra",Integer.class));
  }
  @Test void partialFinalAndRepeatConfirmation() {
    var a=draft("4"); var done=service.concluir(a.id(),a.versao());
    assertNotNull(done.concluidoEm()); amount("4",balance()); assertEquals(1,movements());
    assertEquals("PARCIALMENTE_RECEBIDA",sql.queryForObject("select status from compra where id=?",String.class,compra));
    service.concluir(a.id(),a.versao()); amount("4",balance()); assertEquals(1,movements());
    var b=draft("6"); service.concluir(b.id(),b.versao()); amount("10",balance());
    assertEquals("RECEBIDA",sql.queryForObject("select status from compra where id=?",String.class,compra));
    amount("10",sql.queryForObject("select quantidade_recebida from item_compra where id=?",BigDecimal.class,item));
    amount("100",sql.queryForObject("select valor_total from compra where id=?",BigDecimal.class,compra));
    assertEquals(2,movements());
    assertEquals(usuario,sql.queryForObject("select id_usuario from movimentacao_estoque limit 1",UUID.class));
  }
  @Test void staleVersionsAndConfirmedImmutability() {
    var a=draft("2"); var b=service.atualizar(a.id(),request(compra,item,"3",a.versao()));
    assertThrows(ConflictException.class,()->service.atualizar(a.id(),request(compra,item,"4",a.versao())));
    assertThrows(ConflictException.class,()->service.excluir(a.id(),a.versao()));
    assertThrows(ConflictException.class,()->service.concluir(a.id(),a.versao()));
    var c=service.concluir(b.id(),b.versao());
    assertThrows(ConflictException.class,()->service.excluir(c.id(),c.versao()));
    assertThrows(ConflictException.class,()->service.atualizar(c.id(),request(compra,item,"4",c.versao())));
  }
  @Test void draftsDoNotReserveAndPurchaseIsLockedUntilAllDraftsDeleted() {
    var a=draft("8"); var b=draft("8");
    assertThrows(ConflictException.class,()->compras.atualizar(compra,null));
    service.excluir(a.id(),a.versao()); assertThrows(ConflictException.class,()->compras.atualizar(compra,null));
    service.excluir(b.id(),b.versao());
    // Passing the current DTO after deleting every draft is now allowed.
    var p=compras.buscar(compra);
    var d=new com.vilaverde.erp.dto.compra.CompraRequestDTO(p.idFornecedor(),p.idLoja(),null,p.dataCompra(),com.vilaverde.erp.entity.enums.StatusCompra.RASCUNHO,"",List.of(new com.vilaverde.erp.dto.compra.ItemCompraRequestDTO(null,produto,new BigDecimal("10"),BigDecimal.TEN,BigDecimal.ZERO)));
    compras.atualizar(compra,d);
  }
  @Test void wrongRelationshipsDuplicatesStatusAndItemOwnership() {
    var d=request(compra,item,"2",null);
    var wrong=new ItemRecebimentoRequestDTO(null,item,UUID.randomUUID(),BigDecimal.ONE,null);
    assertThrows(InvalidInputException.class,()->service.criar(UUID.randomUUID(),new RecebimentoCompraRequestDTO(compra,usuario,null,null,StatusDocumento.RASCUNHO,null,List.of(wrong))));
    assertThrows(InvalidInputException.class,()->service.criar(UUID.randomUUID(),new RecebimentoCompraRequestDTO(compra,usuario,null,null,StatusDocumento.RASCUNHO,null,List.of(d.itens().getFirst(),d.itens().getFirst()))));
    assertThrows(InvalidInputException.class,()->service.criar(UUID.randomUUID(),new RecebimentoCompraRequestDTO(compra,usuario,null,null,StatusDocumento.CONCLUIDO,null,d.itens())));
    var a=draft("1"); var b=draft("1");
    var stolen=new ItemRecebimentoRequestDTO(a.itens().getFirst().id(),item,produto,BigDecimal.ONE,null);
    assertThrows(InvalidInputException.class,()->service.atualizar(b.id(),new RecebimentoCompraRequestDTO(compra,usuario,b.versao(),null,StatusDocumento.RASCUNHO,null,List.of(stolen))));
    assertThrows(InvalidInputException.class,()->service.atualizar(a.id(),request(UUID.randomUUID(),item,"1",a.versao())));
  }
  @Test void overReceivingAndCancelledPurchase() {
    assertThrows(ConflictException.class,()->draft("11"));
    var a=draft("8"); var b=draft("8"); service.concluir(a.id(),a.versao());
    assertThrows(ConflictException.class,()->service.concluir(b.id(),b.versao())); amount("8",balance()); assertEquals(1,movements());
    sql.update("update compra set status='CANCELADA' where id=?",compra);
    assertThrows(ConflictException.class,()->draft("1"));
    assertThrows(ConflictException.class,()->service.concluir(b.id(),b.versao()));
  }
  @Test void idempotencyChangedPayloadAndTombstone() {
    UUID key=UUID.randomUUID(); var d=request(compra,item,"3",null);
    var a=service.criar(key,d); assertEquals(a.id(),service.criar(key,d).id());
    assertThrows(ConflictException.class,()->service.criar(key,request(compra,item,"4",null)));
    service.excluir(a.id(),a.versao()); assertThrows(ConflictException.class,()->service.criar(key,d));
    assertEquals(0,sql.queryForObject("select count(*) from recebimento_compra",Integer.class));
  }
  List<Object> parallel(Callable<?> a,Callable<?> b) throws Exception {
    var start=new CountDownLatch(1); var ready=new CountDownLatch(2);
    try(var pool=Executors.newFixedThreadPool(2)) {
      var futures=List.of(a,b).stream().map(task->pool.submit(()->{org.springframework.security.core.context.SecurityContextHolder.getContext().setAuthentication(authentication); ready.countDown(); start.await(); try {return task.call();} catch (ConflictException e) {return e;} })).toList();
      assertTrue(ready.await(5,TimeUnit.SECONDS)); start.countDown();
      List<Object> result=new ArrayList<>(); for(var f:futures) result.add(f.get(20,TimeUnit.SECONDS)); return result;
    }
  }
  @Test void concurrentCreationAndConfirmationAreIdempotent() throws Exception {
    UUID key=UUID.randomUUID(); var d=request(compra,item,"4",null);
    var results=parallel(()->service.criar(key,d),()->service.criar(key,d));
    assertEquals(((RecebimentoCompraResponseDTO)results.get(0)).id(),((RecebimentoCompraResponseDTO)results.get(1)).id());
    var a=(RecebimentoCompraResponseDTO)results.getFirst();
    parallel(()->service.concluir(a.id(),a.versao()),()->service.concluir(a.id(),a.versao()));
    amount("4",balance()); assertEquals(1,movements());
  }
  @Test void simultaneousReceiptsCannotOverReceive() throws Exception {
    var a=draft("7"); var b=draft("7");
    var results=parallel(()->service.concluir(a.id(),a.versao()),()->service.concluir(b.id(),b.versao()));
    assertEquals(1,results.stream().filter(ConflictException.class::isInstance).count()); amount("7",balance()); assertEquals(1,movements());
  }
  @Test void differentPurchasesIncrementSameStockWithoutLostUpdate() throws Exception {
    UUID fornecedor=sql.queryForObject("select id_fornecedor from compra where id=?",UUID.class,compra);
    UUID c=purchase(fornecedor),i=purchaseItem(c,produto,"10");
    var a=draft("4"); var b=service.criar(UUID.randomUUID(),request(c,i,"6",null));
    parallel(()->service.concluir(a.id(),a.versao()),()->service.concluir(b.id(),b.versao()));
    amount("10",balance()); assertEquals(2,movements());
  }
  @Test void failureAfterFirstStockWriteRollsBackEverything() {
    // Overflow the second stock increment after an earlier product has already been processed.
    UUID p=UUID.randomUUID(); sql.update("insert into produto select criado_em,?,id_categoria,id_unidade_medida,tipo_venda,descricao,nome,status from produto where id=?",p,produto);
    UUID i=purchaseItem(compra,p,"10");
    UUID last=produto.toString().compareTo(p.toString())>0 ? produto : p;
    sql.update("insert into estoque_produto(id,id_produto,id_loja,quantidade_atual,atualizado_em) values (?,?,?,99999999999.999,now())",UUID.randomUUID(),last,loja);
    var d=request(compra,item,"2",null);
    var a=service.criar(UUID.randomUUID(),new RecebimentoCompraRequestDTO(compra,usuario,null,d.dataRecebimento(),StatusDocumento.RASCUNHO,null,List.of(d.itens().getFirst(),new ItemRecebimentoRequestDTO(null,i,p,BigDecimal.ONE,null))));
    assertThrows(org.springframework.dao.DataAccessException.class,()->service.concluir(a.id(),a.versao()));
    assertEquals(StatusDocumento.RASCUNHO,service.buscar(a.id()).status()); assertEquals(0,movements());
    amount("0",sql.queryForObject("select sum(quantidade_recebida) from item_compra",BigDecimal.class));
    assertEquals(1,sql.queryForObject("select count(*) from estoque_produto",Integer.class));
    assertEquals("RASCUNHO",sql.queryForObject("select status from compra where id=?",String.class,compra));
  }
  @Test void forgedIdentityAndReplayByDifferentActorAreRejected() {
    var key=UUID.randomUUID();var d=request(compra,item,"1",null);var receipt=service.criar(key,d);
    var forged=new RecebimentoCompraRequestDTO(compra,UUID.randomUUID(),null,d.dataRecebimento(),d.status(),d.observacao(),d.itens());
    assertThrows(org.springframework.security.access.AccessDeniedException.class,()->service.criar(UUID.randomUUID(),forged));
    UUID second=UUID.randomUUID();sql.update("insert into usuario(id,nome,email,cargo,senha_hash,status,criado_em,revisado,alterar_senha) values (?,'Second','second@example.test','ADMIN','test','ATIVO',now(),true,false)",second);
    var p=com.vilaverde.erp.security.AuthPrincipal.of(users.findById(second).orElseThrow(),false);
    org.springframework.security.core.context.SecurityContextHolder.getContext().setAuthentication(org.springframework.security.authentication.UsernamePasswordAuthenticationToken.authenticated(p,null,p.getAuthorities()));
    var replay=new RecebimentoCompraRequestDTO(compra,second,null,d.dataRecebimento(),d.status(),d.observacao(),d.itens());
    assertThrows(ResourceNotFoundException.class,()->service.criar(key,replay));
    var changed=new RecebimentoCompraRequestDTO(compra,second,receipt.versao(),d.dataRecebimento(),d.status(),d.observacao(),d.itens());
    var edited=service.atualizar(receipt.id(),changed);assertEquals(usuario,edited.idUsuario());assertEquals(second,edited.idModificador());
    var confirmed=service.concluir(edited.id(),edited.versao());assertEquals(second,confirmed.idConfirmador());
    assertEquals(second,sql.queryForObject("select id_usuario from movimentacao_estoque limit 1",UUID.class));
  }
  @Test void apiValidationAndErrorShape() throws Exception {
    String base="{\"idCompra\":\""+compra+"\",\"idUsuario\":\""+usuario+"\",\"status\":\"RASCUNHO\",\"itens\":[{\"idItemCompra\":\""+item+"\",\"idProduto\":\""+produto+"\",\"quantidade\":%s}]}";
    for(String q:List.of("0","-1","0.0001","100000000000"))
      mvc.perform(post("/api/recebimentos-compras").header("Idempotency-Key",UUID.randomUUID()).contentType("application/json").content(base.formatted(q))).andExpect(status().isBadRequest()).andExpect(jsonPath("$.status").value(400));
    mvc.perform(post("/api/recebimentos-compras").header("Idempotency-Key",UUID.randomUUID()).contentType("application/json").content(base.formatted("1").replaceFirst("\\[\\{.*\\}\\]", "[null]"))).andExpect(status().isBadRequest());
    mvc.perform(post("/api/recebimentos-compras").contentType("application/json").content(base.formatted("1"))).andExpect(status().isBadRequest());
    mvc.perform(get("/api/recebimentos-compras/"+UUID.randomUUID())).andExpect(status().isNotFound());
    org.springframework.security.core.context.SecurityContextHolder.getContext().setAuthentication(authentication);
    var a=draft("1");
    mvc.perform(delete("/api/recebimentos-compras/"+a.id()).param("versao","999")).andExpect(status().isConflict()).andExpect(jsonPath("$.status").value(409));
  }
}
