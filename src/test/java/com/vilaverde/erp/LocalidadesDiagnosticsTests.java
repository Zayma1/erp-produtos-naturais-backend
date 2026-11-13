package com.vilaverde.erp;
import com.vilaverde.erp.exception.*;
import java.net.http.HttpTimeoutException;
import org.junit.jupiter.api.*;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.SecurityContextHolder;
import static org.junit.jupiter.api.Assertions.*;
class LocalidadesDiagnosticsTests {
  @AfterEach void clear(){SecurityContextHolder.clearContext();}
  @Test void onlyAdminReceivesOriginalExceptionDetails(){
    var timeout=new HttpTimeoutException("upstream timeout details");
    timeout.initCause(new java.net.ConnectException("connection cause"));
    var error=new LocalidadesUnavailableException(timeout,null);
    for(String role:new String[]{"", "ROLE_FUNC","ROLE_GER","ROLE_ADMIN"}) {
      SecurityContextHolder.clearContext();
      if(!role.isEmpty())SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken("user","unused",AuthorityUtils.createAuthorityList(role)));
      var response=new GlobalExceptionHandler().unavailable(error);
      assertEquals(503,response.getStatusCode().value());
      assertEquals(role.equals("ROLE_ADMIN")?"REQUEST_TIMEOUT":null,response.getHeaders().getFirst("X-IBGE-Failure"));
      if(role.equals("ROLE_ADMIN")) {
        var trace=String.join("\n",response.getBody().detalhes());
        assertTrue(trace.contains("java.net.http.HttpTimeoutException: upstream timeout details"));
        assertTrue(trace.contains("Caused by: java.net.ConnectException: connection cause"));
        assertTrue(trace.contains("  at "));
      } else assertNull(response.getBody().detalhes());
      assertEquals("no-store",response.getHeaders().getCacheControl());
    }
  }
  @Test void diagnosticTraceIsBoundedEvenWithCyclicCauses(){
    var first=new Exception("x".repeat(10000));var second=new Exception("second",first);first.initCause(second);
    var details=new LocalidadesUnavailableException(first,null).diagnosticDetails();
    assertTrue(details.size()<260);assertTrue(details.stream().allMatch(line->line.length()<=2048));
    assertEquals(1,details.stream().filter(line->line.contains("Caused by:")).count());
  }
  @Test void classifiesDnsTlsAndConnectFailuresWithoutMessages(){
    assertEquals("DNS",new LocalidadesUnavailableException(new java.net.UnknownHostException("secret"),null).diagnostic());
    assertEquals("TLS",new LocalidadesUnavailableException(new javax.net.ssl.SSLHandshakeException("secret"),null).diagnostic());
    assertEquals("CONNECT_TIMEOUT",new LocalidadesUnavailableException(new java.net.http.HttpConnectTimeoutException("secret"),null).diagnostic());
  }
}
