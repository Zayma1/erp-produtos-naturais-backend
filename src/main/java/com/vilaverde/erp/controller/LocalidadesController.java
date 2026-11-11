package com.vilaverde.erp.controller;
import com.vilaverde.erp.service.LocalidadesService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.Operation;
@RestController @RequestMapping("/api/localidades") @RequiredArgsConstructor
@io.swagger.v3.oas.annotations.responses.ApiResponses({@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode="503",description="IBGE indisponível sem cache utilizável",content=@io.swagger.v3.oas.annotations.media.Content(schema=@io.swagger.v3.oas.annotations.media.Schema(implementation=com.vilaverde.erp.exception.ApiError.class))),@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode="200",description="Catálogo IBGE com indicação de cache",content=@io.swagger.v3.oas.annotations.media.Content(schema=@io.swagger.v3.oas.annotations.media.Schema(implementation=LocalidadesService.Catalog.class)))})
public class LocalidadesController {
  private final LocalidadesService service;
  @GetMapping("/estados") @Operation(operationId="listarEstados") public LocalidadesService.Catalog states(){return service.estados();}
  @GetMapping("/estados/{uf}/municipios") @Operation(operationId="listarMunicipios") public LocalidadesService.Catalog cities(@PathVariable String uf){return service.municipios(uf);}
}
