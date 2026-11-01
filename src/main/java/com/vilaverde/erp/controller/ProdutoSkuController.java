package com.vilaverde.erp.controller;

import com.vilaverde.erp.dto.produtosku.ProdutoSkuRequestDTO;
import com.vilaverde.erp.dto.produtosku.ProdutoSkuResponseDTO;
import com.vilaverde.erp.service.ProdutoSkuService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/produtos-sku")
@RequiredArgsConstructor
public class ProdutoSkuController {

  private final ProdutoSkuService produtoSkuService;

  @Operation(operationId = "listarProdutosSku")
  @GetMapping
  public ResponseEntity<List<ProdutoSkuResponseDTO>> listar(
    @RequestParam(required = false) UUID idProduto,
    @RequestParam(required = false) UUID idLoja
  ) {
    return ResponseEntity.ok(produtoSkuService.listar(idProduto, idLoja));
  }

  @Operation(operationId = "buscarProdutoSkuPorId")
  @GetMapping("/{id}")
  public ResponseEntity<ProdutoSkuResponseDTO> buscarPorId(
    @PathVariable UUID id
  ) {
    return ResponseEntity.ok(produtoSkuService.buscarPorId(id));
  }

  @Operation(operationId = "criarProdutoSku")
  @PostMapping
  public ResponseEntity<ProdutoSkuResponseDTO> criar(
    @Valid @RequestBody ProdutoSkuRequestDTO dto
  ) {
    return ResponseEntity.status(HttpStatus.CREATED).body(
      produtoSkuService.criar(dto)
    );
  }

  @Operation(operationId = "atualizarProdutoSku")
  @PutMapping("/{id}")
  public ResponseEntity<ProdutoSkuResponseDTO> atualizar(
    @PathVariable UUID id,
    @Valid @RequestBody ProdutoSkuRequestDTO dto
  ) {
    return ResponseEntity.ok(produtoSkuService.atualizar(id, dto));
  }

  @Operation(operationId = "excluirProdutoSku")
  @DeleteMapping("/{id}")
  public ResponseEntity<Void> excluir(@PathVariable UUID id) {
    produtoSkuService.excluir(id);
    return ResponseEntity.noContent().build();
  }
}
