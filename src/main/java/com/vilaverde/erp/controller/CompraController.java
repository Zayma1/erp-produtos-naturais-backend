package com.vilaverde.erp.controller;

import com.vilaverde.erp.dto.compra.*;
import com.vilaverde.erp.service.CompraService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import java.util.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/compras")
@RequiredArgsConstructor
public class CompraController {

  private final CompraService service;

  @Operation(operationId = "listarCompras")
  @GetMapping
  public List<CompraResponseDTO> listar() {
    return service.listar();
  }

  @Operation(operationId = "buscarCompraPorId")
  @GetMapping("/{id}")
  public CompraResponseDTO buscar(@PathVariable UUID id) {
    return service.buscar(id);
  }

  @Operation(operationId = "criarCompra")
  @PostMapping
  public ResponseEntity<CompraResponseDTO> criar(
    @Valid @RequestBody CompraRequestDTO d
  ) {
    return ResponseEntity.status(HttpStatus.CREATED).body(service.criar(d));
  }

  @Operation(operationId = "atualizarCompra")
  @PutMapping("/{id}")
  public CompraResponseDTO atualizar(
    @PathVariable UUID id,
    @Valid @RequestBody CompraRequestDTO d
  ) {
    return service.atualizar(id, d);
  }

  @Operation(operationId = "excluirCompra")
  @DeleteMapping("/{id}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void excluir(@PathVariable UUID id) {
    service.excluir(id);
  }
}
