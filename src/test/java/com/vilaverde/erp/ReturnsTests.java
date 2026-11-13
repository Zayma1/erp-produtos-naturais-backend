package com.vilaverde.erp;

import com.vilaverde.erp.dto.recebimento.*;
import com.vilaverde.erp.dto.venda.*;
import com.vilaverde.erp.dto.transferencia.*;
import com.vilaverde.erp.dto.devolucao.*;
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
class ReturnsTests {
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


 @Autowired DevolucaoClienteService customers;
 @Autowired DevolucaoFornecedorService suppliers;
 @Test void exportOpenApi() throws Exception {
  var contract=mvc.perform(get("/v3/api-docs")).andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
  var document=new tools.jackson.databind.ObjectMapper().readTree(contract);
  var schemas=document.get("components").get("schemas");
  assertTrue(document.get("paths").get("/api/localidades/estados").get("get").get("responses").has("200"));
  assertTrue(document.get("paths").get("/api/lojas").get("post").get("responses").has("201"));
  assertFalse(document.get("paths").has("/api/auth/redefinir-senha"));
  assertFalse(document.get("paths").has("/api/auth/esqueci-senha"));
  assertFalse(schemas.has("PasswordReset"));
  assertTrue(schemas.get("Reset").get("properties").has("senha"));
  java.nio.file.Files.writeString(java.nio.file.Path.of("openapi-stage14.json"),contract);
 }
 VendaResponseDTO sale(String q,boolean complete) {
  UUID customer=UUID.randomUUID();sql.update("insert into cliente(id,nome,criado_em) values (?,'Cliente',now())",customer);
  var e=sales.criar(UUID.randomUUID(),new VendaRequestDTO(null,null,null,customer,null,loja,null,List.of(new ProdutoVendaRequestDTO(null,produto,new BigDecimal(q),BigDecimal.ONE,BigDecimal.ZERO))));
  return complete?sales.concluir(e.id(),e.versao()):e;
 }
 DevolucaoClienteRequestDTO customer(VendaResponseDTO s,Integer v,ItemDevolucaoClienteRequestDTO... rows) {return new DevolucaoClienteRequestDTO(s.id(),null,null,null,"Defeito","Obs",v,List.of(rows));}
 ItemDevolucaoClienteRequestDTO crow(VendaResponseDTO s,UUID id,String q,boolean stock) {return new ItemDevolucaoClienteRequestDTO(id,s.itens().getFirst().id(),produto,new BigDecimal(q),"Condição",stock);}
 DevolucaoFornecedorRequestDTO supplier(Integer v,ItemDevolucaoFornecedorRequestDTO... rows) {return new DevolucaoFornecedorRequestDTO(compra,null,null,null,"Defeito","Obs",v,List.of(rows));}
 ItemDevolucaoFornecedorRequestDTO srow(UUID id,UUID origin,String q) {return new ItemDevolucaoFornecedorRequestDTO(id,origin,produto,new BigDecimal(q),"Obs");}
 void receive(String q) {var r=service.criar(UUID.randomUUID(),new RecebimentoCompraRequestDTO(compra,usuario,null,null,StatusDocumento.RASCUNHO,null,List.of(new ItemRecebimentoRequestDTO(null,item,produto,new BigDecimal(q),null))));service.concluir(r.id(),r.versao());}
 @Test void mixedPartialCustomerReturnsConsumeAllowanceWithoutChangingSale() {
  stock(produto,loja,"20");var s=sale("10",true);long initial=movements();var body=customer(s,null,crow(s,null,"2.125",true),crow(s,null,"1.875",false));var key=UUID.randomUUID();
  var d=customers.criar(key,body);assertEquals(d.id(),customers.criar(key,body).id());balance(produto,loja,"10");
  var c=customers.concluir(d.id(),d.versao());assertEquals(StatusDocumento.CONCLUIDA,c.status());balance(produto,loja,"12.125");assertEquals(initial+1,movements());assertEquals(usuario,c.idConfirmador());assertEquals("VENDA",c.itens().getFirst().unidadeOrigem());
  customers.concluir(d.id(),d.versao());assertEquals(initial+1,movements());
  var source=sales.buscarPorId(s.id());assertEquals(s.valorTotal(),source.valorTotal());assertEquals(s.status(),source.status());assertEquals(0,new BigDecimal("4").compareTo(source.itens().getFirst().quantidadeDevolvida()));assertEquals(0,new BigDecimal("6").compareTo(source.itens().getFirst().quantidadeDevolvivel()));
  var excessive=customers.criar(UUID.randomUUID(),customer(s,null,crow(s,null,"6.001",false)));assertThrows(ConflictException.class,()->customers.concluir(excessive.id(),0));balance(produto,loja,"12.125");
  var rest=customers.criar(UUID.randomUUID(),customer(s,null,crow(s,null,"6",false)));customers.concluir(rest.id(),0);balance(produto,loja,"12.125");assertEquals(0,sales.buscarPorId(s.id()).itens().getFirst().quantidadeDevolvivel().signum());
 }
 @Test void stableIdentityVersionsCancellationAndValidation() {
  stock(produto,loja,"20");var s=sale("10",true);var d=customers.criar(UUID.randomUUID(),customer(s,null,crow(s,null,"2",true),crow(s,null,"2",false)));var id=d.itens().getFirst().id();
  var edit=customers.atualizar(d.id(),customer(s,d.versao(),crow(s,id,"3",true)));assertEquals(id,edit.itens().getFirst().id());assertEquals(1,edit.itens().size());
  assertThrows(ConflictException.class,()->customers.atualizar(d.id(),customer(s,d.versao(),crow(s,id,"4",true))));
  assertThrows(InvalidInputException.class,()->customers.atualizar(d.id(),customer(s,edit.versao(),crow(s,UUID.randomUUID(),"3",true))));
  assertThrows(InvalidInputException.class,()->customers.criar(UUID.randomUUID(),customer(s,null,crow(s,null,"1.0001",true))));
  assertThrows(InvalidInputException.class,()->customers.criar(UUID.randomUUID(),customer(s,null,crow(s,null,"1",true),crow(s,null,"2",true))));
  var foreign=sale("1",false);assertThrows(InvalidInputException.class,()->customers.criar(UUID.randomUUID(),customer(s,null,crow(foreign,null,"1",true))));
  assertThrows(InvalidInputException.class,()->customers.criar(UUID.randomUUID(),customer(s,null,new ItemDevolucaoClienteRequestDTO(null,s.itens().getFirst().id(),produto,BigDecimal.ONE,"",false))));
  assertThrows(ConflictException.class,()->customers.criar(UUID.randomUUID(),customer(foreign,null,crow(foreign,null,"1",true))));
  var cancelled=customers.cancelar(d.id(),edit.versao(),"Desistência");assertEquals(StatusDocumento.CANCELADA,cancelled.status());assertThrows(ConflictException.class,()->customers.concluir(d.id(),cancelled.versao()));balance(produto,loja,"10");
 }
 @Test void supplierReturnsRequireReceivedAllowanceAndStockPreserveReceipt() {
  receive("8");var before=compras.buscar(compra);var d=suppliers.criar(UUID.randomUUID(),supplier(null,srow(null,item,"3")));var id=d.itens().getFirst().id();d=suppliers.atualizar(d.id(),supplier(d.versao(),srow(id,item,"4")));assertEquals(id,d.itens().getFirst().id());
  var done=suppliers.concluir(d.id(),d.versao());balance(produto,loja,"4");suppliers.concluir(d.id(),0);balance(produto,loja,"4");assertEquals("COMPRA",done.itens().getFirst().unidadeOrigem());
  var after=compras.buscar(compra);assertEquals(before.status(),after.status());assertEquals(before.valorTotal(),after.valorTotal());assertEquals(before.itens().getFirst().quantidadeRecebida(),after.itens().getFirst().quantidadeRecebida());assertEquals(0,new BigDecimal("4").compareTo(after.itens().getFirst().quantidadeDevolvivel()));
  var over=suppliers.criar(UUID.randomUUID(),supplier(null,srow(null,item,"4.001")));assertThrows(ConflictException.class,()->suppliers.concluir(over.id(),0));
  stock(produto,loja,"1");var shortStock=suppliers.criar(UUID.randomUUID(),supplier(null,srow(null,item,"2")));long movements=movements();assertThrows(ConflictException.class,()->suppliers.concluir(shortStock.id(),0));assertEquals(movements,movements());assertEquals(StatusDocumento.RASCUNHO,suppliers.buscar(shortStock.id()).status());balance(produto,loja,"1");
 }
 List<Object> parallel(Callable<?> a,Callable<?> b) throws Exception {
  var pool=Executors.newFixedThreadPool(2);var start=new CountDownLatch(1);
  try {var tasks=List.of(a,b).stream().map(task->pool.submit(()->{org.springframework.security.core.context.SecurityContextHolder.getContext().setAuthentication(authentication);start.await();try{return task.call();}catch(ConflictException e){return e;}finally{org.springframework.security.core.context.SecurityContextHolder.clearContext();}})).toList();start.countDown();return List.of(tasks.get(0).get(20,TimeUnit.SECONDS),tasks.get(1).get(20,TimeUnit.SECONDS));} finally {pool.shutdownNow();}
 }
 @Test void competingReturnsReplayAndStockWritersSerialize() throws Exception {
  stock(produto,loja,"20");var s=sale("10",true);var a=customers.criar(UUID.randomUUID(),customer(s,null,crow(s,null,"6",true)));var b=customers.criar(UUID.randomUUID(),customer(s,null,crow(s,null,"6",false)));var results=parallel(()->customers.concluir(a.id(),0),()->customers.concluir(b.id(),0));assertEquals(1,results.stream().filter(ConflictException.class::isInstance).count());
  receive("8");var d=suppliers.criar(UUID.randomUUID(),supplier(null,srow(null,item,"3")));var n=movements();parallel(()->suppliers.concluir(d.id(),0),()->suppliers.concluir(d.id(),0));assertEquals(n+1,movements());
  stock(produto,loja,"4");var e=suppliers.criar(UUID.randomUUID(),supplier(null,srow(null,item,"3")));var sale=sale("3",false);results=parallel(()->suppliers.concluir(e.id(),0),()->sales.concluir(sale.id(),0));assertEquals(1,results.stream().filter(ConflictException.class::isInstance).count());balance(produto,loja,"1");
  receive("2");stock(produto,loja,"4");var f=suppliers.criar(UUID.randomUUID(),supplier(null,srow(null,item,"3")));var t=draft(loja,destino,"3");results=parallel(()->suppliers.concluir(f.id(),0),()->transfers.concluir(t.id(),0));assertEquals(1,results.stream().filter(ConflictException.class::isInstance).count());balance(produto,loja,"1");
 }
 @Test void customerReturnAndReceivingHaveNoLostUpdateAndOverflowRollsBack() throws Exception {
  stock(produto,loja,"10");var s=sale("5",true);var d=customers.criar(UUID.randomUUID(),customer(s,null,crow(s,null,"2",true)));var receipt=service.criar(UUID.randomUUID(),new RecebimentoCompraRequestDTO(compra,usuario,null,null,StatusDocumento.RASCUNHO,null,List.of(new ItemRecebimentoRequestDTO(null,item,produto,new BigDecimal("4"),null))));parallel(()->customers.concluir(d.id(),0),()->service.concluir(receipt.id(),0));balance(produto,loja,"11");
  stock(produto,loja,"99999999999.999");var overflow=customers.criar(UUID.randomUUID(),customer(s,null,crow(s,null,"1",true)));long n=movements();assertThrows(ConflictException.class,()->customers.concluir(overflow.id(),0));assertEquals(n,movements());assertNull(customers.buscar(overflow.id()).concluidoEm());
 }
 @Test void accessScopeActorIdempotencyAndLegacyGuards() throws Exception {
  receive("8");var key=UUID.randomUUID();var body=supplier(null,srow(null,item,"2"));var d=suppliers.criar(key,body);
  sql.update("update usuario set cargo='GER' where id=?",usuario);sql.update("insert into usuario_loja(id_usuario,id_loja) values (?,?)",usuario,destino);
  var p=com.vilaverde.erp.security.AuthPrincipal.of(users.findById(usuario).orElseThrow(),false);
  org.springframework.security.core.context.SecurityContextHolder.getContext().setAuthentication(org.springframework.security.authentication.UsernamePasswordAuthenticationToken.authenticated(p,null,p.getAuthorities()));
  assertThrows(ResourceNotFoundException.class,()->suppliers.buscar(d.id()));assertTrue(suppliers.listar(null).isEmpty());assertThrows(ResourceNotFoundException.class,()->suppliers.criar(key,body));
  org.springframework.security.core.context.SecurityContextHolder.getContext().setAuthentication(authentication);
  sql.update("update devolucao_fornecedor set legado=true where id=?",d.id());assertThrows(ConflictException.class,()->suppliers.concluir(d.id(),0));assertThrows(ConflictException.class,()->suppliers.criar(UUID.randomUUID(),body));assertTrue(compras.buscar(compra).itens().getFirst().devolucaoLegadaPendente());assertNull(compras.buscar(compra).itens().getFirst().quantidadeDevolvivel());
  mvc.perform(delete("/api/devolucoes-fornecedores/"+d.id())).andExpect(status().isForbidden());
 }

