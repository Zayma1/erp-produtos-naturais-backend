package com.vilaverde.erp;

import com.vilaverde.erp.dto.recebimento.*;
import com.vilaverde.erp.dto.venda.*;
import com.vilaverde.erp.dto.transferencia.*;
import com.vilaverde.erp.dto.ajuste.*;
import com.vilaverde.erp.entity.enums.TipoMovimentacao;
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
class AdjustmentTests {
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

 @Test void exportOpenApi() throws Exception {var json=mvc.perform(get("/v3/api-docs")).andExpect(status().isOk()).andReturn().getResponse().getContentAsString();java.nio.file.Files.writeString(java.nio.file.Path.of("target/openapi-adjustments-test.json"),json);}
 @Autowired AjusteEstoqueService adjustments;
 AjusteRequestDTO adjust(Integer v,String q,TipoMovimentacao type) {return new AjusteRequestDTO(loja,null,"Correção",null,v,List.of(new ItemAjusteRequestDTO(null,produto,type,new BigDecimal(q),"Obs")));}
 @Test void entryExitReplayCancellationAndImmutable() {
   var key=UUID.randomUUID();var request=adjust(null,"3.125",TipoMovimentacao.AJUSTE_ENTRADA);
   var a=adjustments.criar(key,request);assertEquals(a.id(),adjustments.criar(key,request).id());balance(produto,loja,"0");assertEquals(0,movements());
   var done=adjustments.concluir(a.id(),a.versao());balance(produto,loja,"3.125");assertEquals(1,movements());assertEquals(usuario,done.idConfirmador());assertEquals("UN",done.itens().getFirst().unidadeSigla());assertEquals(0,done.itens().getFirst().saldoAntes().signum());
   adjustments.concluir(a.id(),a.versao());assertEquals(1,movements());assertThrows(ConflictException.class,()->adjustments.atualizar(a.id(),adjust(done.versao(),"2",TipoMovimentacao.AJUSTE_ENTRADA)));
   var out=adjustments.criar(UUID.randomUUID(),adjust(null,"1.125",TipoMovimentacao.AJUSTE_SAIDA));adjustments.concluir(out.id(),out.versao());balance(produto,loja,"2");
   var cancel=adjustments.criar(UUID.randomUUID(),request);adjustments.cancelar(cancel.id(),cancel.versao(),"Erro");adjustments.cancelar(cancel.id(),cancel.versao(),"Erro");balance(produto,loja,"2");assertEquals(2,movements());
 }
 @Test void shortageOverflowPrecisionTypesVersions() {
   var a=adjustments.criar(UUID.randomUUID(),adjust(null,"1",TipoMovimentacao.AJUSTE_SAIDA));assertThrows(ConflictException.class,()->adjustments.concluir(a.id(),a.versao()));balance(produto,loja,"0");assertEquals(0,movements());
   assertEquals(0,sql.queryForObject("select count(*) from estoque_produto",Integer.class));
   assertThrows(InvalidInputException.class,()->adjustments.criar(UUID.randomUUID(),adjust(null,"0.0001",TipoMovimentacao.AJUSTE_ENTRADA)));
   assertThrows(InvalidInputException.class,()->adjustments.criar(UUID.randomUUID(),adjust(null,"1",TipoMovimentacao.TRANSFERENCIA_ENTRADA)));
   assertThrows(ConflictException.class,()->adjustments.atualizar(a.id(),adjust(99,"1",TipoMovimentacao.AJUSTE_ENTRADA)));
   stock(produto,loja,"99999999999.999");var entry=adjustments.criar(UUID.randomUUID(),adjust(null,"0.001",TipoMovimentacao.AJUSTE_ENTRADA));assertThrows(ConflictException.class,()->adjustments.concluir(entry.id(),entry.versao()));balance(produto,loja,"99999999999.999");assertEquals(0,movements());
 }

