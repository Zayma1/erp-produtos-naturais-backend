package com.vilaverde.erp.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.servers.Server;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(
  info = @Info(
    title = "ERP Produtos Naturais API",
    version = "1.0.0",
    description = "API do ERP de produtos naturais"
  ),
  servers = {
    @Server(url = "http://localhost:8080", description = "Ambiente local"),
  }
)
public class OpenApiConfig {}
