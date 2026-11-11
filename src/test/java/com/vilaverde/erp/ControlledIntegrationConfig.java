package com.vilaverde.erp;
import com.vilaverde.erp.service.LocalidadesService;
import org.springframework.context.annotation.*;
import java.net.*;
import java.net.http.*;
import java.time.Duration;
/** Only on the test classpath, enabled exclusively by the disposable integration runner. */
@Configuration @Profile("integration-controlled")
public class ControlledIntegrationConfig {
  @Bean @Primary LocalidadesService controlledIntegrationLocalidades() {
    if(!"1".equals(System.getenv("CATALOG_DISPOSABLE")))throw new IllegalStateException("Controlled services require a disposable test environment");
    URI origin=URI.create(System.getenv("IBGE_FIXTURE_URL"));
    if(!"127.0.0.1".equals(origin.getHost()))throw new IllegalStateException("Fixture must use loopback");
    return new LocalidadesService(){
      final HttpClient client=HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(3)).build();
      @Override protected String fetch(String path) throws Exception {
        var response=client.send(HttpRequest.newBuilder(origin.resolve("/ibge/"+path)).timeout(Duration.ofSeconds(5)).GET().build(),HttpResponse.BodyHandlers.ofString());
        if(response.statusCode()!=200)throw new IllegalStateException("IBGE fixture unavailable");return response.body();
      }
    };
  }
}