 @Test void mixedRollbackIdentityValidationAndScope() {
   UUID other=UUID.randomUUID();SkuFixtures.cloneSku(sql,other,produto,"Outro");
   var rows=List.of(new ItemAjusteRequestDTO(null,produto,TipoMovimentacao.AJUSTE_ENTRADA,new BigDecimal("5"),null),new ItemAjusteRequestDTO(null,other,TipoMovimentacao.AJUSTE_SAIDA,BigDecimal.ONE,null));
   var a=adjustments.criar(UUID.randomUUID(),new AjusteRequestDTO(loja,null,"Misto",null,null,rows));
   assertThrows(ConflictException.class,()->adjustments.concluir(a.id(),a.versao()));balance(produto,loja,"0");assertEquals(0,movements());assertEquals(0,sql.queryForObject("select count(*) from estoque_produto",Integer.class));
   var i=a.itens().getFirst();var edit=adjustments.atualizar(a.id(),new AjusteRequestDTO(loja,null,"Revisado",null,a.versao(),List.of(new ItemAjusteRequestDTO(i.id(),produto,TipoMovimentacao.AJUSTE_ENTRADA,BigDecimal.ONE,null))));assertEquals(i.id(),edit.itens().getFirst().id());assertEquals(1,edit.itens().size());assertTrue(edit.versao()>a.versao());assertEquals(usuario,edit.idModificador());
   assertThrows(InvalidInputException.class,()->adjustments.criar(UUID.randomUUID(),new AjusteRequestDTO(loja,null," ",null,null,rows)));
   assertThrows(InvalidInputException.class,()->adjustments.criar(UUID.randomUUID(),new AjusteRequestDTO(loja,null,"Duplicado",null,null,List.of(rows.getFirst(),rows.getFirst()))));
   assertThrows(InvalidInputException.class,()->adjustments.atualizar(a.id(),new AjusteRequestDTO(loja,null,"ID",null,edit.versao(),List.of(new ItemAjusteRequestDTO(UUID.randomUUID(),produto,TipoMovimentacao.AJUSTE_ENTRADA,BigDecimal.ONE,null)))));
   sql.update("update usuario set cargo='GER' where id=?",usuario);
   var manager=com.vilaverde.erp.security.AuthPrincipal.of(users.findById(usuario).orElseThrow(),false);
   org.springframework.security.core.context.SecurityContextHolder.getContext().setAuthentication(org.springframework.security.authentication.UsernamePasswordAuthenticationToken.authenticated(manager,null,manager.getAuthorities()));
   assertThrows(ResourceNotFoundException.class,()->adjustments.buscar(a.id()));assertTrue(adjustments.listar(null,null).isEmpty());
   sql.update("update usuario set cargo='FUNC' where id=?",usuario);var employee=com.vilaverde.erp.security.AuthPrincipal.of(users.findById(usuario).orElseThrow(),false);org.springframework.security.core.context.SecurityContextHolder.getContext().setAuthentication(org.springframework.security.authentication.UsernamePasswordAuthenticationToken.authenticated(employee,null,employee.getAuthorities()));assertThrows(org.springframework.security.access.AccessDeniedException.class,()->adjustments.buscar(a.id()));
 }
  List<Object> parallel(Callable<?> a,Callable<?> b) throws Exception {
    var start=new CountDownLatch(1); var ready=new CountDownLatch(2);
    try(var pool=Executors.newFixedThreadPool(2)) {
      var futures=List.of(a,b).stream().map(task->pool.submit(()->{org.springframework.security.core.context.SecurityContextHolder.getContext().setAuthentication(authentication); ready.countDown(); start.await(); try {return task.call();} catch (ConflictException e) {return e;} })).toList();
      assertTrue(ready.await(5,TimeUnit.SECONDS)); start.countDown();
      List<Object> result=new ArrayList<>(); for(var f:futures) result.add(f.get(20,TimeUnit.SECONDS)); return result;
    }
  }
 @Test void concurrentWritersAndRepeatedConfirmation() throws Exception {
   stock(produto,loja,"10");var a=adjustments.criar(UUID.randomUUID(),adjust(null,"1",TipoMovimentacao.AJUSTE_SAIDA));
   parallel(()->adjustments.concluir(a.id(),0),()->adjustments.concluir(a.id(),0));balance(produto,loja,"9");assertEquals(1,movements());
   var b=adjustments.criar(UUID.randomUUID(),adjust(null,"6",TipoMovimentacao.AJUSTE_SAIDA));var c=adjustments.criar(UUID.randomUUID(),adjust(null,"6",TipoMovimentacao.AJUSTE_SAIDA));var results=parallel(()->adjustments.concluir(b.id(),0),()->adjustments.concluir(c.id(),0));assertEquals(1,results.stream().filter(ConflictException.class::isInstance).count());balance(produto,loja,"3");
   var receipt=service.criar(UUID.randomUUID(),new RecebimentoCompraRequestDTO(compra,null,null,null,StatusDocumento.RASCUNHO,null,List.of(new ItemRecebimentoRequestDTO(null,item,produto,new BigDecimal("4"),null))));
   var d=adjustments.criar(UUID.randomUUID(),adjust(null,"1",TipoMovimentacao.AJUSTE_ENTRADA));parallel(()->adjustments.concluir(d.id(),0),()->service.concluir(receipt.id(),0));balance(produto,loja,"8");
   var transfer=draft(loja,destino,"2");var e=adjustments.criar(UUID.randomUUID(),adjust(null,"1",TipoMovimentacao.AJUSTE_SAIDA));parallel(()->transfers.concluir(transfer.id(),0),()->adjustments.concluir(e.id(),0));balance(produto,loja,"5");balance(produto,destino,"2");
   UUID customer=UUID.randomUUID();sql.update("insert into cliente(id,nome,criado_em) values (?,'C',now())",customer);
   var sale=sales.criar(UUID.randomUUID(),new VendaRequestDTO(null,null,null,customer,null,loja,null,List.of(new ProdutoVendaRequestDTO(null,produto,new BigDecimal("4"),BigDecimal.ONE,BigDecimal.ZERO))));
   var f=adjustments.criar(UUID.randomUUID(),adjust(null,"4",TipoMovimentacao.AJUSTE_SAIDA));results=parallel(()->adjustments.concluir(f.id(),0),()->sales.concluir(sale.id(),0));assertEquals(1,results.stream().filter(ConflictException.class::isInstance).count());balance(produto,loja,"1");
 }

