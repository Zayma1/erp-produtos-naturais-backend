package com.vilaverde.erp.controller;

import com.vilaverde.erp.dto.recebimento.*;
import com.vilaverde.erp.service.RecebimentoCompraService;
import jakarta.validation.Valid;
import java.util.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/recebimentos-compras")
@RequiredArgsConstructor
public class RecebimentoCompraController {

  private final RecebimentoCompraService service;

  @GetMapping
  public List<RecebimentoCompraResponseDTO> listar() {
    return service.listar();
  }

  @GetMapping("/{id}")
  public RecebimentoCompraResponseDTO buscar(@PathVariable UUID id) {
    return service.buscar(id);
  }

  @PostMapping
  public ResponseEntity<RecebimentoCompraResponseDTO> criar(
    @Valid @RequestBody RecebimentoCompraRequestDTO d
  ) {
    return ResponseEntity.status(HttpStatus.CREATED).body(service.criar(d));
  }

  @PutMapping("/{id}")
  public RecebimentoCompraResponseDTO atualizar(
    @PathVariable UUID id,
    @Valid @RequestBody RecebimentoCompraRequestDTO d
  ) {
    return service.atualizar(id, d);
  }

  @DeleteMapping("/{id}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void excluir(@PathVariable UUID id) {
    service.excluir(id);
  }
}
