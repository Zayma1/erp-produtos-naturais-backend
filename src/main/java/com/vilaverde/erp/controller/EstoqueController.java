package com.vilaverde.erp.controller;

import com.vilaverde.erp.dto.estoque.EstoqueResponseDTO;
import com.vilaverde.erp.dto.estoque.SaldoEstoqueResponseDTO;
import com.vilaverde.erp.service.EstoqueService;
import io.swagger.v3.oas.annotations.Operation;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/estoques")
@RequiredArgsConstructor
public class EstoqueController {

  private final EstoqueService estoqueService;

  @Operation(operationId = "listarEstoques")
  @GetMapping
  public ResponseEntity<List<EstoqueResponseDTO>> listar(
    @RequestParam(required = false) UUID idProduto,
    @RequestParam(required = false) UUID idLoja
  ) {
    return ResponseEntity.ok(estoqueService.listar(idProduto, idLoja));
  }

  @Operation(operationId = "buscarEstoquePorId")
  @GetMapping("/{id}")
  public ResponseEntity<EstoqueResponseDTO> buscarPorId(@PathVariable UUID id) {
    return ResponseEntity.ok(estoqueService.buscarPorId(id));
  }

  @Operation(operationId = "consultarSaldoEstoque")
  @GetMapping("/saldo")
  public ResponseEntity<SaldoEstoqueResponseDTO> saldo(
    @RequestParam UUID idProduto,
    @RequestParam UUID idLoja
  ) {
    return ResponseEntity.ok(estoqueService.saldo(idProduto, idLoja));
  }
}
