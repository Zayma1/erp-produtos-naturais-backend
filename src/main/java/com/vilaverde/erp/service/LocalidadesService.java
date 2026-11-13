package com.vilaverde.erp.service;
import java.util.*;
import java.time.*;
import java.net.*;
import java.net.http.*;
import java.io.IOException;
import org.springframework.stereotype.Service;
import com.vilaverde.erp.exception.InvalidInputException;
import tools.jackson.databind.ObjectMapper;
@Service
public class LocalidadesService {
  public record Place(int id,String nome,String sigla) {}
  public record Catalog(List<Place> dados,boolean desatualizado,Instant atualizadoEm) {}
  public record Address(String uf,String nome,Integer id) {}
  private static final org.slf4j.Logger log=org.slf4j.LoggerFactory.getLogger(LocalidadesService.class);
  private final Map<String,Catalog> cache=new HashMap<>();
  private final HttpClient http=HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(5)).followRedirects(HttpClient.Redirect.NEVER).build();
  private final ObjectMapper json=new ObjectMapper();
  @org.springframework.beans.factory.annotation.Value("${IBGE_RELAY_ORIGIN:}")
  private String configuredRelayOrigin="";
  private volatile Instant preferRelayUntil=Instant.MIN;
  protected URI relayEndpoint(String path) {
    if(!path.matches("estados(?:/[A-Z]{2}/municipios)?"))return null;
    String origin=configuredRelayOrigin.strip();
    if(origin.equalsIgnoreCase("disabled"))return null;
    if(origin.isEmpty()) {
      // Automatic fallback is scoped to this deployment, never to an incoming Host header.
      if(!"true".equals(System.getenv("RENDER")) || !"erp-produtos-naturais-backend.onrender.com".equals(System.getenv("RENDER_EXTERNAL_HOSTNAME")))return null;
      origin="https://erp-produtos-naturais-frontend.vercel.app";
    }
    URI base=URI.create(origin);
    if(!"https".equals(base.getScheme()) || base.getHost()==null || base.getUserInfo()!=null || base.getQuery()!=null || base.getFragment()!=null || !(base.getPath().isEmpty() || base.getPath().equals("/")))
      throw new IllegalArgumentException("IBGE_RELAY_ORIGIN deve ser uma origem HTTPS fixa");
    return base.resolve("/api/localidades/ibge/"+path);
  }
  protected String relaySession() {
    var attributes=org.springframework.web.context.request.RequestContextHolder.getRequestAttributes();
    if(!(attributes instanceof org.springframework.web.context.request.ServletRequestAttributes servlet))return null;
    String cookie=servlet.getRequest().getHeader("Cookie");if(cookie==null)return null;
    return Arrays.stream(cookie.split(";")).map(String::strip).filter(v->v.matches("SESSION=[A-Za-z0-9%_+/=.-]+") && v.length()>8).findFirst().orElse(null);
  }
  // Test subclasses may replace the endpoint without exposing a configurable upstream in production.
  protected URI endpoint(String path) {
    return URI.create("https://servicodados.ibge.gov.br/api/v1/localidades/"+path+"?orderBy=nome");
  }
  protected Duration requestBudget() {return Duration.ofSeconds(6);}
  private static final class UpstreamStatusException extends IOException {
    final int status;
    final boolean transientFailure;
    UpstreamStatusException(int status,boolean retryPermitted) {
      super("IBGE respondeu HTTP "+status);
      this.status=status;
      this.transientFailure=retryPermitted && (status==429 || status==500 || status==502 || status==503 || status==504);
    }
    boolean transientFailure() {return transientFailure;}
  }
  protected String fetch(String path) throws Exception {
    // Both attempts share six seconds. Even a cold states + municipalities lookup stays below the 15-second proxy timeout.
    long deadline=System.nanoTime()+requestBudget().toNanos();
    String session=relaySession();URI relay=session==null?null:relayEndpoint(path);
    boolean relayFirst=relay!=null && preferRelayUntil.isAfter(now());
    for(int attempt=0;;attempt++) {
      long remaining=deadline-System.nanoTime();
      if(remaining<=0)throw new HttpTimeoutException("Tempo limite da consulta ao IBGE esgotado");
      try {
        boolean viaRelay=relay!=null && (attempt==0?relayFirst:!relayFirst);
        // Reserve half the shared budget for the alternative network path when available.
        long timeout=relay!=null && attempt==0?Math.max(1,remaining/2):remaining;
        var request=HttpRequest.newBuilder(viaRelay?relay:endpoint(path)).timeout(Duration.ofNanos(timeout))
          .header("Accept","application/json").GET();
        if(viaRelay)request.header("Cookie",session);
        var response=http.send(request.build(),HttpResponse.BodyHandlers.ofString());
        if(response.statusCode()!=200) {
          // Do not retry immediately when IBGE explicitly requests a delay.
          boolean retryPermitted=response.headers().firstValue("Retry-After").map("0"::equals).orElse(true);
          throw new UpstreamStatusException(response.statusCode(),retryPermitted);
        }
        preferRelayUntil=viaRelay?now().plus(Duration.ofHours(1)):Instant.MIN;
        return response.body();
      }catch(InterruptedException e) {
        Thread.currentThread().interrupt();throw e;
      }catch(IOException e) {
        if(attempt>=1 || System.nanoTime()>=deadline ||
          (e instanceof UpstreamStatusException status && !status.transientFailure()))throw e;
      }
    }
  }
  protected Instant now() {return Instant.now();}
  public synchronized Catalog catalog(String path) {
    var previous=cache.get(path);var now=now();
    if(previous!=null && previous.atualizadoEm().plus(Duration.ofHours(24)).isAfter(now))return previous;
    try {
      var root=json.readTree(fetch(path));if(!root.isArray() || root.isEmpty())throw new IllegalStateException("Resposta IBGE não contém uma lista de localidades");
      var places=new ArrayList<Place>();
      for(var item:root) {
        if(!item.has("id") || !item.has("nome"))throw new IllegalStateException("Localidade IBGE sem identificador ou nome");
        places.add(new Place(item.get("id").asInt(),item.get("nome").asText(),item.has("sigla")?item.get("sigla").asText():null));
      }
      var value=new Catalog(List.copyOf(places),false,now);cache.put(path,value);return value;
    }catch(Exception e) {
      boolean usable=previous!=null && previous.atualizadoEm().plus(Duration.ofDays(7)).isAfter(now);
      // Log only the public locality path and exception; never the relay's session Cookie header.
      log.warn("Falha na consulta IBGE: recurso={}, cacheUtilizavel={}",path,usable,e);
      if(usable)return new Catalog(previous.dados(),true,previous.atualizadoEm());
      throw new com.vilaverde.erp.exception.LocalidadesUnavailableException(e,e instanceof UpstreamStatusException status?"HTTP_"+status.status:null);
    }
  }
  public Catalog estados() {return catalog("estados");}
  public Catalog municipios(String uf) {
    if(uf==null || estados().dados().stream().noneMatch(p->uf.equals(p.sigla())))throw new InvalidInputException("UF inválida");
    return catalog("estados/"+uf+"/municipios");
  }
  public Address validate(String uf,String city,Integer code,String oldUf,String oldCity,Integer oldCode) {
    if(code==null && oldCode==null && Objects.equals(uf,oldUf) && Objects.equals(city,oldCity))return new Address(uf,city,null);
    if(code==null)throw new InvalidInputException("Selecione o município no catálogo IBGE");
    var place=municipios(uf).dados().stream().filter(p->p.id()==code).findFirst().orElseThrow(()->new InvalidInputException("Município incompatível com a UF"));
    return new Address(uf,place.nome(),code);
  }
}
