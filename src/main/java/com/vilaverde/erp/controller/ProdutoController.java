package com.vilaverde.erp.controller;

import com.vilaverde.erp.dto.produto.ProdutoRequestDTO;
import com.vilaverde.erp.dto.produto.ProdutoResponseDTO;
import com.vilaverde.erp.service.ProdutoService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/produtos")
@RequiredArgsConstructor
public class ProdutoController {

  private final ProdutoService produtoService;

  @Operation(operationId = "listarProdutos")
  @GetMapping
  public ResponseEntity<List<ProdutoResponseDTO>> listar() {
    return ResponseEntity.ok(produtoService.listar());
  }

  @Operation(operationId = "buscarProdutoPorId")
  @GetMapping("/{id}")
  public ResponseEntity<ProdutoResponseDTO> buscarPorId(@PathVariable UUID id) {
    return ResponseEntity.ok(produtoService.buscarPorId(id));
  }

  @Operation(operationId = "criarProduto")
  @PostMapping
  public ResponseEntity<ProdutoResponseDTO> criar(
    @Valid @RequestBody ProdutoRequestDTO dto
  ) {
    return ResponseEntity.status(HttpStatus.CREATED).body(
      produtoService.criar(dto)
    );
  }

  @Operation(operationId = "atualizarProduto")
  @PutMapping("/{id}")
  public ResponseEntity<ProdutoResponseDTO> atualizar(
    @PathVariable UUID id,
    @Valid @RequestBody ProdutoRequestDTO dto
  ) {
    return ResponseEntity.ok(produtoService.atualizar(id, dto));
  }

  @Operation(operationId = "excluirProduto")
  @DeleteMapping("/{id}")
  public ResponseEntity<Void> excluir(@PathVariable UUID id) {
    produtoService.excluir(id);
    return ResponseEntity.noContent().build();
  }
}