 @Test void aggregateProductsAcrossPurchaseItemsAndRollbackEntireDocument() {
  var second=purchaseItem(compra,produto,"10");
  var receipt=service.criar(UUID.randomUUID(),new RecebimentoCompraRequestDTO(compra,usuario,null,null,StatusDocumento.RASCUNHO,null,List.of(new ItemRecebimentoRequestDTO(null,item,produto,new BigDecimal("5"),null),new ItemRecebimentoRequestDTO(null,second,produto,new BigDecimal("5"),null))));service.concluir(receipt.id(),0);
  stock(produto,loja,"5");var d=suppliers.criar(UUID.randomUUID(),supplier(null,srow(null,item,"3"),srow(null,second,"3")));long n=movements();assertThrows(ConflictException.class,()->suppliers.concluir(d.id(),0));balance(produto,loja,"5");assertEquals(n,movements());assertNull(suppliers.buscar(d.id()).idConfirmador());
  stock(produto,loja,"6");suppliers.concluir(d.id(),0);balance(produto,loja,"0");assertEquals(n+2,movements());
  assertThrows(InvalidInputException.class,()->suppliers.criar(UUID.randomUUID(),supplier(null,srow(null,item,"1"),srow(null,item,"1"))));
  var other=purchaseItem(UUID.fromString(sql.queryForObject("select id from compra limit 1",String.class)),produto,"1");
  var fake=new ItemDevolucaoFornecedorRequestDTO(null,other,UUID.randomUUID(),BigDecimal.ONE,null);assertThrows(InvalidInputException.class,()->suppliers.criar(UUID.randomUUID(),supplier(null,fake)));
 }
 @Test void lateMovementFailureRollsBackStockStatusAllowanceAndAuditTogether() {
  stock(produto,loja,"10");var s=sale("5",true);var d=customers.criar(UUID.randomUUID(),customer(s,null,crow(s,null,"1",true)));long n=movements();
  sql.execute("create function fail_return() returns trigger language plpgsql as $$ begin if NEW.id_devolucao_cliente is not null then raise exception 'forced return rollback'; end if; return NEW; end $$");sql.execute("create trigger fail_return before insert on movimentacao_estoque for each row execute function fail_return()");
  try {assertThrows(org.springframework.dao.DataAccessException.class,()->customers.concluir(d.id(),0));}finally{sql.execute("drop trigger fail_return on movimentacao_estoque");sql.execute("drop function fail_return()");}
  balance(produto,loja,"5");assertEquals(n,movements());var after=customers.buscar(d.id());assertEquals(StatusDocumento.RASCUNHO,after.status());assertEquals(d.versao(),after.versao());assertNull(after.idConfirmador());assertNull(after.concluidoEm());assertEquals(0,sales.buscarPorId(s.id()).itens().getFirst().quantidadeDevolvida().signum());
 }
 @Test void repeatedCreationIsActorOwnedAndLifecycleInputIsRejected() throws Exception {
  receive("8");var key=UUID.randomUUID();var body=supplier(null,srow(null,item,"2"));var results=parallel(()->suppliers.criar(key,body),()->suppliers.criar(key,body));assertEquals(((DevolucaoFornecedorResponseDTO)results.get(0)).id(),((DevolucaoFornecedorResponseDTO)results.get(1)).id());
  assertThrows(ConflictException.class,()->suppliers.criar(key,supplier(null,srow(null,item,"1"))));
  assertThrows(InvalidInputException.class,()->suppliers.criar(UUID.randomUUID(),new DevolucaoFornecedorRequestDTO(compra,null,null,StatusDocumento.CONCLUIDA,"X",null,null,body.itens())));
  assertThrows(InvalidInputException.class,()->suppliers.criar(UUID.randomUUID(),supplier(null,srow(null,item,"100000000000"))));
  UUID other=UUID.randomUUID();sql.update("insert into usuario(id,nome,email,usuario,cargo,senha_hash,status,criado_em,revisado,alterar_senha) values (?,'Outro','outro@test.local','outro','ADMIN','test','ATIVO',now(),true,false)",other);
  var principal=com.vilaverde.erp.security.AuthPrincipal.of(users.findById(other).orElseThrow(),false);org.springframework.security.core.context.SecurityContextHolder.getContext().setAuthentication(org.springframework.security.authentication.UsernamePasswordAuthenticationToken.authenticated(principal,null,principal.getAuthorities()));assertThrows(ResourceNotFoundException.class,()->suppliers.criar(key,body));
  sql.update("update usuario set cargo='FUNC' where id=?",other);principal=com.vilaverde.erp.security.AuthPrincipal.of(users.findById(other).orElseThrow(),false);org.springframework.security.core.context.SecurityContextHolder.getContext().setAuthentication(org.springframework.security.authentication.UsernamePasswordAuthenticationToken.authenticated(principal,null,principal.getAuthorities()));assertThrows(org.springframework.security.access.AccessDeniedException.class,()->suppliers.listar(null));assertThrows(org.springframework.security.access.AccessDeniedException.class,()->customers.listar(null));
 }

