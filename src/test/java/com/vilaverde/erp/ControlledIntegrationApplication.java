package com.vilaverde.erp;
/** Explicit test-only entrypoint: the production artifact never includes this class. */
public final class ControlledIntegrationApplication {
  public static void main(String[] args) {
    if(!"1".equals(System.getenv("CATALOG_DISPOSABLE")))throw new IllegalStateException("Disposable environment required");
    org.springframework.boot.SpringApplication.run(new Class<?>[]{ErpApplication.class,ControlledIntegrationConfig.class},args);
  }
}
