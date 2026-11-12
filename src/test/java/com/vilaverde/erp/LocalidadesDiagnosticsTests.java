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
  @Test void onlyAdminReceivesBoundedDiagnosticAndNoExceptionDetails(){
    var error=new LocalidadesUnavailableException(new HttpTimeoutException("sensitive upstream details"),null);
    for(String role:new String[]{"", "ROLE_FUNC","ROLE_GER","ROLE_ADMIN"}) {
      SecurityContextHolder.clearContext();
      if(!role.isEmpty())SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken("user","unused",AuthorityUtils.createAuthorityList(role)));
      var response=new GlobalExceptionHandler().unavailable(error);
      assertEquals(503,response.getStatusCode().value());
      assertEquals(role.equals("ROLE_ADMIN")?"REQUEST_TIMEOUT":null,response.getHeaders().getFirst("X-IBGE-Failure"));
      assertFalse(response.getBody().toString().contains("sensitive"));
      assertEquals("no-store",response.getHeaders().getCacheControl());
    }
  }
  @Test void classifiesDnsTlsAndConnectFailuresWithoutMessages(){
    assertEquals("DNS",new LocalidadesUnavailableException(new java.net.UnknownHostException("secret"),null).diagnostic());
    assertEquals("TLS",new LocalidadesUnavailableException(new javax.net.ssl.SSLHandshakeException("secret"),null).diagnostic());
    assertEquals("CONNECT_TIMEOUT",new LocalidadesUnavailableException(new java.net.http.HttpConnectTimeoutException("secret"),null).diagnostic());
  }
}