 @Test void competingSupplierDocumentsCannotExceedReceivedAllowance() throws Exception {
  receive("8");stock(produto,loja,"20");var a=suppliers.criar(UUID.randomUUID(),supplier(null,srow(null,item,"6")));var b=suppliers.criar(UUID.randomUUID(),supplier(null,srow(null,item,"6")));long n=movements();
  var results=parallel(()->suppliers.concluir(a.id(),0),()->suppliers.concluir(b.id(),0));assertEquals(1,results.stream().filter(ConflictException.class::isInstance).count());balance(produto,loja,"14");assertEquals(n+1,movements());assertEquals(0,new BigDecimal("2").compareTo(compras.buscar(compra).itens().getFirst().quantidadeDevolvivel()));
  var c=suppliers.criar(UUID.randomUUID(),supplier(null,srow(null,item,"1.125")));suppliers.concluir(c.id(),0);balance(produto,loja,"12.875");assertEquals(0,new BigDecimal("0.875").compareTo(compras.buscar(compra).itens().getFirst().quantidadeDevolvivel()));
 }
 @Test void oneInsufficientProductLeavesEverySupplierItemAndAuditUnchanged() {
  UUID second=UUID.randomUUID();SkuFixtures.cloneSku(sql,second,produto,"Segundo");UUID secondItem=purchaseItem(compra,second,"10");
  var receipt=service.criar(UUID.randomUUID(),new RecebimentoCompraRequestDTO(compra,usuario,null,null,StatusDocumento.RASCUNHO,null,List.of(new ItemRecebimentoRequestDTO(null,item,produto,new BigDecimal("5"),null),new ItemRecebimentoRequestDTO(null,secondItem,second,new BigDecimal("5"),null))));service.concluir(receipt.id(),0);stock(second,loja,"0");
  var d=suppliers.criar(UUID.randomUUID(),supplier(null,srow(null,item,"1"),new ItemDevolucaoFornecedorRequestDTO(null,secondItem,second,BigDecimal.ONE,null)));long n=movements();assertThrows(ConflictException.class,()->suppliers.concluir(d.id(),0));balance(produto,loja,"5");balance(second,loja,"0");assertEquals(n,movements());var after=suppliers.buscar(d.id());assertEquals(d.versao(),after.versao());assertEquals(StatusDocumento.RASCUNHO,after.status());assertNull(after.concluidoEm());assertNull(after.idConfirmador());assertTrue(compras.buscar(compra).itens().stream().allMatch(i->i.quantidadeDevolvida().signum()==0));
 }

