package com.vilaverde.erp.controller;

import com.vilaverde.erp.dto.produtoloja.ProdutoLojaRequestDTO;
import com.vilaverde.erp.dto.produtoloja.ProdutoLojaResponseDTO;
import com.vilaverde.erp.service.ProdutoLojaService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/produtos-loja")
@RequiredArgsConstructor
public class ProdutoLojaController {

  private final ProdutoLojaService produtoLojaService;

  @Operation(operationId = "listarProdutosLoja")
  @GetMapping
  public ResponseEntity<List<ProdutoLojaResponseDTO>> listar(
    @RequestParam(required = false) UUID idProduto,
    @RequestParam(required = false) UUID idLoja
  ) {
    return ResponseEntity.ok(produtoLojaService.listar(idProduto, idLoja));
  }

  @Operation(operationId = "buscarProdutoLojaPorId")
  @GetMapping("/{id}")
  public ResponseEntity<ProdutoLojaResponseDTO> buscarPorId(
    @PathVariable UUID id
  ) {
    return ResponseEntity.ok(produtoLojaService.buscarPorId(id));
  }

  @Operation(operationId = "criarProdutoLoja")
  @PostMapping
  public ResponseEntity<ProdutoLojaResponseDTO> criar(
    @Valid @RequestBody ProdutoLojaRequestDTO dto
  ) {
    return ResponseEntity.status(HttpStatus.CREATED).body(
      produtoLojaService.criar(dto)
    );
  }

  @Operation(operationId = "excluirProdutoLoja")
  @DeleteMapping("/{id}")
  public ResponseEntity<Void> excluir(@PathVariable UUID id) {
    produtoLojaService.excluir(id);
    return ResponseEntity.noContent().build();
  }
}
