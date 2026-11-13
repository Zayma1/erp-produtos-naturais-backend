package com.vilaverde.erp;
import com.vilaverde.erp.service.*;
import com.vilaverde.erp.dto.usuario.*;
import com.vilaverde.erp.dto.cliente.*;
import com.vilaverde.erp.dto.unidademedida.*;
import com.vilaverde.erp.entity.enums.*;
import com.vilaverde.erp.exception.*;
import com.vilaverde.erp.repository.*;
import java.util.*;
import java.util.concurrent.*;
import java.time.*;
import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.*;
@SpringBootTest
class FormsAuthTests {
  @DynamicPropertySource static void db(DynamicPropertyRegistry r) {
    r.add("spring.datasource.url",()->Objects.requireNonNull(System.getenv("TEST_DATABASE_URL")));
    r.add("spring.datasource.username",()->"stage7");r.add("spring.datasource.password",()->"local-stage7-only");
  }
  @Autowired JdbcTemplate sql;@Autowired UsuarioService users;@Autowired UsuarioRepository accounts;
  @Autowired LoginService login;
  @Autowired ClienteService clients;@Autowired UnidadeMedidaService units;
  UUID admin;final String password="Password-8291";
  @BeforeEach void seed(){sql.execute("truncate loja,usuario,cliente,fornecedor,unidade_medida,auth_attempt cascade");users.bootstrap("Nome de exibição","person@example.test",password);admin=accounts.findByEmail("person@example.test").orElseThrow().getId();}
  @Test void usernameIsIndependentCaseInsensitiveAndSharesLoginRateLimit(){
    assertEquals(200,login.login("PERSON",password).status());assertEquals(200,login.login("person@example.test",password).status());
    assertEquals(401,login.login("Nome de exibição",password).status());
    for(int i=0;i<4;i++)assertEquals(401,login.login(i%2==0?"person":"PERSON@example.test","bad").status());
    assertEquals(429,login.login("person","bad").status());assertEquals(429,login.login("person@example.test",password).status());
    assertEquals("person",accounts.findById(admin).orElseThrow().getUsuario());
  }
  @Test void duplicateAndMalformedUsersAreRejected(){
    assertThrows(ConflictException.class,()->users.criar(new UsuarioRequestDTO("Other","PERSON","other@example.test",password,CargoUsuario.FUNC,StatusCadastro.ATIVO,Set.of())));
    assertThrows(InvalidInputException.class,()->users.criar(new UsuarioRequestDTO("Other","bad user","other@example.test",password,CargoUsuario.FUNC,StatusCadastro.ATIVO,Set.of())));
  }
  @Test void documentVectorsAndMasks(){
    for(String v:List.of("04.252.011/0001-10","04252011000110","12.ABC.345/01DE-35","12abc34501de35"))assertTrue(Documents.valid(v,true),v);
    assertTrue(Documents.valid("529.982.247-25",false));
    for(String v:List.of("11111111111","52998224724","529982247250","ABC"))assertFalse(Documents.valid(v,false));
    for(String v:List.of("00000000000000","12ABC34501DE34","12ABC34501DEAA","@4252011000110"))assertFalse(Documents.valid(v,true));
  }
  @Test void optionalCpfDuplicateAndOwnEdit(){
    clients.criar(new ClienteRequestDTO("A",null,null));clients.criar(new ClienteRequestDTO("B","",null));
    var saved=clients.criar(new ClienteRequestDTO("C","529.982.247-25",null));assertEquals("52998224725",saved.cpf());
    clients.atualizar(saved.id(),new ClienteRequestDTO("Changed","52998224725",null));
    assertThrows(ConflictException.class,()->clients.criar(new ClienteRequestDTO("D","52998224725",null)));
  }
  @Test void concurrentCpfAndNormalizedUnitNames() throws Exception {
    try(var pool=Executors.newFixedThreadPool(2)) {
      var gate=new CountDownLatch(1);var results=List.of(1,2).stream().map(n->pool.submit(()->{gate.await();try{clients.criar(new ClienteRequestDTO("C","52998224725",null));return true;}catch(RuntimeException e){return false;}})).toList();gate.countDown();int count=0;for(var result:results)if(result.get())count++;assertEquals(1,count);
    }
    var unit=units.criar(new UnidadeMedidaRequestDTO("KG","  Quilo   Grama "));assertEquals("Quilo Grama",unit.nome());
    units.atualizar(unit.id(),new UnidadeMedidaRequestDTO("KG","QUILO GRAMA"));
    assertThrows(ConflictException.class,()->units.criar(new UnidadeMedidaRequestDTO("OTHER"," quilo  grama ")));
    units.criar(new UnidadeMedidaRequestDTO("KG","Outra unidade"));
  }
  @Test void controlledIbgeCacheStaleFailureAndAssociation(){
    class Controlled extends LocalidadesService {
      Instant clock=Instant.parse("2026-01-01T00:00:00Z");boolean fail=false;int calls=0;
      @Override protected Instant now(){return clock;}
      @Override protected String fetch(String path){calls++;if(fail)throw new IllegalStateException();return path.equals("estados")?"[{\"id\":42,\"nome\":\"Santa Catarina\",\"sigla\":\"SC\"}]":"[{\"id\":4204608,\"nome\":\"Criciúma\"}]";}
    }
    var service=new Controlled();assertFalse(service.estados().desatualizado());service.estados();assertEquals(1,service.calls);
    assertEquals("Criciúma",service.validate("SC","ignored",4204608,null,null,null).nome());assertThrows(InvalidInputException.class,()->service.validate("SC","C",1,null,null,null));
    service.fail=true;service.clock=service.clock.plus(Duration.ofHours(25));assertTrue(service.estados().desatualizado());
    service.clock=service.clock.plus(Duration.ofDays(7));assertThrows(org.springframework.web.server.ResponseStatusException.class,service::estados);
    assertEquals("Antigo",service.validate("SC","Antigo",null,"SC","Antigo",null).nome());assertThrows(InvalidInputException.class,()->service.validate("SC","Alterado",null,"SC","Antigo",null));
  }

