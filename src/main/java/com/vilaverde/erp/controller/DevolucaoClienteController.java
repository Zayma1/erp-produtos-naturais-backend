package com.vilaverde.erp.controller;

import com.vilaverde.erp.dto.devolucao.*;
import com.vilaverde.erp.service.DevolucaoClienteService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import java.util.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/devolucoes-clientes")
@RequiredArgsConstructor
public class DevolucaoClienteController {

  private final DevolucaoClienteService service;

  @Operation(operationId = "listarDevolucoesClientes")
  @GetMapping
  public List<DevolucaoClienteResponseDTO> listar() {
    return service.listar();
  }

  @Operation(operationId = "buscarDevolucaoClientePorId")
  @GetMapping("/{id}")
  public DevolucaoClienteResponseDTO buscar(@PathVariable UUID id) {
    return service.buscar(id);
  }

  @Operation(operationId = "criarDevolucaoCliente")
  @PostMapping
  public ResponseEntity<DevolucaoClienteResponseDTO> criar(
    @Valid @RequestBody DevolucaoClienteRequestDTO d
  ) {
    return ResponseEntity.status(HttpStatus.CREATED).body(service.criar(d));
  }

  @Operation(operationId = "excluirDevolucaoCliente")
  @DeleteMapping("/{id}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void excluir(@PathVariable UUID id) {
    service.excluir(id);
  }
}
