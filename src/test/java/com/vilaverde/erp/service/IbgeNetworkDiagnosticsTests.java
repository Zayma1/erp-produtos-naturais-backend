package com.vilaverde.erp.service;
import java.net.*;
import java.net.http.HttpClient;
import java.time.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;
class IbgeNetworkDiagnosticsTests {
  @Test void asynchronousRunIsSharedCachedAndCanRefreshAfterFiveMinutes() throws Exception {
    var gate=new CountDownLatch(1);var calls=new AtomicInteger();
    class Probe extends IbgeNetworkDiagnostics {
      Instant time=Instant.parse("2026-11-13T00:00:00Z");
      @Override protected Instant now(){return time;}
      @Override protected List<Check> checks(){calls.incrementAndGet();try{gate.await();}catch(InterruptedException e){Thread.currentThread().interrupt();}return List.of(new Check("controlled",true,1,List.of("ok")));}
    }
    var probe=new Probe();assertEquals("RUNNING",probe.inspect().state());
    for(int i=0;i<10;i++)assertEquals("RUNNING",probe.inspect().state());
    gate.countDown();var finished=await(probe);assertEquals(1,calls.get());assertEquals("COMPLETED",finished.state());
    assertEquals(finished,probe.inspect());probe.time=probe.time.plusSeconds(301);
    long refreshDeadline=System.nanoTime()+TimeUnit.SECONDS.toNanos(5);
    while(calls.get()<2 && System.nanoTime()<refreshDeadline){probe.inspect();Thread.sleep(10);}
    await(probe);assertEquals(2,calls.get());
  }
  static IbgeNetworkDiagnostics.Report await(IbgeNetworkDiagnostics probe) throws InterruptedException {
    long deadline=System.nanoTime()+TimeUnit.SECONDS.toNanos(5);IbgeNetworkDiagnostics.Report report;
    do{report=probe.inspect();if(!report.state().equals("RUNNING"))return report;Thread.sleep(10);}while(System.nanoTime()<deadline);
    throw new AssertionError("Diagnostic did not finish");
  }
  @Test void internalFailureAndDeadlineAreReportedInsteadOfHoldingTheHttpRequest() throws Exception {
    var failed=new IbgeNetworkDiagnostics(){@Override protected List<Check> checks(){throw new IllegalStateException("controlled failure");}};
    assertEquals("FAILED",await(failed).state());assertTrue(failed.inspect().checks().getFirst().details().toString().contains("controlled failure"));
    var gate=new CountDownLatch(1);
    class Probe extends IbgeNetworkDiagnostics {
      Instant time=Instant.parse("2026-11-13T00:00:00Z");
      @Override protected Instant now(){return time;}
      @Override protected List<Check> checks(){try{gate.await();}catch(InterruptedException e){Thread.currentThread().interrupt();}return List.of();}
    }
    var timed=new Probe();timed.inspect();timed.time=timed.time.plusSeconds(61);assertEquals("TIMED_OUT",timed.inspect().state());gate.countDown();
  }
  @Test void realTcpAndHttpProbesReportControlledTransportAndElapsedTime() throws Exception {
    var server=HttpServer.create(new InetSocketAddress("127.0.0.1",0),0);
    server.createContext("/",exchange->{try{try{Thread.sleep(40);}catch(InterruptedException e){Thread.currentThread().interrupt();}exchange.getResponseHeaders().set("Content-Type","application/json");byte[] body="[]".getBytes();exchange.sendResponseHeaders(200,body.length);exchange.getResponseBody().write(body);}finally{exchange.close();}});
    server.start();
    try {
      var probe=new IbgeNetworkDiagnostics(){@Override protected URI endpoint(){return URI.create("http://127.0.0.1:"+server.getAddress().getPort()+"/");}};
      assertTrue(probe.tcp(InetAddress.getByName("127.0.0.1")).success());
      var response=probe.http(HttpClient.Version.HTTP_1_1,false);assertTrue(response.success(),response.details().toString());assertTrue(response.elapsedMs()>=30);assertTrue(response.details().contains("status=200"));assertTrue(response.details().contains("version=HTTP_1_1"));
    }finally{server.stop(0);}
  }
}
