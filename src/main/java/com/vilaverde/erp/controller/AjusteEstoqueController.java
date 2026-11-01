package com.vilaverde.erp.controller;

import com.vilaverde.erp.dto.ajuste.*;
import com.vilaverde.erp.service.AjusteEstoqueService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import java.util.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/ajustes-estoque")
@RequiredArgsConstructor
public class AjusteEstoqueController {

  private final AjusteEstoqueService service;

  @Operation(operationId = "listarAjustesEstoque")
  @GetMapping
  public List<AjusteResponseDTO> listar() {
    return service.listar();
  }

  @Operation(operationId = "buscarAjusteEstoquePorId")
  @GetMapping("/{id}")
  public AjusteResponseDTO buscar(@PathVariable UUID id) {
    return service.buscar(id);
  }

  @Operation(operationId = "criarAjusteEstoque")
  @PostMapping
  public ResponseEntity<AjusteResponseDTO> criar(
    @Valid @RequestBody AjusteRequestDTO d
  ) {
    return ResponseEntity.status(HttpStatus.CREATED).body(service.criar(d));
  }

  @Operation(operationId = "concluirAjusteEstoque")
  @PostMapping("/{id}/concluir")
  public AjusteResponseDTO concluir(@PathVariable UUID id) {
    return service.concluir(id);
  }
}