 @Autowired ProdutoSkuService skus;
 @Autowired EstoqueService stocks;
 @Autowired MovimentacaoEstoqueService history;
 UUID variant(String code,String type) {
  UUID id=UUID.randomUUID();
  sql.update("insert into produto_sku(id,id_produto,id_unidade_medida,tipo_venda,status,sku,descricao_apresentacao,criado_em) select ?,id_produto,id_unidade_medida,?,'ATIVO',?,?,now() from produto_sku where id=?",id,type,code,"Pacote "+code,produto);
  return id;
 }
 VendaResponseDTO saleSku(UUID sku,String q) {
  UUID customer=UUID.randomUUID();sql.update("insert into cliente(id,nome,criado_em) values (?,'Cliente SKU',now())",customer);
  return sales.criar(UUID.randomUUID(),new VendaRequestDTO(null,null,null,customer,null,loja,null,List.of(new ProdutoVendaRequestDTO(null,sku,new BigDecimal(q),BigDecimal.ONE,BigDecimal.ZERO))));
 }
 @Test void stage13VariantsHaveIndependentBalancesPricesAndCombinedFilters() {
  UUID a=variant("200g","UNIDADE"),b=variant("500g","UNIDADE");
  stock(a,loja,"2");stock(b,loja,"100");stock(produto,loja,"3.125");
  UUID parent=skus.buscarPorId(a).idProduto();assertEquals(parent,skus.buscarPorId(b).idProduto());
   // Price independence is persisted by SKU/store, with manual document prices unchanged.
  sql.update("insert into produto_preco_loja(id,id_produto_sku,id_loja,preco,data_inicio,criado_em) values (?,?,?,5,now(),now()),(?,?,?,8,now(),now())",UUID.randomUUID(),a,loja,UUID.randomUUID(),b,loja);
  assertEquals(2,sql.queryForObject("select count(distinct preco) from produto_preco_loja",Integer.class));
  assertEquals(1,stocks.listar(parent,a,loja).size());assertEquals(0,stocks.listar(UUID.randomUUID(),a,loja).size());
  var sale=saleSku(a,"3");assertThrows(ConflictException.class,()->sales.concluir(sale.id(),sale.versao()));balance(a,loja,"2");balance(b,loja,"100");
  var ok=saleSku(a,"1");sales.concluir(ok.id(),ok.versao());balance(a,loja,"1");balance(b,loja,"100");balance(produto,loja,"3.125");
  assertEquals(1,history.listar(parent,a,loja).size());assertEquals(0,history.listar(parent,b,loja).size());
  assertThrows(InvalidInputException.class,()->saleSku(a,"0.125"));
  var bulk=saleSku(produto,"0.125");sales.concluir(bulk.id(),bulk.versao());balance(produto,loja,"3");
 }
 @Test void stage13PurchaseReceiptSaleReturnKeepSkuAndSnapshots() {
  UUID a=variant("200g","UNIDADE"),b=variant("500g","UNIDADE");
  UUID supplier=sql.queryForObject("select id from fornecedor limit 1",UUID.class);
  var buy=compras.criar(new com.vilaverde.erp.dto.compra.CompraRequestDTO(supplier,loja,null,null,null,null,List.of(new com.vilaverde.erp.dto.compra.ItemCompraRequestDTO(null,a,new BigDecimal("10"),BigDecimal.ONE,BigDecimal.ZERO))));
  var original=buy.itens().getFirst();
  var receipt=service.criar(UUID.randomUUID(),new RecebimentoCompraRequestDTO(buy.id(),null,null,null,StatusDocumento.RASCUNHO,null,List.of(new ItemRecebimentoRequestDTO(null,original.id(),a,new BigDecimal("10"),null))));
  assertThrows(InvalidInputException.class,()->service.criar(UUID.randomUUID(),new RecebimentoCompraRequestDTO(buy.id(),null,null,null,StatusDocumento.RASCUNHO,null,List.of(new ItemRecebimentoRequestDTO(null,original.id(),b,BigDecimal.ONE,null)))));
  service.concluir(receipt.id(),receipt.versao());balance(a,loja,"10");balance(b,loja,"0");
  var sale=saleSku(a,"2");sales.concluir(sale.id(),sale.versao());balance(a,loja,"8");
  var request=new DevolucaoClienteRequestDTO(sale.id(),null,null,null,"Retorno SKU",null,null,List.of(new ItemDevolucaoClienteRequestDTO(null,sale.itens().getFirst().id(),a,BigDecimal.ONE,"Obs",true)));
  var ret=customers.criar(UUID.randomUUID(),request);customers.concluir(ret.id(),ret.versao());balance(a,loja,"9");balance(b,loja,"0");
  var saved=skus.buscarPorId(a);
  skus.atualizar(a,new com.vilaverde.erp.dto.produtosku.ProdutoSkuRequestDTO(saved.idProduto(),saved.status(),"NEW-CODE",null,null,saved.idUnidadeMedida(),saved.tipoVenda(),"Nova apresentação"));
  assertEquals("200g",sales.buscarPorId(sale.id()).itens().getFirst().skuCodigoSnapshot());assertEquals("Pacote 200g",customers.buscar(ret.id()).itens().getFirst().skuApresentacaoSnapshot());
  assertThrows(org.springframework.dao.DataIntegrityViolationException.class,()->skus.atualizar(a,new com.vilaverde.erp.dto.produtosku.ProdutoSkuRequestDTO(saved.idProduto(),saved.status(),"NEW-CODE",null,null,saved.idUnidadeMedida(),com.vilaverde.erp.entity.enums.TipoVenda.PESO,"Nova apresentação")));
  UUID anotherUnit=UUID.randomUUID();sql.update("insert into unidade_medida(id,nome,sigla) values (?,'Outra','OUT')",anotherUnit);
  assertThrows(org.springframework.dao.DataIntegrityViolationException.class,()->skus.atualizar(a,new com.vilaverde.erp.dto.produtosku.ProdutoSkuRequestDTO(saved.idProduto(),saved.status(),"NEW-CODE",null,null,anotherUnit,saved.tipoVenda(),"Nova apresentação")));
  assertThrows(org.springframework.dao.DataIntegrityViolationException.class,()->skus.excluir(a));
 }
 @Test void stage13UnitFractionsRejectedInAllInheritedEndpoints() {
  UUID a=variant("Unidade","UNIDADE");UUID supplier=sql.queryForObject("select id from fornecedor limit 1",UUID.class);UUID purchase=purchase(supplier),origin=purchaseItem(purchase,a,"10");
  assertThrows(InvalidInputException.class,()->service.criar(UUID.randomUUID(),new RecebimentoCompraRequestDTO(purchase,null,null,null,StatusDocumento.RASCUNHO,null,List.of(new ItemRecebimentoRequestDTO(null,origin,a,new BigDecimal("0.5"),null)))));
  stock(a,loja,"5");var sale=saleSku(a,"2");sales.concluir(sale.id(),sale.versao());
  var request=new DevolucaoClienteRequestDTO(sale.id(),null,null,null,"Retorno",null,null,List.of(new ItemDevolucaoClienteRequestDTO(null,sale.itens().getFirst().id(),a,new BigDecimal("0.5"),"Obs",true)));
  assertThrows(InvalidInputException.class,()->customers.criar(UUID.randomUUID(),request));
 }

