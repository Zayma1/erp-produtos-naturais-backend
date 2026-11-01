package com.vilaverde.erp.controller;

import com.vilaverde.erp.dto.categoria.CategoriaRequestDTO;
import com.vilaverde.erp.dto.categoria.CategoriaResponseDTO;
import com.vilaverde.erp.service.CategoriaService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/categorias")
@RequiredArgsConstructor
public class CategoriaController {

  private final CategoriaService categoriaService;

  @Operation(operationId = "listarCategorias")
  @GetMapping
  public ResponseEntity<List<CategoriaResponseDTO>> listar() {
    return ResponseEntity.ok(categoriaService.listar());
  }

  @Operation(operationId = "buscarCategoriaPorId")
  @GetMapping("/{id}")
  public ResponseEntity<CategoriaResponseDTO> buscarPorId(
    @PathVariable UUID id
  ) {
    return ResponseEntity.ok(categoriaService.buscarPorId(id));
  }

  @Operation(operationId = "criarCategoria")
  @PostMapping
  public ResponseEntity<CategoriaResponseDTO> criar(
    @Valid @RequestBody CategoriaRequestDTO dto
  ) {
    return ResponseEntity.status(HttpStatus.CREATED).body(
      categoriaService.criar(dto)
    );
  }

  @Operation(operationId = "atualizarCategoria")
  @PutMapping("/{id}")
  public ResponseEntity<CategoriaResponseDTO> atualizar(
    @PathVariable UUID id,
    @Valid @RequestBody CategoriaRequestDTO dto
  ) {
    return ResponseEntity.ok(categoriaService.atualizar(id, dto));
  }

  @Operation(operationId = "excluirCategoria")
  @DeleteMapping("/{id}")
  public ResponseEntity<Void> excluir(@PathVariable UUID id) {
    categoriaService.excluir(id);
    return ResponseEntity.noContent().build();
  }
}