  @org.springframework.boot.test.context.TestConfiguration
  static class ControlledCatalog {
    @org.springframework.context.annotation.Bean @org.springframework.context.annotation.Primary
    LocalidadesService controlledLocalidades() {return new LocalidadesService(){
      @Override protected String fetch(String path){return path.equals("estados")?"[{\"id\":42,\"nome\":\"Santa Catarina\",\"sigla\":\"SC\"}]":"[{\"id\":4204608,\"nome\":\"Criciúma\"}]";}
    };}
  }
  @Autowired FornecedorService suppliers;@Autowired LojaService stores;
  @Test void supplierExactlyOneDocumentAndCrossRegistryReuse() {
    var request=new com.vilaverde.erp.dto.fornecedor.FornecedorRequestDTO("F","F","SC","ignored","B",4204608,"","04.252.011/0001-10",null);
    var saved=suppliers.criar(request);assertEquals("04252011000110",saved.cnpj());assertEquals("Criciúma",saved.cidade());
    suppliers.atualizar(saved.id(),request);assertThrows(ConflictException.class,()->suppliers.criar(request));
    assertThrows(InvalidInputException.class,()->suppliers.criar(new com.vilaverde.erp.dto.fornecedor.FornecedorRequestDTO("F","F","SC","C","B",4204608,"","04252011000110","52998224725")));
    clients.criar(new ClienteRequestDTO("C","52998224725",null));
    suppliers.criar(new com.vilaverde.erp.dto.fornecedor.FornecedorRequestDTO("F","F","SC","C","B",4204608,"",null,"529.982.247-25"));
    assertThrows(ConflictException.class,()->suppliers.criar(new com.vilaverde.erp.dto.fornecedor.FornecedorRequestDTO("F","F","SC","C","B",4204608,"",null,"52998224725")));
  }
  @Test void numericAndAlphanumericStoresPersistCanonicalDocuments() {
    var principal=com.vilaverde.erp.security.AuthPrincipal.of(accounts.findById(admin).orElseThrow(),false);
    org.springframework.security.core.context.SecurityContextHolder.getContext().setAuthentication(org.springframework.security.authentication.UsernamePasswordAuthenticationToken.authenticated(principal,null,principal.getAuthorities()));
    try {
      var request=new com.vilaverde.erp.dto.loja.LojaRequestDTO("L","L","SC","ignored","B","1",4204608,"","04.252.011/0001-10");
      var saved=stores.criar(request);assertEquals("04252011000110",saved.cnpj());stores.atualizar(saved.id(),request);
      assertThrows(ConflictException.class,()->stores.criar(new com.vilaverde.erp.dto.loja.LojaRequestDTO("L","L","SC","C","B","1",4204608,"","04252011000110")));
      assertEquals("12ABC34501DE35",stores.criar(new com.vilaverde.erp.dto.loja.LojaRequestDTO("L","L","SC","C","B","1",4204608,"","12.abc.345/01de-35")).cnpj());
      assertThrows(InvalidInputException.class,()->stores.criar(new com.vilaverde.erp.dto.loja.LojaRequestDTO("L","L","SC","C","B","1",4204608,"","bad")));
    } finally {org.springframework.security.core.context.SecurityContextHolder.clearContext();}
  }

  @Test void supplierAndUnitIndexesCoverConcurrentWrites() throws Exception {
    var request=new com.vilaverde.erp.dto.fornecedor.FornecedorRequestDTO("F","F","SC","C","B",4204608,"","04.252.011/0001-10",null);
    try(var pool=Executors.newFixedThreadPool(2)) {
      var gate=new CountDownLatch(1);
      var jobs=List.of(1,2).stream().map(n->pool.submit(()->{gate.await();try{suppliers.criar(request);return true;}catch(RuntimeException e){return false;}})).toList();gate.countDown();int successes=0;for(var job:jobs)if(job.get())successes++;assertEquals(1,successes);
      var start=new CountDownLatch(1);
      var unitsJobs=List.of("  Nova   Unidade ","NOVA UNIDADE").stream().map(name->pool.submit(()->{start.await();try{units.criar(new UnidadeMedidaRequestDTO("UN",name));return true;}catch(RuntimeException e){return false;}})).toList();start.countDown();successes=0;for(var job:unitsJobs)if(job.get())successes++;assertEquals(1,successes);
    }
  }

  @Test void unchangedLegacyAddressTreatsEmptyAndNullComplementEqually() {
    UUID id=UUID.randomUUID();sql.update("insert into fornecedor(id,nome_fantasia,razao_social,estado,cidade,bairro,cnpj,status) values (?,'F','F','SC','Município antigo sem correspondência','B','04252011000110','ATIVO')",id);
    var unchanged=new com.vilaverde.erp.dto.fornecedor.FornecedorRequestDTO("Nome alterado","F","SC","Município antigo sem correspondência","B",null,"","04252011000110",null);
    var saved=suppliers.atualizar(id,unchanged);assertNull(saved.municipioIbge());assertEquals("Município antigo sem correspondência",saved.cidade());
    assertThrows(InvalidInputException.class,()->suppliers.atualizar(id,new com.vilaverde.erp.dto.fornecedor.FornecedorRequestDTO("Nome alterado","F","SC",saved.cidade(),"Outro bairro",null,"","04252011000110",null)));
  }
}
