package com.vilaverde.erp.controller;

import com.vilaverde.erp.dto.estoque.MovimentacaoResponseDTO;
import com.vilaverde.erp.service.MovimentacaoEstoqueService;
import io.swagger.v3.oas.annotations.Operation;
import java.util.*;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/movimentacoes-estoque")
@RequiredArgsConstructor
public class MovimentacaoEstoqueController {

  private final MovimentacaoEstoqueService service;

  @Operation(operationId = "listarMovimentacoesEstoque")
  @GetMapping
  public List<MovimentacaoResponseDTO> listar(
    @RequestParam(required = false) UUID idProduto,
    @RequestParam(required = false) UUID idProdutoSku,
    @RequestParam(required = false) UUID idLoja
  ) {
    return service.listar(idProduto, idProdutoSku, idLoja);
  }

  @Operation(operationId = "buscarMovimentacaoEstoquePorId")
  @GetMapping("/{id}")
  public MovimentacaoResponseDTO buscar(@PathVariable UUID id) {
    return service.buscar(id);
  }
}
