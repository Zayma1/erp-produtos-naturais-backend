package com.vilaverde.erp;

import com.vilaverde.erp.service.*;
import com.vilaverde.erp.repository.UsuarioRepository;
import com.vilaverde.erp.dto.usuario.UsuarioRequestDTO;
import com.vilaverde.erp.entity.enums.*;
import java.net.*;
import java.net.http.*;
import java.util.*;
import java.util.concurrent.*;
import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.session.jdbc.JdbcIndexedSessionRepository;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import tools.jackson.databind.*;

@SpringBootTest(webEnvironment=SpringBootTest.WebEnvironment.RANDOM_PORT)
@org.springframework.context.annotation.Import(AuthenticationTests.ProbeFixture.class)
class AuthenticationTests {
  @org.springframework.boot.test.context.TestConfiguration
  static class ProbeFixture {
    @org.springframework.context.annotation.Bean @org.springframework.context.annotation.Primary
    IbgeNetworkDiagnostics controlledNetworkDiagnostics(){return new IbgeNetworkDiagnostics(){
      @Override protected List<Check> checks(){return List.of(new Check("controlled",true,0,List.of("offline CI fixture")));}
    };}
  }
  @Test void networkDiagnosticsAreRestrictedToAdministrators() throws Exception {
    assertEquals(401,new Browser().call("GET","/api/localidades/diagnostico",null,false).statusCode());
    var m=authenticated("manager@example.test",manager);assertEquals(403,m.call("GET","/api/localidades/diagnostico",null,false).statusCode());
    var e=authenticated("employee@example.test",employee);assertEquals(403,e.call("GET","/api/localidades/diagnostico",null,false).statusCode());
    var a=authenticated("admin@example.test",admin);var response=a.call("GET","/api/localidades/diagnostico",null,false);
    assertEquals(200,response.statusCode());assertEquals("no-store",response.headers().firstValue("Cache-Control").orElse(""));assertTrue(json.readTree(response.body()).has("state"));
  }

