package com.vilaverde.erp.controller;

import com.vilaverde.erp.dto.produto.*;
import com.vilaverde.erp.service.ProdutoPrecoLojaService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import java.util.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/skus-precos-loja")
@RequiredArgsConstructor
public class ProdutoPrecoLojaController {

  private final ProdutoPrecoLojaService service;

  @Operation(operationId = "listarPrecosProdutoLoja")
  @GetMapping
  public List<ProdutoPrecoLojaResponseDTO> listar(
    @RequestParam(required = false) UUID idProduto,
    @RequestParam(required = false) UUID idProdutoSku,
    @RequestParam(required = false) UUID idLoja
  ) {
    return service.listar(idProduto, idProdutoSku, idLoja);
  }

  @Operation(operationId = "buscarPrecoProdutoLojaPorId")
  @GetMapping("/{id}")
  public ProdutoPrecoLojaResponseDTO buscar(@PathVariable UUID id) {
    return service.buscar(id);
  }

  @Operation(operationId = "criarPrecoProdutoLoja")
  @PostMapping
  public ResponseEntity<ProdutoPrecoLojaResponseDTO> criar(
    @Valid @RequestBody ProdutoPrecoLojaRequestDTO d
  ) {
    return ResponseEntity.status(HttpStatus.CREATED).body(service.criar(d));
  }

  @Operation(operationId = "atualizarPrecoProdutoLoja")
  @PutMapping("/{id}")
  public ProdutoPrecoLojaResponseDTO atualizar(
    @PathVariable UUID id,
    @Valid @RequestBody ProdutoPrecoLojaRequestDTO d
  ) {
    return service.atualizar(id, d);
  }

  @Operation(operationId = "excluirPrecoProdutoLoja")
  @DeleteMapping("/{id}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void excluir(@PathVariable UUID id) {
    service.excluir(id);
  }
}
