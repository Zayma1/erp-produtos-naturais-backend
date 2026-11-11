package com.vilaverde.erp.controller;
import com.vilaverde.erp.service.LocalidadesService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.Operation;
@RestController @RequestMapping("/api/localidades") @RequiredArgsConstructor
public class LocalidadesController {
  private final LocalidadesService service;
  @GetMapping("/estados") @Operation(operationId="listarEstados") public LocalidadesService.Catalog states(){return service.estados();}
  @GetMapping("/estados/{uf}/municipios") @Operation(operationId="listarMunicipios") public LocalidadesService.Catalog cities(@PathVariable String uf){return service.municipios(uf);}
}