  @DynamicPropertySource static void db(DynamicPropertyRegistry r) {
    r.add("spring.datasource.url",()->Objects.requireNonNull(System.getenv("TEST_DATABASE_URL")));
    r.add("spring.datasource.username",()->"stage7");r.add("spring.datasource.password",()->"local-stage7-only");
    r.add("server.servlet.session.cookie.secure",()->false);
  }
  @LocalServerPort int port;
  @Autowired JdbcTemplate sql;
  @Autowired UsuarioService users;
  @Autowired UsuarioRepository repository;
  @Autowired PasswordEncoder encoder;
  @Autowired JdbcIndexedSessionRepository sessions;
  ObjectMapper json=new ObjectMapper();
  final String initial="Temporary-Auth-8291", permanent="Permanent-Auth-8291";
  UUID admin, store, other, manager, employee, purchase;
  class Browser {
    CookieManager cookies=new CookieManager(null,CookiePolicy.ACCEPT_ALL);
    HttpClient http=HttpClient.newBuilder().cookieHandler(cookies).build();
    String csrf;
    HttpResponse<String> call(String method,String path,Object body,boolean token) throws Exception {
      var r=HttpRequest.newBuilder(URI.create("http://127.0.0.1:"+port+path)).header("Accept","application/json");
      if(token) {if(csrf==null)renew();r.header("X-CSRF-TOKEN",csrf);}
      if(body!=null)r.header("Content-Type","application/json");
      if(method.equals("POST") && path.equals("/api/vendas"))r.header("Idempotency-Key",UUID.randomUUID().toString());
      return http.send(r.method(method,body==null ? HttpRequest.BodyPublishers.noBody() : HttpRequest.BodyPublishers.ofString(json.writeValueAsString(body))).build(),HttpResponse.BodyHandlers.ofString());
    }
    void renew() throws Exception {csrf=json.readTree(call("GET","/api/auth/csrf",null,false).body()).get("token").asText();}
    HttpResponse<String> login(String email,String password) throws Exception { var result=call("POST","/api/auth/login",Map.of("login",email,"senha",password),true);csrf=null;return result; }
    JsonNode me() throws Exception { var r=call("GET","/api/auth/me",null,false);assertEquals(200,r.statusCode(),r.body());return json.readTree(r.body()); }
  }
  @BeforeEach void seed() {
    sql.execute("TRUNCATE loja,usuario,categoria,unidade_medida,fornecedor,recebimento_idempotencia,spring_session,auth_attempt CASCADE");
    users.bootstrap("Admin"," ADMIN@example.test ",initial);admin=repository.findByEmail("admin@example.test").orElseThrow().getId();
    store=store("One","11111111111111");other=store("Two","22222222222222");
    manager=users.criar(new UsuarioRequestDTO("Manager","manager@example.test",initial,CargoUsuario.GER,StatusCadastro.ATIVO,Set.of(store))).id();
    employee=users.criar(new UsuarioRequestDTO("Employee","employee@example.test",initial,CargoUsuario.FUNC,StatusCadastro.ATIVO,Set.of(store))).id();
    UUID supplier=UUID.randomUUID();purchase=UUID.randomUUID();
    sql.update("insert into fornecedor(id,nome_fantasia,razao_social,estado,cidade,bairro,status) values (?,'F','F','SC','C','B','ATIVO')",supplier);
    sql.update("insert into compra(id,id_fornecedor,id_loja,status,valor_total,data_compra,criado_em) values (?,?,?,'RASCUNHO',0,now(),now())",purchase,supplier,other);
  }
  UUID store(String name,String cnpj) { var id=UUID.randomUUID();sql.update("insert into loja(id,cnpj,nome_fantasia,razao_social,bairro,cidade,estado,numero,status,criado_em) values (?,?,?,?,'B','C','SC','1','ATIVA',now())",id,cnpj,name,name);return id; }
  Browser authenticated(String email,UUID id) throws Exception {users.changePassword(id,initial,permanent);var b=new Browser();assertEquals(200,b.login(email,permanent).statusCode());return b;}
  @Test void anonymousAndCsrfAreEnforcedDirectly() throws Exception {
    var b=new Browser();for(String path:List.of("/api/categorias","/api/usuarios","/api/compras","/api/estoques"))assertEquals(401,b.call("GET",path,null,false).statusCode());
    assertTrue(b.call("GET","/api/categorias",null,false).body().contains("Autenticação necessária"));
    assertEquals(401,b.call("POST","/api/auth/login",Map.of("email","admin@example.test","senha",initial),false).statusCode());
    var a=authenticated("admin@example.test",admin);
    assertEquals(403,a.call("POST","/api/categorias",Map.of("nome","Forbidden"),false).statusCode());
    assertEquals(403,a.call("POST","/api/auth/logout",null,false).statusCode());
    assertEquals(201,a.call("POST","/api/categorias",Map.of("nome","Allowed"),true).statusCode());
  }
  @Autowired com.vilaverde.erp.config.InitialAdminInitializer initializer;
  @Test void automaticAdminIsHashedForcedToChangeAndNotResetOnRestart() throws Exception {
    sql.execute("TRUNCATE usuario CASCADE");
    initializer.run(null);
    var account=repository.findByEmail(AuthorizationService.DEFAULT_ADMIN_EMAIL).orElseThrow();
    assertTrue(encoder.matches("admin",account.getSenhaHash()));assertNotEquals("admin",account.getSenhaHash());
    assertTrue(account.isAlterarSenha());assertEquals(CargoUsuario.ADMIN,account.getCargo());
    var browser=new Browser();assertEquals(200,browser.login(" ADMIN ","admin").statusCode());
    assertEquals(403,browser.call("GET","/api/categorias",null,false).statusCode());
    assertEquals(204,browser.call("POST","/api/auth/alterar-senha",Map.of("senhaAtual","admin","novaSenha",permanent),true).statusCode());
    var updated=repository.findById(account.getId()).orElseThrow();String hash=updated.getSenhaHash();long version=updated.getAuthVersion();
    initializer.run(null);initializer.run(null);
    var unchanged=repository.findById(account.getId()).orElseThrow();assertEquals(hash,unchanged.getSenhaHash());assertEquals(version,unchanged.getAuthVersion());assertFalse(unchanged.isAlterarSenha());
    browser.csrf=null;assertEquals(401,browser.login("admin","admin").statusCode());assertEquals(200,browser.login("admin",permanent).statusCode());
    assertEquals(1,repository.count());
  }
  @Test void concurrentAutomaticBootstrapsCreateOneAdminAndNeverChangeExistingAdmins() throws Exception {
    String oldHash=repository.findById(admin).orElseThrow().getSenhaHash();
    initializer.run(null);assertEquals(oldHash,repository.findById(admin).orElseThrow().getSenhaHash());assertFalse(repository.existsByEmail(AuthorizationService.DEFAULT_ADMIN_EMAIL));
    sql.execute("TRUNCATE usuario CASCADE");
    new com.vilaverde.erp.config.InitialAdminInitializer(users,false,"admin").run(null);assertEquals(0,repository.count());
    assertThrows(com.vilaverde.erp.exception.InvalidInputException.class,()->users.bootstrap("Admin","manual@example.test","admin"));
    assertEquals(0,repository.count());
    try(var executor=Executors.newFixedThreadPool(2)) {
      var gate=new CountDownLatch(1);
      var tasks=List.of(1,2).stream().map(n->executor.submit(()->{gate.await();return users.bootstrapDefaultAdmin("admin");})).toList();gate.countDown();
      assertNotEquals(tasks.get(0).get(),tasks.get(1).get());
    }
    assertEquals(1,repository.count());
  }
  @Test void passwordPolicyAcceptsEightAndRejectsSevenCharacters() throws Exception {
    var browser=new Browser();assertEquals(200,browser.login("admin@example.test",initial).statusCode());
    assertEquals(400,browser.call("POST","/api/auth/alterar-senha",Map.of("senhaAtual",initial,"novaSenha","1234567"),true).statusCode());
    assertEquals(204,browser.call("POST","/api/auth/alterar-senha",Map.of("senhaAtual",initial,"novaSenha","newpass8"),true).statusCode());
    browser.csrf=null;assertEquals(200,browser.login("admin@example.test","newpass8").statusCode());
    assertThrows(com.vilaverde.erp.exception.InvalidInputException.class,()->users.reset(employee,"1234567"));
    users.reset(employee,"temp1234");assertTrue(encoder.matches("temp1234",repository.findById(employee).orElseThrow().getSenhaHash()));
    assertThrows(com.vilaverde.erp.exception.InvalidInputException.class,()->users.criar(new UsuarioRequestDTO("Short","short@example.test","1234567",CargoUsuario.FUNC,StatusCadastro.ATIVO,Set.of())));
    var account=users.criar(new UsuarioRequestDTO("Eight","eight@example.test","temp1234",CargoUsuario.FUNC,StatusCadastro.ATIVO,Set.of()));
    assertTrue(encoder.matches("temp1234",repository.findById(account.id()).orElseThrow().getSenhaHash()));
    assertEquals(400,browser.call("POST","/api/usuarios/"+employee+"/senha-temporaria",Map.of("senha","1234567"),true).statusCode());
    assertEquals(204,browser.call("POST","/api/usuarios/"+employee+"/senha-temporaria",Map.of("senha","temp5678"),true).statusCode());
  }
  @Test void exportAuthenticatedContract() throws Exception {
    var a=authenticated("admin@example.test",admin);var r=a.call("GET","/v3/api-docs",null,false);assertEquals(200,r.statusCode());
    var contract=json.readTree(r.body());
    assertTrue(contract.path("paths").path("/api/auth/login").path("post").path("responses").has("429"));
    assertTrue(contract.path("paths").path("/api/auth/logout").path("post").path("responses").has("204"));
    String output=System.getenv("OPENAPI_EXPORT_FILE");if(output!=null)java.nio.file.Files.writeString(java.nio.file.Path.of(output),r.body());
  }
  @Test void normalizedLoginHashRotationAndMandatoryPasswordChange() throws Exception {
    var u=repository.findById(admin).orElseThrow();assertNotEquals(initial,u.getSenhaHash());assertTrue(encoder.matches(initial,u.getSenhaHash()));
    var b=new Browser();b.renew();String before=b.cookies.getCookieStore().getCookies().toString();
    var login=b.login(" ADMIN@EXAMPLE.TEST ",initial);assertEquals(200,login.statusCode(),login.body());
    assertNotEquals(before,b.cookies.getCookieStore().getCookies().toString());assertFalse(login.body().contains("senhaHash"));assertFalse(login.body().contains(u.getSenhaHash()));
    assertTrue(b.me().get("alterarSenha").asBoolean());assertEquals(403,b.call("GET","/api/categorias",null,false).statusCode());
    assertEquals(204,b.call("POST","/api/auth/alterar-senha",Map.of("senhaAtual",initial,"novaSenha",permanent),true).statusCode());
    assertEquals(401,b.call("GET","/api/auth/me",null,false).statusCode());
    b.csrf=null;assertEquals(200,b.login("admin@example.test",permanent).statusCode());assertFalse(b.me().get("alterarSenha").asBoolean());
    assertEquals(1,sql.queryForObject("select count(*) from spring_session where principal_name='admin@example.test'",Integer.class));
  }
  @Test void throttlePersistsAndUsesNormalizedEmailEvenForMissingAccounts() throws Exception {
    var b=new Browser();for(int i=0;i<4;i++)assertEquals(401,b.login(" MISSING@EXAMPLE.TEST ","wrong").statusCode());
    assertEquals(429,b.login("missing@example.test","wrong").statusCode());assertEquals(429,new Browser().login("missing@example.test","wrong").statusCode());
    sql.update("update auth_attempt set started_at=now()-interval '16 minutes'");assertEquals(401,b.login("missing@example.test","wrong").statusCode());
  }
  @Test void rolesAndStoresApplyWithoutFiltersAndDocumentIdsAreHidden() throws Exception {
    var m=authenticated("manager@example.test",manager);var e=authenticated("employee@example.test",employee);
    assertEquals(403,m.call("GET","/api/usuarios",null,false).statusCode());assertEquals(403,e.call("GET","/api/fornecedores",null,false).statusCode());assertEquals(403,e.call("GET","/api/compras",null,false).statusCode());
    assertEquals(403,e.call("POST","/api/categorias",Map.of("nome","No"),true).statusCode());
    var stores=json.readTree(m.call("GET","/api/lojas",null,false).body());assertEquals(1,stores.size());assertEquals(store.toString(),stores.get(0).get("id").asText());
    assertEquals("[]",m.call("GET","/api/compras",null,false).body());assertEquals(404,m.call("GET","/api/compras/"+purchase,null,false).statusCode());
    assertEquals(404,m.call("GET","/api/lojas/"+other,null,false).statusCode());
    assertEquals(400,e.call("PUT","/api/vendas/"+UUID.randomUUID(),Map.of(),true).statusCode());
    assertEquals(400,m.call("POST","/api/ajustes-estoque",Map.of(),true).statusCode());
    assertEquals(403,e.call("POST","/api/ajustes-estoque",Map.of(),true).statusCode());
    assertEquals(404,e.call("GET","/api/estoques/saldo?idProdutoSku="+UUID.randomUUID()+"&idLoja="+other,null,false).statusCode());
  }

