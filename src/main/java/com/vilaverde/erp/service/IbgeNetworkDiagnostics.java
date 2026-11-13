package com.vilaverde.erp.service;

import java.net.*;
import java.net.http.*;
import java.time.*;
import java.util.*;
import java.util.concurrent.*;
import javax.net.ssl.*;
import org.springframework.stereotype.Service;

/** Operational probes with fixed destinations, no credentials or application data. */
@Service
public class IbgeNetworkDiagnostics {
  public record Check(String name,boolean success,long elapsedMs,List<String> details) {}
  public record Report(String state,String host,Instant startedAt,Instant finishedAt,List<Check> checks) {}
  private record Completed(List<Check> checks,Instant at) {}
  private FutureTask<Completed> task;
  private Thread worker;
  private Report report;
  protected URI endpoint(){return URI.create("https://servicodados.ibge.gov.br/api/v1/localidades/estados?orderBy=nome");}
  protected Instant now(){return Instant.now();}
  public synchronized Report inspect() {
    if(task==null || (task.isDone() && !worker.isAlive() && report.startedAt().plus(Duration.ofMinutes(5)).isBefore(now()))) {
      report=new Report("RUNNING",endpoint().getHost(),now(),null,List.of());
      task=new FutureTask<>(()->{var checks=checks();return new Completed(checks,now());});
      worker=Thread.ofVirtual().name("ibge-network-diagnostic").start(task);
    }
    if(!report.state().equals("RUNNING"))return report;
    if(task.isDone()) {
      try {var completed=task.get();report=new Report("COMPLETED",report.host(),report.startedAt(),completed.at(),List.copyOf(completed.checks()));}
      catch(Exception e){report=new Report("FAILED",report.host(),report.startedAt(),now(),List.of(new Check("DIAGNOSTIC",false,0,errors(e))));}
    } else if(report.startedAt().plusSeconds(60).isBefore(now())) {
      task.cancel(true);worker.interrupt();
      report=new Report("TIMED_OUT",report.host(),report.startedAt(),now(),List.of(new Check("DIAGNOSTIC",false,60000,List.of("Diagnostic exceeded 60 seconds"))));
    }
    return report;
  }
  @FunctionalInterface interface Probe {List<String> run() throws Exception;}
  private Check measure(String name,Probe probe) {
    long start=System.nanoTime();
    try{var details=probe.run();return new Check(name,true,(System.nanoTime()-start)/1000000,details);}
    catch(Exception e){if(e instanceof InterruptedException)Thread.currentThread().interrupt();return new Check(name,false,(System.nanoTime()-start)/1000000,errors(e));}
  }
  static List<String> errors(Throwable error) {
    var lines=new ArrayList<String>();var seen=Collections.newSetFromMap(new IdentityHashMap<Throwable,Boolean>());
    for(int depth=0;error!=null && depth<8 && seen.add(error);depth++,error=error.getCause()) {
      String line=error.getClass().getName()+": "+error.getMessage();lines.add(line.substring(0,Math.min(line.length(),2048)));
    }
    return List.copyOf(lines);
  }
  protected InetAddress[] resolve() throws Exception {
    var resolution=new FutureTask<InetAddress[]>(()->InetAddress.getAllByName(endpoint().getHost()));
    var thread=Thread.ofVirtual().name("ibge-dns-diagnostic").start(resolution);
    try{return resolution.get(3,TimeUnit.SECONDS);}finally{resolution.cancel(true);thread.interrupt();}
  }
  private int port(){return endpoint().getPort()>0?endpoint().getPort():443;}
  protected Check tcp(InetAddress ip) {
    return measure("TCP_"+ip.getHostAddress(),()->{
      try(var socket=new Socket()) {socket.connect(new InetSocketAddress(ip,port()),2000);return List.of("Connected to "+ip.getHostAddress()+":"+port());}
    });
  }
  protected Check tls(InetAddress ip,boolean alpn,boolean tls12) {
    String name=tls12?"TLS12_ALPN":alpn?"TLS_DEFAULT_ALPN":"TLS_DEFAULT_NO_ALPN";
    return measure(name,()->{
      try(var socket=new Socket()) {
        socket.connect(new InetSocketAddress(ip,port()),2000);socket.setSoTimeout(3000);
        try(var ssl=(SSLSocket)((SSLSocketFactory)SSLSocketFactory.getDefault()).createSocket(socket,endpoint().getHost(),port(),true)) {
          ssl.setSoTimeout(3000);var params=ssl.getSSLParameters();
          params.setEndpointIdentificationAlgorithm("HTTPS");
          if(alpn)params.setApplicationProtocols(new String[]{"h2","http/1.1"});
          if(tls12)params.setProtocols(new String[]{"TLSv1.2"});
          ssl.setSSLParameters(params);ssl.startHandshake();
          return List.of("IP="+ip.getHostAddress(),"protocol="+ssl.getSession().getProtocol(),"cipher="+ssl.getSession().getCipherSuite(),"ALPN="+ssl.getApplicationProtocol());
        }
      }
    });
  }
  protected Check http(HttpClient.Version version,boolean tls12) {
    return httpRequest(endpoint(),version.name()+(tls12?"_TLS12":"_DEFAULT_TLS"),version,tls12,true);
  }
  protected Check control(String name,URI target) {
    return httpRequest(target,name,HttpClient.Version.HTTP_1_1,false,false);
  }
  private Check httpRequest(URI target,String name,HttpClient.Version version,boolean tls12,boolean require200) {
    return measure(name,()->{
      var builder=HttpClient.newBuilder().version(version).connectTimeout(Duration.ofSeconds(3)).followRedirects(HttpClient.Redirect.NEVER);
      if(tls12){var params=new SSLParameters();params.setProtocols(new String[]{"TLSv1.2"});builder.sslParameters(params);}
      try(var client=builder.build()) {
        var response=client.send(HttpRequest.newBuilder(target).timeout(Duration.ofSeconds(6)).header("Accept","application/json").GET().build(),HttpResponse.BodyHandlers.discarding());
        var details=List.of("host="+target.getHost(),"status="+response.statusCode(),"version="+response.version(),"content-type="+response.headers().firstValue("Content-Type").orElse(""));
        if(require200 && response.statusCode()!=200)throw new java.io.IOException(String.join("; ",details));
        return details;
      }
    });
  }
  protected List<Check> checks() {
    var results=new ArrayList<Check>();var addresses=new ArrayList<InetAddress>();
    // Reachability controls: an HTTP response, including a denial/redirect, proves HTTPS worked.
    results.add(control("HTTPS_CONTROL_PUBLIC",URI.create("https://example.com/")));
    results.add(control("HTTPS_CONTROL_FRONTEND",URI.create("https://erp-produtos-naturais-frontend.vercel.app/login")));
    results.add(measure("DNS",()->{addresses.addAll(Arrays.stream(resolve()).distinct().limit(4).toList());return addresses.stream().map(ip->(ip instanceof Inet6Address?"IPv6=":"IPv4=")+ip.getHostAddress()).toList();}));
    InetAddress reachable=null;
    for(var ip:addresses){var result=tcp(ip);results.add(result);if(result.success() && reachable==null)reachable=ip;}
    if(reachable!=null) {
      results.add(tls(reachable,false,false));results.add(tls(reachable,true,false));results.add(tls(reachable,true,true));
    }
    results.add(http(HttpClient.Version.HTTP_2,false));
    results.add(http(HttpClient.Version.HTTP_1_1,false));
    results.add(http(HttpClient.Version.HTTP_1_1,true));
    return List.copyOf(results);
  }
}
