package com.vilaverde.erp;
import java.sql.*;
import java.util.*;
import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;
import org.springframework.jdbc.datasource.SingleConnectionDataSource;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;
import org.springframework.core.io.ClassPathResource;

class MigrationTests {
  String url,name,admin;
  Connection connection;
  @BeforeEach void create() throws Exception {
    admin=Objects.requireNonNull(System.getenv("TEST_DATABASE_URL"));
    name="migration_"+UUID.randomUUID().toString().replace("-","");
    try(var c=DriverManager.getConnection(admin,"stage7","local-stage7-only")) { c.createStatement().execute("create database "+name); }
    url=admin.substring(0,admin.lastIndexOf('/')+1)+name;
    connection=DriverManager.getConnection(url,"stage7","local-stage7-only");
  }
  @AfterEach void drop() throws Exception {
    if(connection!=null) connection.close();
    try(var c=DriverManager.getConnection(admin,"stage7","local-stage7-only")) { c.createStatement().execute("drop database "+name+" with (force)"); }
  }
  Flyway flyway() { return Flyway.configure().dataSource(url,"stage7","local-stage7-only").baselineOnMigrate(false).locations("classpath:db/migration").load(); }
  void legacy() { new ResourceDatabasePopulator(new ClassPathResource("db/migration/V1__legacy_schema.sql")).execute(new SingleConnectionDataSource(connection,true)); }
  @Test void emptyDatabaseAndReconnectRetainOperationalData() throws Exception {
    flyway().migrate();
    UUID key=UUID.randomUUID();
    connection.createStatement().execute("insert into recebimento_idempotencia values ('"+key+"','hash','"+UUID.randomUUID()+"',true)");
    connection.close(); connection=DriverManager.getConnection(url,"stage7","local-stage7-only");
    flyway().migrate();
    var result=connection.createStatement().executeQuery("select count(*) from recebimento_idempotencia"); result.next(); assertEquals(1,result.getInt(1));
  }
  @Test void existingSchemaRequiresExplicitBaselineThenMigrates() throws Exception {
    legacy();
    connection.createStatement().execute("insert into categoria values ('"+UUID.randomUUID()+"','Preserved')");
    assertThrows(org.flywaydb.core.api.FlywayException.class,()->flyway().migrate());
    flyway().baseline(); flyway().migrate();
    var result=connection.createStatement().executeQuery("select nome from categoria"); result.next(); assertEquals("Preserved",result.getString(1));
  }
  @Test void legacyReceivedQuantitiesAreNeverReplayed() throws Exception {
    legacy();
    String c=UUID.randomUUID().toString(),u=UUID.randomUUID().toString(),p=UUID.randomUUID().toString(),f=UUID.randomUUID().toString(),l=UUID.randomUUID().toString(),b=UUID.randomUUID().toString();
    var st=connection.createStatement();
    st.execute("insert into categoria values ('"+c+"','C')");
    st.execute("insert into unidade_medida(id,nome,sigla) values ('"+u+"','U','U')");
    st.execute("insert into produto(id,nome,id_categoria,id_unidade_medida,tipo_venda,status,criado_em) values ('"+p+"','P','"+c+"','"+u+"','UNIDADE','ATIVO',now())");
    st.execute("insert into fornecedor(id,nome_fantasia,razao_social,estado,cidade,bairro,status) values ('"+f+"','F','F','SC','C','B','ATIVO')");
    st.execute("insert into loja(id,cnpj,nome_fantasia,razao_social,bairro,cidade,estado,numero,status,criado_em) values ('"+l+"','12345678901234','L','L','B','C','SC','1','ATIVA',now())");
    st.execute("insert into compra(id,id_fornecedor,id_loja,status,valor_total,data_compra,criado_em) values ('"+b+"','"+f+"','"+l+"','RASCUNHO',10,now(),now())");
    st.execute("insert into item_compra(id,id_compra,id_produto,quantidade,quantidade_recebida,custo_unitario,desconto,total_item) values ('"+UUID.randomUUID()+"','"+b+"','"+p+"',10,2,1,0,10)");
    flyway().baseline(); assertThrows(org.flywaydb.core.api.FlywayException.class,()->flyway().migrate());
    var result=st.executeQuery("select quantidade_recebida from item_compra"); result.next(); assertEquals(2,result.getInt(1));
    result=st.executeQuery("select count(*) from estoque_produto"); result.next(); assertEquals(0,result.getInt(1));
  }
  @Test void incompatibleLegacyReceivingBlocksWithoutSchemaChanges() throws Exception {
    legacy();
    // A legacy trigger could apply unversioned stock effects: it must block migration.
    connection.createStatement().execute("create function legacy_receiving() returns trigger language plpgsql as $$ begin return NEW; end $$");
    connection.createStatement().execute("create trigger legacy_receiving before insert on recebimento_compra for each row execute function legacy_receiving()");
    flyway().baseline(); assertThrows(org.flywaydb.core.api.FlywayException.class,()->flyway().migrate());
    var result=connection.createStatement().executeQuery("select count(*) from information_schema.columns where table_name='recebimento_compra' and column_name='versao'"); result.next(); assertEquals(0,result.getInt(1));
  }
  @Test void authenticationMigrationInvalidatesLegacyWithoutGrantingPrivileges() throws Exception {
    Flyway.configure().dataSource(url,"stage7","local-stage7-only").locations("classpath:db/migration").target("2").load().migrate();
    UUID id=UUID.randomUUID();var st=connection.createStatement();
    st.execute("insert into usuario(id,nome,email,cargo,senha_hash,status,criado_em) values ('"+id+"','Legacy',' LEGACY@EXAMPLE.TEST ','ADMIN','plaintext-never-accepted','ATIVO',now())");
    flyway().migrate();var r=st.executeQuery("select * from usuario where id='"+id+"'");r.next();
    assertEquals("legacy@example.test",r.getString("email"));assertEquals("ADMIN",r.getString("cargo_legado"));assertEquals("FUNC",r.getString("cargo"));assertFalse(r.getBoolean("revisado"));assertEquals("!INVALIDATED!",r.getString("senha_hash"));
    r=st.executeQuery("select count(*) from usuario_loja");r.next();assertEquals(0,r.getInt(1));
  }
  @Test void normalizedEmailCollisionReportsEveryIdAndRollsBack() throws Exception {
    Flyway.configure().dataSource(url,"stage7","local-stage7-only").locations("classpath:db/migration").target("2").load().migrate();
    var st=connection.createStatement();UUID a=UUID.randomUUID(),b=UUID.randomUUID();
    st.execute("insert into usuario(id,nome,email,cargo,senha_hash,status,criado_em) values ('"+a+"','A',' A@example.test ','ADMIN','legacy','ATIVO',now()),('"+b+"','B','a@example.test','GER','legacy','ATIVO',now())");
    var ex=assertThrows(org.flywaydb.core.api.FlywayException.class,()->flyway().migrate());String detail=ex.getMessage();assertTrue(detail.contains(a.toString()));assertTrue(detail.contains(b.toString()));
    var r=st.executeQuery("select count(*) from usuario where senha_hash='legacy'");r.next();assertEquals(2,r.getInt(1));
  }

}
