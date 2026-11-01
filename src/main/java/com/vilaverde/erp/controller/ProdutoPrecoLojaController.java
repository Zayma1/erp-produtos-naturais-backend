package com.vilaverde.erp.controller;

import com.vilaverde.erp.dto.produto.*;
import com.vilaverde.erp.service.ProdutoPrecoLojaService;
import jakarta.validation.Valid;
import java.util.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/produtos-precos-loja")
@RequiredArgsConstructor
public class ProdutoPrecoLojaController {

  private final ProdutoPrecoLojaService service;

  @GetMapping
  public List<ProdutoPrecoLojaResponseDTO> listar(
    @RequestParam(required = false) UUID idProduto,
    @RequestParam(required = false) UUID idLoja
  ) {
    return service.listar(idProduto, idLoja);
  }

  @GetMapping("/{id}")
  public ProdutoPrecoLojaResponseDTO buscar(@PathVariable UUID id) {
    return service.buscar(id);
  }

  @PostMapping
  public ResponseEntity<ProdutoPrecoLojaResponseDTO> criar(
    @Valid @RequestBody ProdutoPrecoLojaRequestDTO d
  ) {
    return ResponseEntity.status(HttpStatus.CREATED).body(service.criar(d));
  }

  @PutMapping("/{id}")
  public ProdutoPrecoLojaResponseDTO atualizar(
    @PathVariable UUID id,
    @Valid @RequestBody ProdutoPrecoLojaRequestDTO d
  ) {
    return service.atualizar(id, d);
  }

  @DeleteMapping("/{id}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void excluir(@PathVariable UUID id) {
    service.excluir(id);
  }
}