  @Test void assignedStoreIsolationCoversCollectionsDetailsWritesAndConfirmation() throws Exception {
    var m=authenticated("manager@example.test",manager);var e=authenticated("employee@example.test",employee);
    UUID cat=UUID.randomUUID(),unit=UUID.randomUUID(),product=UUID.randomUUID(),customer=UUID.randomUUID();
    UUID stock=UUID.randomUUID(),movement=UUID.randomUUID(),sale=UUID.randomUUID(),receipt=UUID.randomUUID();
    sql.update("insert into categoria values (?,'C')",cat);sql.update("insert into unidade_medida(id,nome,sigla) values (?,'U','UN')",unit);
    SkuFixtures.create(sql,product,cat,unit,"P");
    sql.update("insert into cliente(id,nome,criado_em) values (?,'Customer',now())",customer);
    sql.update("insert into estoque_produto(id,id_produto_sku,id_loja,quantidade_atual,atualizado_em) values (?,?,?,10,now())",stock,product,other);
    sql.update("insert into movimentacao_estoque(id,id_produto_sku,id_loja,tipo,quantidade,data_movimentacao,id_usuario) values (?,?,?,'ENTRADA_COMPRA',10,now(),?)",movement,product,other,admin);
    sql.update("insert into venda(id,data_venda,status,subtotal,desconto_total,valor_total,criado_em,id_cliente,id_usuario,id_loja) values (?,now(),'ABERTA',0,0,0,now(),?,?,?)",sale,customer,admin,other);
    sql.update("insert into recebimento_compra(id,id_compra,id_usuario,status,criado_em,data_recebimento,versao) values (?,?,?,'RASCUNHO',now(),now(),0)",receipt,purchase,admin);
    for(var browser:List.of(m,e)) {
      for(String resource:List.of("estoques","movimentacoes-estoque","vendas")) {
        var response=browser.call("GET","/api/"+resource,null,false);assertEquals(200,response.statusCode(),response.body());assertEquals("[]",response.body());
        assertEquals("[]",browser.call("GET","/api/"+resource+"?idLoja="+other,null,false).body());
      }
      assertEquals(404,browser.call("GET","/api/estoques/"+stock,null,false).statusCode());assertEquals(404,browser.call("GET","/api/movimentacoes-estoque/"+movement,null,false).statusCode());assertEquals(404,browser.call("GET","/api/vendas/"+sale,null,false).statusCode());
      assertEquals(404,browser.call("GET","/api/estoques/saldo?idProdutoSku="+product+"&idLoja="+other,null,false).statusCode());
      var body=Map.of("idCliente",customer,"idLoja",other,"itens",List.of(Map.of("idProdutoSku",product,"quantidade",1,"precoUnitario",1)));
      assertEquals(404,browser.call("POST","/api/vendas",body,true).statusCode());
      var forged=new HashMap<String,Object>(body);forged.put("idLoja",store);forged.put("idUsuario",admin);
      assertEquals(403,browser.call("POST","/api/vendas",forged,true).statusCode());
    }
    assertEquals("[]",m.call("GET","/api/recebimentos-compras",null,false).body());assertEquals("[]",m.call("GET","/api/recebimentos-compras?idCompra="+purchase,null,false).body());
    assertEquals(404,m.call("GET","/api/recebimentos-compras/"+receipt,null,false).statusCode());
    assertEquals(404,m.call("POST","/api/recebimentos-compras/"+receipt+"/concluir",Map.of("versao",0),true).statusCode());
    assertEquals(404,m.call("DELETE","/api/recebimentos-compras/"+receipt+"?versao=0",null,true).statusCode());
    var own=Map.of("idCliente",customer,"idLoja",store,"itens",List.of(Map.of("idProdutoSku",product,"quantidade",1,"precoUnitario",1)));
    var created=e.call("POST","/api/vendas",own,true);assertEquals(201,created.statusCode(),created.body());assertEquals(employee.toString(),json.readTree(created.body()).get("idUsuario").asText());
    var saved=json.readTree(created.body());String saleId=saved.get("id").asText();
    assertEquals(404,e.call("POST","/api/vendas/"+sale+"/concluir",Map.of("versao",0),true).statusCode());
    assertEquals(404,e.call("POST","/api/vendas/"+sale+"/cancelar",Map.of("versao",0,"motivo","Teste"),true).statusCode());
    var move=new HashMap<String,Object>(own);move.put("versao",0);move.put("idLoja",other);
    assertEquals(404,e.call("PUT","/api/vendas/"+saleId,move,true).statusCode());
    assertEquals(403,e.call("PUT","/api/vendas/"+saleId,move,false).statusCode());
    assertEquals(409,e.call("POST","/api/vendas/"+saleId+"/concluir",Map.of("versao",0),true).statusCode());
    assertEquals(200,e.call("POST","/api/vendas/"+saleId+"/cancelar",Map.of("versao",0,"motivo","Teste"),true).statusCode());
    assertEquals(403,e.call("DELETE","/api/vendas/"+saleId,null,true).statusCode());

  }
  @Test void logoutAndPasswordResetRevokeAllSessions() throws Exception {
    var a=authenticated("admin@example.test",admin);var second=new Browser();assertEquals(200,second.login("admin@example.test",permanent).statusCode());
    users.reset(admin,"Reset-temporary-8291");assertEquals(401,a.call("GET","/api/auth/me",null,false).statusCode());assertEquals(401,second.call("GET","/api/auth/me",null,false).statusCode());
    assertEquals(401,new Browser().login("admin@example.test",permanent).statusCode());
    var b=new Browser();assertEquals(200,b.login("admin@example.test","Reset-temporary-8291").statusCode());
    var logout=b.call("POST","/api/auth/logout",Map.of(),true);assertEquals(204,logout.statusCode());assertEquals(401,b.call("GET","/api/auth/me",null,false).statusCode());
  }
  @Test void permissionChangesAndDeactivationRevokeAndFinalAdminIsProtected() throws Exception {
    var m=authenticated("manager@example.test",manager);
    users.atualizar(manager,new UsuarioRequestDTO("Manager","manager@example.test",null,CargoUsuario.FUNC,StatusCadastro.ATIVO,Set.of()));
    assertEquals(401,m.call("GET","/api/auth/me",null,false).statusCode());
    var e=authenticated("employee@example.test",employee);users.excluir(employee);assertEquals(401,e.call("GET","/api/auth/me",null,false).statusCode());
    assertThrows(com.vilaverde.erp.exception.ConflictException.class,()->users.excluir(admin));
    assertThrows(com.vilaverde.erp.exception.ConflictException.class,()->users.bootstrap("Other","other@example.test",initial));
  }
  @Test void idleAndAbsoluteSessionExpiryAreEnforced() throws Exception {
    var a=authenticated("admin@example.test",admin);
    sql.update("update spring_session set expiry_time=0,last_access_time=0");assertEquals(401,a.call("GET","/api/auth/me",null,false).statusCode());
    a.csrf=null;assertEquals(200,a.login("admin@example.test",permanent).statusCode());
    for(Object entry:sessions.findByPrincipalName("admin@example.test").values()) { var s=(org.springframework.session.Session)entry;s.setAttribute("authenticatedAt",System.currentTimeMillis()-43200001L);((org.springframework.session.SessionRepository)sessions).save(s); }
    assertEquals(401,a.call("GET","/api/auth/me",null,false).statusCode());
  }
  @Test void simultaneousFinalAdminRemovalKeepsOneActive() throws Exception {
    UUID second=users.criar(new UsuarioRequestDTO("Second","second@example.test",initial,CargoUsuario.ADMIN,StatusCadastro.ATIVO,Set.of())).id();
    try(var executor=Executors.newFixedThreadPool(2)) {
      var gate=new CountDownLatch(1);
      var futures=List.of(admin,second).stream().map(id->executor.submit(()->{gate.await();try{users.excluir(id);return true;}catch(com.vilaverde.erp.exception.ConflictException ex){return false;}})).toList();gate.countDown();
      assertNotEquals(futures.get(0).get(),futures.get(1).get());
    }
    assertEquals(1,sql.queryForObject("select count(*) from usuario where cargo='ADMIN' and status='ATIVO' and revisado",Integer.class));
  }

