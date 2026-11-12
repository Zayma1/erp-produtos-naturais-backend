package com.vilaverde.erp.exception;

import java.net.*;
import java.net.http.*;
import javax.net.ssl.SSLException;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

/** Fixed diagnostic codes only: never expose exception messages or upstream response bodies. */
public class LocalidadesUnavailableException extends ResponseStatusException {
  private final String diagnostic;
  public LocalidadesUnavailableException(Exception cause,String upstreamCode) {
    super(HttpStatus.SERVICE_UNAVAILABLE,"Catálogo IBGE indisponível. Tente atualizar o campo.",cause);
    diagnostic=upstreamCode==null?classify(cause):upstreamCode;
  }
  public String diagnostic(){return diagnostic;}
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
