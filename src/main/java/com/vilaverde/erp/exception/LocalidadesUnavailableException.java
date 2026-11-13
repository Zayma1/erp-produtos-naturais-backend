package com.vilaverde.erp.exception;

import java.net.*;
import java.net.http.*;
import javax.net.ssl.SSLException;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

/** Diagnostic details are returned only to administrators by the exception handler. */
public class LocalidadesUnavailableException extends ResponseStatusException {
  private final String diagnostic;
  public LocalidadesUnavailableException(Exception cause,String upstreamCode) {
    super(HttpStatus.SERVICE_UNAVAILABLE,"Catálogo IBGE indisponível. Tente atualizar o campo.",cause);
    diagnostic=upstreamCode==null?classify(cause):upstreamCode;
  }
  public String diagnostic(){return diagnostic;}
  public java.util.List<String> diagnosticDetails() {
    var lines=new java.util.ArrayList<String>();
    lines.add("IBGE diagnostic: "+diagnostic);
    lines.add("Java: "+System.getProperty("java.version"));
    lines.add("preferIPv4Stack="+System.getProperty("java.net.preferIPv4Stack","false")+
      "; preferIPv6Addresses="+System.getProperty("java.net.preferIPv6Addresses","false"));
    // Include the original cause chain and frames, bounded even for nested or cyclic exceptions.
    var seen=java.util.Collections.newSetFromMap(new java.util.IdentityHashMap<Throwable,Boolean>());
    Throwable cause=getCause();
    for(int depth=0;cause!=null && depth<12 && seen.add(cause);depth++,cause=cause.getCause()) {
      String description=(depth==0?"":"Caused by: ")+cause.getClass().getName()+": "+cause.getMessage();
      lines.add(description.substring(0,Math.min(description.length(),2048)));
      var frames=cause.getStackTrace();
      for(int i=0;i<Math.min(frames.length,20);i++)lines.add("  at "+frames[i]);
      if(frames.length>20)lines.add("  ... "+(frames.length-20)+" more frames");
    }
    return java.util.List.copyOf(lines);
  }
  private static String classify(Throwable cause) {
    for(int depth=0;cause!=null && depth<16;depth++,cause=cause.getCause()) {
      if(cause instanceof UnknownHostException)return "DNS";
      if(cause instanceof SSLException)return "TLS";
      if(cause instanceof HttpConnectTimeoutException)return "CONNECT_TIMEOUT";
      if(cause instanceof HttpTimeoutException)return "REQUEST_TIMEOUT";
      if(cause instanceof ConnectException)return "CONNECTION";
      if(cause instanceof InterruptedException)return "INTERRUPTED";
    }
    return "INVALID_RESPONSE_OR_IO";
  }
}
