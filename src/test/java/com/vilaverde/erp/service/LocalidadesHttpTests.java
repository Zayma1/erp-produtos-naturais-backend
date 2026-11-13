package com.vilaverde.erp.service;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.net.*;
import java.net.http.HttpTimeoutException;
import java.nio.charset.StandardCharsets;
import java.time.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.*;
import org.springframework.web.server.ResponseStatusException;
import static org.junit.jupiter.api.Assertions.*;

class LocalidadesHttpTests {
  static final String STATES="[{\"id\":42,\"sigla\":\"SC\",\"nome\":\"Santa Catarina\",\"regiao\":{\"id\":4,\"nome\":\"Sul\"}}]";
  HttpServer server;
  ExecutorService executor;
  final AtomicInteger requests=new AtomicInteger();
  interface Handler {void handle(HttpExchange exchange,int attempt) throws Exception;}
  Handler handler;
  @BeforeEach void start() throws IOException {
    requests.set(0);server=HttpServer.create(new InetSocketAddress("127.0.0.1",0),0);
    executor=Executors.newVirtualThreadPerTaskExecutor();server.setExecutor(executor);
    server.createContext("/",exchange->{try{handler.handle(exchange,requests.incrementAndGet());}catch(InterruptedException e){Thread.currentThread().interrupt();}catch(Exception e){throw new IOException(e);}finally{exchange.close();}});
    server.start();
  }
  @AfterEach void stop(){server.stop(0);executor.shutdownNow();}
  class Client extends LocalidadesService {
    Instant clock=Instant.parse("2026-11-11T00:00:00Z");
    Duration budget=Duration.ofSeconds(5);
    @Override protected URI endpoint(String path){return URI.create("http://127.0.0.1:"+server.getAddress().getPort()+"/"+path+"?orderBy=nome");}
    @Override protected Duration requestBudget(){return budget;}
    @Override protected Instant now(){return clock;}
  }
  class RelayClient extends Client {
    @Override protected URI relayEndpoint(String path){return URI.create("http://127.0.0.1:"+server.getAddress().getPort()+"/relay/"+path);}
    @Override protected String relaySession(){return "SESSION=controlled";}
  }
  @Test void relayIsUsedAfterTransientFailureAndPreferredForTheNextCatalog() {
    handler=(exchange,n)->{
      boolean relay=exchange.getRequestURI().getPath().startsWith("/relay/");
      assertEquals(relay?"SESSION=controlled":null,exchange.getRequestHeaders().getFirst("Cookie"));
      reply(exchange,relay?200:503,relay?STATES:"{}");
    };
    var client=new RelayClient();assertEquals(1,client.estados().dados().size());assertEquals(2,requests.get());
    client.catalog("estados/SC/municipios");assertEquals(3,requests.get());
    handler=(exchange,n)->reply(exchange,exchange.getRequestURI().getPath().startsWith("/relay/")?503:200,STATES);
    client.catalog("estados/SP/municipios");assertEquals(5,requests.get());
    client.catalog("estados/RJ/municipios");assertEquals(6,requests.get());
  }
  @Test void permanentStatusAndRetryAfterDoNotTriggerTheRelay() {
    for(int status:new int[]{403,429}) {
      requests.set(0);handler=(exchange,n)->{assertFalse(exchange.getRequestURI().getPath().startsWith("/relay/"));if(status==429)exchange.getResponseHeaders().set("Retry-After","120");reply(exchange,status,"{}");};
      assertThrows(ResponseStatusException.class,()->new RelayClient().estados());assertEquals(1,requests.get());
    }
  }
  @Test void relayWithoutARequestSessionIsNeverCalled() {
    handler=(exchange,n)->{assertFalse(exchange.getRequestURI().getPath().startsWith("/relay/"));assertNull(exchange.getRequestHeaders().getFirst("Cookie"));reply(exchange,n==1?503:200,STATES);};
    var client=new RelayClient(){@Override protected String relaySession(){return null;}};
    assertEquals(1,client.estados().dados().size());assertEquals(2,requests.get());
  }
  @Test void slowDirectAttemptLeavesTimeForRelayWithinOneBudget() {
    handler=(exchange,n)->reply(exchange,200,STATES);var client=new RelayClient();client.estados();
    handler=(exchange,n)->{if(!exchange.getRequestURI().getPath().startsWith("/relay/"))Thread.sleep(2000);reply(exchange,200,STATES);};
    client.budget=Duration.ofSeconds(1);long start=System.nanoTime();
    assertEquals(1,client.catalog("estados/SC/municipios").dados().size());
    assertTrue(System.nanoTime()-start<Duration.ofMillis(1500).toNanos());assertEquals(3,requests.get());
  }
  void reply(HttpExchange exchange,int status,String body) throws IOException {
    byte[] bytes=body.getBytes(StandardCharsets.UTF_8);exchange.getResponseHeaders().set("Content-Type","application/json; charset=utf-8");
    exchange.sendResponseHeaders(status,bytes.length);exchange.getResponseBody().write(bytes);
  }
  @Test void transientStatusIsRetriedAndTheSuccessfulCatalogIsCached() {
    for(int status:new int[]{429,500,502,503,504}) {
      requests.set(0);
      handler=(exchange,n)->{assertEquals("application/json",exchange.getRequestHeaders().getFirst("Accept"));reply(exchange,n==1?status:200,n==1?"{}":STATES);};
      var client=new Client();assertEquals("SC",client.estados().dados().getFirst().sigla());assertEquals(2,requests.get());
      client.estados();assertEquals(2,requests.get(),"Successful retries must populate the normal cache");
    }
  }
  @Test void permanentStatusIsNotRetriedAndTheCauseIsLogged() {
    var logger=(ch.qos.logback.classic.Logger)org.slf4j.LoggerFactory.getLogger(LocalidadesService.class);
    var messages=new ch.qos.logback.core.read.ListAppender<ch.qos.logback.classic.spi.ILoggingEvent>();messages.start();logger.addAppender(messages);
    try {
    handler=(exchange,n)->reply(exchange,403,"{}");
    var failure=assertThrows(ResponseStatusException.class,()->new Client().estados());
    assertEquals(503,failure.getStatusCode().value());assertEquals(1,requests.get());
    assertTrue(failure.getCause().getMessage().contains("HTTP 403"));assertTrue(messages.list.getFirst().getFormattedMessage().contains("recurso=estados"));assertTrue(messages.list.getFirst().getThrowableProxy().getMessage().contains("HTTP 403"));
    assertEquals("Catálogo IBGE indisponível. Tente atualizar o campo.",failure.getReason());
    } finally {logger.detachAppender(messages);messages.stop();}
  }
  @Test void repeatedFailureKeepsStaleCacheAndDoesNotRenewItsTimestamp() {
    handler=(exchange,n)->reply(exchange,200,STATES);var client=new Client();var original=client.estados();
    client.clock=client.clock.plus(Duration.ofHours(25));handler=(exchange,n)->reply(exchange,502,"{}");
    var stale=client.estados();assertTrue(stale.desatualizado());assertEquals(original.dados(),stale.dados());assertEquals(original.atualizadoEm(),stale.atualizadoEm());assertEquals(3,requests.get());
    client.clock=original.atualizadoEm().plus(Duration.ofDays(7));assertThrows(ResponseStatusException.class,client::estados);assertEquals(5,requests.get());
  }
  @Test void timeoutUsesOneSharedBudgetAndPreservesItsCause() {
    handler=(exchange,n)->reply(exchange,200,STATES);
    var client=new Client();client.estados();client.clock=client.clock.plus(Duration.ofDays(8));
    handler=(exchange,n)->{Thread.sleep(2000);reply(exchange,200,STATES);};
    client.budget=Duration.ofMillis(500);
    long start=System.nanoTime();var failure=assertThrows(ResponseStatusException.class,client::estados);
    assertInstanceOf(HttpTimeoutException.class,failure.getCause());assertTrue(System.nanoTime()-start<Duration.ofSeconds(2).toNanos());assertEquals(2,requests.get());
  }
  @Test void invalidJsonDoesNotPopulateTheCacheAndTheNextRequestCanRecover() {
    handler=(exchange,n)->reply(exchange,200,n==1?"{\"message\":\"not a catalog\"}":STATES);
    var client=new Client();assertThrows(ResponseStatusException.class,client::estados);assertEquals(1,requests.get());
    assertEquals(1,client.estados().dados().size());assertEquals(2,requests.get());
  }
  @Test void realTransportParsesMunicipalityNamesAndReusesTheStateCache() {
    handler=(exchange,n)->reply(exchange,200,exchange.getRequestURI().getPath().endsWith("municipios")?"[{\"id\":4204608,\"nome\":\"Criciúma\"}]":STATES);
    var client=new Client();client.estados();assertEquals("Criciúma",client.validate("SC","ignored",4204608,null,null,null).nome());assertEquals(2,requests.get());
  }
  @Test void retryAfterDoesNotCauseAnImmediateRetryOrAnUnboundedWait() {
    handler=(exchange,n)->{exchange.getResponseHeaders().set("Retry-After","120");reply(exchange,503,"{}");};
    var failure=assertThrows(ResponseStatusException.class,()->new Client().estados());
    assertEquals(1,requests.get());assertTrue(failure.getCause().getMessage().contains("HTTP 503"));
  }
}
