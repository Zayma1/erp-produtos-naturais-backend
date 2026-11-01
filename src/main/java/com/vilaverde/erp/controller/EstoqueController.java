package com.vilaverde.erp.controller;

import com.vilaverde.erp.dto.estoque.EstoqueResponseDTO;
import com.vilaverde.erp.dto.estoque.SaldoEstoqueResponseDTO;
import com.vilaverde.erp.service.EstoqueService;
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

  @GetMapping
  public ResponseEntity<List<EstoqueResponseDTO>> listar(
    @RequestParam(required = false) UUID idProduto,
    @RequestParam(required = false) UUID idLoja
  ) {
    return ResponseEntity.ok(estoqueService.listar(idProduto, idLoja));
  }

  @GetMapping("/{id}")
  public ResponseEntity<EstoqueResponseDTO> buscarPorId(@PathVariable UUID id) {
    return ResponseEntity.ok(estoqueService.buscarPorId(id));
  }

  @GetMapping("/saldo")
  public ResponseEntity<SaldoEstoqueResponseDTO> saldo(
    @RequestParam UUID idProduto,
    @RequestParam UUID idLoja
  ) {
    return ResponseEntity.ok(estoqueService.saldo(idProduto, idLoja));
  }
}