 @Test void concurrentCreationIsStableAndFailureRollsBackAudit() throws Exception {
   var key=UUID.randomUUID();var body=adjust(null,"2",TipoMovimentacao.AJUSTE_ENTRADA);var results=parallel(()->adjustments.criar(key,body),()->adjustments.criar(key,body));assertEquals(((AjusteResponseDTO)results.get(0)).id(),((AjusteResponseDTO)results.get(1)).id());
   var a=(AjusteResponseDTO)results.getFirst();
   sql.execute("create function fail_adjustment() returns trigger language plpgsql as $$ begin raise exception 'forced adjustment rollback'; end $$");sql.execute("create trigger fail_adjustment before insert on movimentacao_estoque for each row execute function fail_adjustment()");
   try {assertThrows(Exception.class,()->adjustments.concluir(a.id(),a.versao()));balance(produto,loja,"0");assertEquals(0,movements());var saved=adjustments.buscar(a.id());assertEquals(a.versao(),saved.versao());assertNull(saved.idConfirmador());assertNull(saved.concluidoEm());assertNull(saved.itens().getFirst().saldoAntes());assertEquals(0,sql.queryForObject("select count(*) from estoque_produto",Integer.class));}
   finally {sql.execute("drop trigger fail_adjustment on movimentacao_estoque");sql.execute("drop function fail_adjustment()");}
 }
}
