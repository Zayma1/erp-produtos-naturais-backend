package com.vilaverde.erp;

import com.vilaverde.erp.dto.recebimento.*;
import com.vilaverde.erp.dto.venda.*;
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
class SalesTests {
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
  @Autowired VendaService sales;
  UUID customer;
  @BeforeEach void customer() { customer=UUID.randomUUID();sql.update("insert into cliente(id,nome,criado_em) values (?,'Customer',now())",customer); }
  ProdutoVendaRequestDTO row(UUID id,UUID product,String q,String price,String discount) {return new ProdutoVendaRequestDTO(id,product,new BigDecimal(q),new BigDecimal(price),new BigDecimal(discount));}
  VendaRequestDTO request(Integer version,List<ProdutoVendaRequestDTO> rows) {return new VendaRequestDTO(version,null,null,customer,null,loja,"Observação",rows);}
  VendaResponseDTO sale(String q) {return sales.criar(UUID.randomUUID(),request(null,List.of(row(null,produto,q,"5","0"))));}
  void stock(String q) {sql.update("insert into estoque_produto(id,id_produto,id_loja,quantidade_atual,atualizado_em) values (?,?,?,?,now())",UUID.randomUUID(),produto,loja,new BigDecimal(q));}
  BigDecimal balance() {return sql.queryForObject("select coalesce(sum(quantidade_atual),0) from estoque_produto",BigDecimal.class);}
  long movements() {return sql.queryForObject("select count(*) from movimentacao_estoque",Long.class);}
  void amount(String n,BigDecimal v) {assertEquals(0,new BigDecimal(n).compareTo(v));}
  @Test void lifecycleIdentitySnapshotsRoundingAndVersions() {
    stock("5");var a=sale("1");var id=a.itens().getFirst().id();
    var b=sales.atualizar(a.id(),request(a.versao(),List.of(row(id,produto,"2","5","1"))));
    assertEquals(id,b.itens().getFirst().id());assertEquals("Observação",b.observacao());assertTrue(b.versao()>a.versao());
    assertThrows(ConflictException.class,()->sales.atualizar(a.id(),request(a.versao(),List.of(row(id,produto,"3","5","0")))));
    sql.update("update unidade_medida set sigla='NEW'");
    var done=sales.concluir(b.id(),b.versao());amount("3",balance());assertEquals(1,movements());
    amount("9",done.valorTotal());amount("9",done.itens().getFirst().totalItem());assertEquals("UN",done.itens().getFirst().unidadeSiglaSnapshot());
    assertEquals(usuario,done.idConfirmador());assertEquals(usuario,done.idUsuario());assertNotNull(done.concluidoEm());
    sales.concluir(b.id(),b.versao());amount("3",balance());assertEquals(1,movements());
    assertThrows(ConflictException.class,()->sales.cancelar(done.id(),done.versao(),"No"));
    assertThrows(ConflictException.class,()->sales.atualizar(done.id(),request(done.versao(),List.of(row(id,produto,"1","1","0")))));
    var round=sales.criar(UUID.randomUUID(),request(null,List.of(row(null,produto,"0.001","5","0"),row(null,produto,"0.001","5","0"))));
    amount("0.01",round.subtotal());amount("0.01",round.itens().getFirst().totalItem());
  }
  @Test void cancellationAndShortageNeverChangeStock() {
    var a=sale("2");assertThrows(ConflictException.class,()->sales.concluir(a.id(),a.versao()));amount("0",balance());
    assertEquals("ABERTA",sales.buscarPorId(a.id()).status().name());
    assertThrows(InvalidInputException.class,()->sales.cancelar(a.id(),a.versao()," "));
    var b=sales.cancelar(a.id(),a.versao(),"Cliente desistiu");assertEquals("CANCELADA",b.status().name());assertEquals(usuario,b.idCancelador());
    assertEquals(b.id(),sales.cancelar(a.id(),a.versao(),"Cliente desistiu").id());assertThrows(ConflictException.class,()->sales.concluir(a.id(),b.versao()));
    assertEquals(0,movements());amount("0",balance());
  }
  @Test void duplicateProductsAggregateBeforeAnyEffectsAndForeignItemIdsFail() {
    stock("3");var a=sales.criar(UUID.randomUUID(),request(null,List.of(row(null,produto,"2","1","0"),row(null,produto,"2","1","0"))));
    assertThrows(ConflictException.class,()->sales.concluir(a.id(),a.versao()));amount("3",balance());assertEquals(0,movements());
    var b=sale("1");assertThrows(InvalidInputException.class,()->sales.atualizar(b.id(),request(b.versao(),List.of(row(a.itens().getFirst().id(),produto,"1","1","0")))));
    var edited=sales.atualizar(a.id(),request(a.versao(),List.of(row(a.itens().getFirst().id(),produto,"1","1","0"))));
    assertEquals(1,edited.itens().size());sales.concluir(edited.id(),edited.versao());amount("2",balance());
  }
  @Test void numericValidationStatusAndIdempotency() {
    for(var i:List.of(row(null,produto,"1","-1","0"),row(null,produto,"1.0001","1","0"),row(null,produto,"1","1","2"),row(null,produto,"1","10000000000","0")))
      assertThrows(InvalidInputException.class,()->sales.criar(UUID.randomUUID(),request(null,List.of(i))));
    var d=request(null,List.of(row(null,produto,"1","1","0")));UUID key=UUID.randomUUID();var a=sales.criar(key,d);
    assertEquals(a.id(),sales.criar(key,d).id());assertThrows(ConflictException.class,()->sales.criar(key,request(null,List.of(row(null,produto,"2","1","0")))));
    assertThrows(InvalidInputException.class,()->sales.criar(UUID.randomUUID(),new VendaRequestDTO(null,"CONCLUIDA",null,customer,null,loja,null,d.itens())));
  }
  List<Object> parallel(Callable<?> a,Callable<?> b) throws Exception {
    var start=new CountDownLatch(1); var ready=new CountDownLatch(2);
    try(var pool=Executors.newFixedThreadPool(2)) {
      var futures=List.of(a,b).stream().map(task->pool.submit(()->{org.springframework.security.core.context.SecurityContextHolder.getContext().setAuthentication(authentication); ready.countDown(); start.await(); try {return task.call();} catch (ConflictException e) {return e;} })).toList();
      assertTrue(ready.await(5,TimeUnit.SECONDS)); start.countDown();
      List<Object> result=new ArrayList<>(); for(var f:futures) result.add(f.get(20,TimeUnit.SECONDS)); return result;
    }
  }
  @Test void competingSalesAndRepeatedConfirmationNeverOversell() throws Exception {
    stock("3");var a=sale("2");var b=sale("2");
    var results=parallel(()->sales.concluir(a.id(),a.versao()),()->sales.concluir(b.id(),b.versao()));
    assertEquals(1,results.stream().filter(ConflictException.class::isInstance).count());amount("1",balance());assertEquals(1,movements());
    var done=sales.listar(null,null,null).stream().filter(v->v.status().name().equals("CONCLUIDA")).findFirst().orElseThrow();
    parallel(()->sales.concluir(done.id(),done.versao()),()->sales.concluir(done.id(),done.versao()));amount("1",balance());assertEquals(1,movements());
  }
  @Test void concurrentCreateAndReceivingAreAtomic() throws Exception {
    stock("5");UUID key=UUID.randomUUID();var d=request(null,List.of(row(null,produto,"2","1","0")));
    var results=parallel(()->sales.criar(key,d),()->sales.criar(key,d));assertEquals(((VendaResponseDTO)results.getFirst()).id(),((VendaResponseDTO)results.get(1)).id());
    var sale=(VendaResponseDTO)results.getFirst();var receipt=service.criar(UUID.randomUUID(),new RecebimentoCompraRequestDTO(compra,null,null,null,StatusDocumento.RASCUNHO,null,List.of(new ItemRecebimentoRequestDTO(null,item,produto,new BigDecimal("4"),null))));
    parallel(()->sales.concluir(sale.id(),sale.versao()),()->service.concluir(receipt.id(),receipt.versao()));amount("7",balance());assertEquals(2,movements());
  }
  @Test void movementFailureRollsBackAllEffects() {
    stock("5");
    UUID second=UUID.randomUUID();
    sql.update("insert into produto(id,nome,id_categoria,id_unidade_medida,tipo_venda,status,criado_em) select ?,'Second',id_categoria,id_unidade_medida,tipo_venda,status,now() from produto where id=?",second,produto);
    sql.update("insert into estoque_produto(id,id_produto,id_loja,quantidade_atual,atualizado_em) values (?,?,?,5,now())",UUID.randomUUID(),second,loja);
    var a=sales.criar(UUID.randomUUID(),request(null,List.of(row(null,produto,"2","1","0"),row(null,second,"2","1","0"))));
    sql.execute("create function fail_sale_movement() returns trigger language plpgsql as $$ begin raise exception 'forced rollback'; end $$");
    sql.execute("create trigger fail_sale_movement before insert on movimentacao_estoque for each row execute function fail_sale_movement()");
    try {assertThrows(Exception.class,()->sales.concluir(a.id(),a.versao()));amount("10",balance());assertEquals(0,movements());var saved=sales.buscarPorId(a.id());assertEquals(a.versao(),saved.versao());assertNull(saved.concluidoEm());assertNull(saved.idConfirmador());assertEquals("ABERTA",saved.status().name());}
    finally {sql.execute("drop trigger fail_sale_movement on movimentacao_estoque");sql.execute("drop function fail_sale_movement()");}
  }
  @Test void creatorRemainsStableAndCreationReplayBelongsToItsActor() {
    stock("5");UUID key=UUID.randomUUID();var d=request(null,List.of(row(null,produto,"1","1","0")));var a=sales.criar(key,d);
    UUID modifier=UUID.randomUUID();sql.update("insert into usuario(id,nome,email,cargo,senha_hash,status,criado_em,revisado,alterar_senha) values (?,'Modifier','modifier@example.test','ADMIN','test','ATIVO',now(),true,false)",modifier);
    var principal=com.vilaverde.erp.security.AuthPrincipal.of(users.findById(modifier).orElseThrow(),false);
    org.springframework.security.core.context.SecurityContextHolder.getContext().setAuthentication(org.springframework.security.authentication.UsernamePasswordAuthenticationToken.authenticated(principal,null,principal.getAuthorities()));
    assertThrows(ResourceNotFoundException.class,()->sales.criar(key,d));
    var b=sales.atualizar(a.id(),request(a.versao(),List.of(row(a.itens().getFirst().id(),produto,"2","1","0"))));
    assertEquals(usuario,b.idUsuario());assertEquals(modifier,b.idModificador());
    var done=sales.concluir(b.id(),b.versao());assertEquals(usuario,done.idUsuario());assertEquals(modifier,done.idConfirmador());
    sql.update("update venda set legado=true where id=?",done.id());
    assertThrows(ConflictException.class,()->sales.concluir(done.id(),done.versao()));
  }

}
