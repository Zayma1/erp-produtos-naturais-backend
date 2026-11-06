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

  void salesLegacy(String status,boolean invalid) throws Exception {
    Flyway.configure().dataSource(url,"stage7","local-stage7-only").locations("classpath:db/migration").target("3").load().migrate();
    var st=connection.createStatement();
    st.execute("insert into categoria values ('00000000-0000-4000-8000-000000000001','C')");
    st.execute("insert into unidade_medida(id,nome,sigla) values ('00000000-0000-4000-8000-000000000002','U','KG')");
    st.execute("insert into produto(id,nome,id_categoria,id_unidade_medida,tipo_venda,status,criado_em) values ('00000000-0000-4000-8000-000000000003','P','00000000-0000-4000-8000-000000000001','00000000-0000-4000-8000-000000000002','UNIDADE','ATIVO',now())");
    st.execute("insert into loja(id,cnpj,nome_fantasia,razao_social,bairro,cidade,estado,numero,status,criado_em) values ('00000000-0000-4000-8000-000000000004','12345678901234','L','L','B','C','SC','1','ATIVA',now())");
    st.execute("insert into cliente(id,nome,criado_em) values ('00000000-0000-4000-8000-000000000007','C',now())");
    st.execute("insert into usuario(id,nome,email,cargo,senha_hash,status,criado_em) values ('00000000-0000-4000-8000-000000000008','U','u@example.test','FUNC','test','ATIVO',now())");
    st.execute("insert into venda(id,id_cliente,id_usuario,data_venda,status,subtotal,desconto_total,valor_total,criado_em,id_loja) values ('00000000-0000-4000-8000-000000000005','00000000-0000-4000-8000-000000000007','00000000-0000-4000-8000-000000000008',now(),'"+status+"',1,0,1,now(),'00000000-0000-4000-8000-000000000004')");
    st.execute("insert into produto_venda(id,id_venda,id_produto,quantidade,preco_unitario,desconto,total_item,unidade_sigla_snapshot) values ('00000000-0000-4000-8000-000000000006','00000000-0000-4000-8000-000000000005','00000000-0000-4000-8000-000000000003',1,"+(invalid ? "-1" : "1")+",0,1,'KG')");
  }
  @Test void validOpenSalesPreserveItemsSnapshotsAndReferences() throws Exception {
    salesLegacy("ABERTA",false);flyway().migrate();
    var r=connection.createStatement().executeQuery("select v.versao,v.legado,i.unidade_sigla_snapshot,i.id from venda v join produto_venda i on i.id_venda=v.id");r.next();
    assertEquals(0,r.getInt(1));assertFalse(r.getBoolean(2));assertEquals("KG",r.getString(3));assertEquals("00000000-0000-4000-8000-000000000006",r.getString(4));
  }
  @Test void historicalCompletedSalesAreQuarantinedWithoutStockReplay() throws Exception {
    salesLegacy("CONCLUIDA",false);flyway().migrate();flyway().migrate();
    var r=connection.createStatement().executeQuery("select legado,concluido_em from venda");r.next();assertTrue(r.getBoolean(1));assertNull(r.getObject(2));
    r=connection.createStatement().executeQuery("select count(*) from movimentacao_estoque");r.next();assertEquals(0,r.getInt(1));
    r=connection.createStatement().executeQuery("select count(*) from estoque_produto");r.next();assertEquals(0,r.getInt(1));
  }
  @Test void invalidSalesBlockV4WithoutAlteringLegacyData() throws Exception {
    salesLegacy("ABERTA",true);assertThrows(org.flywaydb.core.api.FlywayException.class,()->flyway().migrate());
    var r=connection.createStatement().executeQuery("select preco_unitario from produto_venda");r.next();assertEquals(-1,r.getInt(1));
    r=connection.createStatement().executeQuery("select count(*) from information_schema.columns where table_name='venda' and column_name='versao'");r.next();assertEquals(0,r.getInt(1));
  }


 @Test void v4TransfersBecomeReadOnlyWithoutInventedSnapshotsOrEffects() throws Exception {
   salesLegacy("ABERTA",false);
   Flyway.configure().dataSource(url,"stage7","local-stage7-only").locations("classpath:db/migration").target("4").load().migrate();
   var st=connection.createStatement();String other=UUID.randomUUID().toString(),transfer=UUID.randomUUID().toString(),item=UUID.randomUUID().toString();
   st.execute("insert into loja(id,cnpj,nome_fantasia,razao_social,bairro,cidade,estado,numero,status,criado_em) select '"+other+"','22345678901234','Other',razao_social,bairro,cidade,estado,numero,status,criado_em from loja");
   st.execute("insert into transferencia_estoque(id,id_loja_origem,id_loja_destino,status,criado_em) values ('"+transfer+"','00000000-0000-4000-8000-000000000004','"+other+"','RASCUNHO',now())");
   st.execute("insert into item_transferencia(id,id_transferencia,id_produto,quantidade) values ('"+item+"','"+transfer+"','00000000-0000-4000-8000-000000000003',1)");
   flyway().migrate();connection.close();connection=DriverManager.getConnection(url,"stage7","local-stage7-only");flyway().migrate();
   var r=connection.createStatement().executeQuery("select t.legado,t.versao,t.concluido_em,i.id,i.unidade_sigla_snapshot from transferencia_estoque t join item_transferencia i on t.id=i.id_transferencia");r.next();
   assertTrue(r.getBoolean(1));assertEquals(0,r.getInt(2));assertNull(r.getObject(3));assertEquals(item,r.getString(4));assertNull(r.getString(5));
   r=connection.createStatement().executeQuery("select count(*) from movimentacao_estoque");r.next();assertEquals(0,r.getInt(1));
   r=connection.createStatement().executeQuery("select count(*) from estoque_produto");r.next();assertEquals(0,r.getInt(1));
 }
}
