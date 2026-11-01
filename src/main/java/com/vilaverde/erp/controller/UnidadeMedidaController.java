package com.vilaverde.erp.controller;

import com.vilaverde.erp.dto.unidademedida.UnidadeMedidaRequestDTO;
import com.vilaverde.erp.dto.unidademedida.UnidadeMedidaResponseDTO;
import com.vilaverde.erp.service.UnidadeMedidaService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/unidades-medida")
@RequiredArgsConstructor
public class UnidadeMedidaController {

  private final UnidadeMedidaService unidadeMedidaService;

  @Operation(operationId = "listarUnidadesMedida")
  @GetMapping
  public ResponseEntity<List<UnidadeMedidaResponseDTO>> listar() {
    return ResponseEntity.ok(unidadeMedidaService.listar());
  }

  @Operation(operationId = "buscarUnidadeMedidaPorId")
  @GetMapping("/{id}")
  public ResponseEntity<UnidadeMedidaResponseDTO> buscarPorId(
    @PathVariable UUID id
  ) {
    return ResponseEntity.ok(unidadeMedidaService.buscarPorId(id));
  }

  @Operation(operationId = "criarUnidadeMedida")
  @PostMapping
  public ResponseEntity<UnidadeMedidaResponseDTO> criar(
    @Valid @RequestBody UnidadeMedidaRequestDTO dto
  ) {
    return ResponseEntity.status(HttpStatus.CREATED).body(
      unidadeMedidaService.criar(dto)
    );
  }

  @Operation(operationId = "atualizarUnidadeMedida")
  @PutMapping("/{id}")
  public ResponseEntity<UnidadeMedidaResponseDTO> atualizar(
    @PathVariable UUID id,
    @Valid @RequestBody UnidadeMedidaRequestDTO dto
  ) {
    return ResponseEntity.ok(unidadeMedidaService.atualizar(id, dto));
  }

  @Operation(operationId = "excluirUnidadeMedida")
  @DeleteMapping("/{id}")
  public ResponseEntity<Void> excluir(@PathVariable UUID id) {
    unidadeMedidaService.excluir(id);
    return ResponseEntity.noContent().build();
  }
}