 @Test void stage13ConcurrentVariantSalesAndMultiVariantRollback() throws Exception {
  UUID a=variant("200g","UNIDADE"),b=variant("500g","UNIDADE");stock(a,loja,"2");stock(b,loja,"2");
  var x=saleSku(a,"2");var y=saleSku(b,"2");parallel(()->sales.concluir(x.id(),0),()->sales.concluir(y.id(),0));balance(a,loja,"0");balance(b,loja,"0");
  stock(a,loja,"2");stock(b,loja,"1");
  UUID customer=x.idCliente();var draft=sales.criar(UUID.randomUUID(),new VendaRequestDTO(null,null,null,customer,null,loja,null,List.of(new ProdutoVendaRequestDTO(null,a,new BigDecimal("2"),BigDecimal.ONE,BigDecimal.ZERO),new ProdutoVendaRequestDTO(null,b,new BigDecimal("2"),BigDecimal.ONE,BigDecimal.ZERO))));
  long count=movements();assertThrows(ConflictException.class,()->sales.concluir(draft.id(),0));balance(a,loja,"2");balance(b,loja,"1");assertEquals(count,movements());
 }
 @Test void stage13InactiveSkuKeepsExistingDocumentsAndReturnsAccessible() {
  UUID a=variant("200g","UNIDADE");stock(a,loja,"5");var s=saleSku(a,"2");sales.concluir(s.id(),0);
  var saved=skus.buscarPorId(a);skus.atualizar(a,new com.vilaverde.erp.dto.produtosku.ProdutoSkuRequestDTO(saved.idProduto(),com.vilaverde.erp.entity.enums.StatusProduto.INATIVO,saved.sku(),null,null,saved.idUnidadeMedida(),saved.tipoVenda(),saved.descricaoApresentacao()));
  assertEquals(a,sales.buscarPorId(s.id()).itens().getFirst().idProdutoSku());assertThrows(InvalidInputException.class,()->saleSku(a,"1"));
  var req=new DevolucaoClienteRequestDTO(s.id(),null,null,null,"Retorno inativo",null,null,List.of(new ItemDevolucaoClienteRequestDTO(null,s.itens().getFirst().id(),a,BigDecimal.ONE,"Obs",true)));
  var e=customers.criar(UUID.randomUUID(),req);customers.concluir(e.id(),0);balance(a,loja,"4");
 }
}
