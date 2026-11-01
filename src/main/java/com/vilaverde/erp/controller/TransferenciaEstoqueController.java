package com.vilaverde.erp.controller;

import com.vilaverde.erp.dto.transferencia.*;
import com.vilaverde.erp.service.TransferenciaEstoqueService;
import jakarta.validation.Valid;
import java.util.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/transferencias-estoque")
@RequiredArgsConstructor
public class TransferenciaEstoqueController {

  private final TransferenciaEstoqueService service;

  @GetMapping
  public List<TransferenciaResponseDTO> listar() {
    return service.listar();
  }

  @GetMapping("/{id}")
  public TransferenciaResponseDTO buscar(@PathVariable UUID id) {
    return service.buscar(id);
  }

  @PostMapping
  public ResponseEntity<TransferenciaResponseDTO> criar(
    @Valid @RequestBody TransferenciaRequestDTO d
  ) {
    return ResponseEntity.status(HttpStatus.CREATED).body(service.criar(d));
  }

  @PostMapping("/{id}/concluir")
  public TransferenciaResponseDTO concluir(
    @PathVariable UUID id,
    @RequestParam(required = false) UUID idUsuario
  ) {
    return service.concluir(id, idUsuario);
  }
}
