package com.vilaverde.erp.controller;

import com.vilaverde.erp.dto.devolucao.*;
import com.vilaverde.erp.service.DevolucaoFornecedorService;
import jakarta.validation.Valid;
import java.util.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/devolucoes-fornecedores")
@RequiredArgsConstructor
public class DevolucaoFornecedorController {

  private final DevolucaoFornecedorService service;

  @GetMapping
  public List<DevolucaoFornecedorResponseDTO> listar() {
    return service.listar();
  }

  @GetMapping("/{id}")
  public DevolucaoFornecedorResponseDTO buscar(@PathVariable UUID id) {
    return service.buscar(id);
  }

  @PostMapping
  public ResponseEntity<DevolucaoFornecedorResponseDTO> criar(
    @Valid @RequestBody DevolucaoFornecedorRequestDTO d
  ) {
    return ResponseEntity.status(HttpStatus.CREATED).body(service.criar(d));
  }

  @DeleteMapping("/{id}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void excluir(@PathVariable UUID id) {
    service.excluir(id);
  }
}
