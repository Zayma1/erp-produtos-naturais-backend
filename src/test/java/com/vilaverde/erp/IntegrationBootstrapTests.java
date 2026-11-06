package com.vilaverde.erp;
import com.vilaverde.erp.service.UsuarioService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
// Available only on the test classpath; the runner must own the disposable database.
@EnabledIfEnvironmentVariable(named="CATALOG_DISPOSABLE",matches="1")
@SpringBootTest(webEnvironment=SpringBootTest.WebEnvironment.NONE)
class IntegrationBootstrapTests {
  @DynamicPropertySource static void db(DynamicPropertyRegistry r) {
    String url=java.util.Objects.requireNonNull(System.getenv("DB_URL"));
    if(!url.startsWith("jdbc:postgresql://127.0.0.1:"))throw new IllegalStateException("Disposable bootstrap requires loopback database");
    r.add("spring.datasource.url",()->url);r.add("spring.datasource.username",()->System.getenv("DB_USER"));r.add("spring.datasource.password",()->System.getenv("DB_PASSWORD"));
  }
  @Autowired UsuarioService users;
  @Autowired com.vilaverde.erp.repository.UsuarioRepository repository;
  @Test void bootstrapOwnedEnvironment() {
    String password=java.util.Objects.requireNonNull(System.getenv("INTEGRATION_ADMIN_PASSWORD"));
    users.bootstrap("Integration Admin","stage8-admin@example.test",password+"-temporary");
    users.changePassword(repository.findByEmail("stage8-admin@example.test").orElseThrow().getId(),password+"-temporary",password);
  }
}