  @Test void onlyAdministratorsResetPasswordsAndOldSessionsAreRevoked() throws Exception {
    var a=authenticated("admin@example.test",admin);var m=authenticated("manager@example.test",manager);var e=authenticated("employee@example.test",employee);
    String path="/api/usuarios/"+employee+"/senha-temporaria",temporary="NewTemporary8291",next="ChangedByPerson8291";
    assertEquals(403,e.call("POST",path,Map.of("senha",temporary),true).statusCode());
    assertEquals(403,m.call("POST",path,Map.of("senha",temporary),true).statusCode());
    assertEquals(403,a.call("POST",path,Map.of("senha",temporary),false).statusCode());
    assertEquals(204,a.call("POST",path,Map.of("senha",temporary),true).statusCode());
    assertEquals(401,e.call("GET","/api/auth/me",null,false).statusCode());e.csrf=null;
    assertEquals(401,e.login("employee@example.test",permanent).statusCode());
    assertEquals(200,e.login("employee@example.test",temporary).statusCode());assertTrue(e.me().get("alterarSenha").asBoolean());
    assertEquals(403,e.call("GET","/api/clientes",null,false).statusCode());
    assertEquals(204,e.call("POST","/api/auth/alterar-senha",Map.of("senhaAtual",temporary,"novaSenha",next),true).statusCode());
    e.csrf=null;assertEquals(200,e.login("employee@example.test",next).statusCode());assertFalse(e.me().get("alterarSenha").asBoolean());
    var account=repository.findById(employee).orElseThrow();assertEquals(CargoUsuario.FUNC,account.getCargo());assertEquals(Set.of(store),account.getLojas());assertEquals(StatusCadastro.ATIVO,account.getStatus());
    for(String removed:List.of("/api/auth/esqueci-senha","/api/auth/redefinir-senha"))assertEquals(403,a.call("POST",removed,Map.of("token","old-token","senha",next),true).statusCode());
  }
}
