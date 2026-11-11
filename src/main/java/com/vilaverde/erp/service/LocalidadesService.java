package com.vilaverde.erp.service;
import java.util.*;
import java.time.*;
import java.net.*;
import java.net.http.*;
import org.springframework.stereotype.Service;
import com.vilaverde.erp.exception.InvalidInputException;
import tools.jackson.databind.ObjectMapper;
@Service
public class LocalidadesService {
  public record Place(int id,String nome,String sigla) {}
  public record Catalog(List<Place> dados,boolean desatualizado,Instant atualizadoEm) {}
  public record Address(String uf,String nome,Integer id) {}
  private final Map<String,Catalog> cache=new HashMap<>();
  private final HttpClient http=HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(3)).followRedirects(HttpClient.Redirect.NEVER).build();
  private final ObjectMapper json=new ObjectMapper();
  protected String fetch(String path) throws Exception {
    var response=http.send(HttpRequest.newBuilder(URI.create("https://servicodados.ibge.gov.br/api/v1/localidades/"+path+"?orderBy=nome")).timeout(Duration.ofSeconds(5)).GET().build(),HttpResponse.BodyHandlers.ofString());
    if(response.statusCode()!=200)throw new IllegalStateException();return response.body();
  }
  protected Instant now() {return Instant.now();}
  public synchronized Catalog catalog(String path) {
    var previous=cache.get(path);var now=now();
    if(previous!=null && previous.atualizadoEm().plus(Duration.ofHours(24)).isAfter(now))return previous;
    try {
      var root=json.readTree(fetch(path));if(!root.isArray() || root.isEmpty())throw new IllegalStateException();
      var places=new ArrayList<Place>();
      for(var item:root) {
        if(!item.has("id") || !item.has("nome"))throw new IllegalStateException();
        places.add(new Place(item.get("id").asInt(),item.get("nome").asText(),item.has("sigla")?item.get("sigla").asText():null));
      }
      var value=new Catalog(List.copyOf(places),false,now);cache.put(path,value);return value;
    }catch(Exception e) {
      if(previous!=null && previous.atualizadoEm().plus(Duration.ofDays(7)).isAfter(now))return new Catalog(previous.dados(),true,previous.atualizadoEm());
      throw new org.springframework.web.server.ResponseStatusException(org.springframework.http.HttpStatus.SERVICE_UNAVAILABLE,"Catálogo IBGE indisponível. Tente atualizar o campo.");
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
