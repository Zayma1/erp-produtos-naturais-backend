package com.vilaverde.erp.service;
import org.junit.jupiter.api.*;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.context.request.*;
import static org.junit.jupiter.api.Assertions.*;
class LocalidadesRelayConfigurationTests {
  @AfterEach void clear(){RequestContextHolder.resetRequestAttributes();}
  @Test void usesOnlyConfiguredHttpsOriginAndAllowedCatalogPaths(){
    var service=new LocalidadesService();ReflectionTestUtils.setField(service,"configuredRelayOrigin","https://trusted.test/");
    assertEquals("https://trusted.test/api/localidades/ibge/estados",service.relayEndpoint("estados").toString());
    assertEquals("https://trusted.test/api/localidades/ibge/estados/SC/municipios",service.relayEndpoint("estados/SC/municipios").toString());
    assertNull(service.relayEndpoint("../evil"));
    for(String origin:new String[]{"http://evil.test","https://user:pass@trusted.test","https://trusted.test/path","https://trusted.test?query=yes"}) {
      ReflectionTestUtils.setField(service,"configuredRelayOrigin",origin);assertThrows(IllegalArgumentException.class,()->service.relayEndpoint("estados"));
    }
    ReflectionTestUtils.setField(service,"configuredRelayOrigin","disabled");assertNull(service.relayEndpoint("estados"));
  }
  @Test void forwardsOnlyOriginalWireSessionCookieToTheTrustedRelay(){
    var service=new LocalidadesService();assertNull(service.relaySession());
    var request=new MockHttpServletRequest();request.addHeader("Cookie","tracking=private; SESSION=encoded%3D; other=private");
    RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));assertEquals("SESSION=encoded%3D",service.relaySession());
    request.removeHeader("Cookie");request.addHeader("Cookie","SESSION=bad value; tracking=private");assertNull(service.relaySession());
  }
}
