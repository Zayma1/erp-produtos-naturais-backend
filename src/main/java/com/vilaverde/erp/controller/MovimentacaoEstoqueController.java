package com.vilaverde.erp.controller;

import com.vilaverde.erp.dto.estoque.MovimentacaoResponseDTO;
import com.vilaverde.erp.service.MovimentacaoEstoqueService;
import java.util.*;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/movimentacoes-estoque")
@RequiredArgsConstructor
public class MovimentacaoEstoqueController {

  private final MovimentacaoEstoqueService service;

  @GetMapping
  public List<MovimentacaoResponseDTO> listar(
    @RequestParam(required = false) UUID idProduto,
    @RequestParam(required = false) UUID idLoja
  ) {
    return service.listar(idProduto, idLoja);
  }

  @GetMapping("/{id}")
  public MovimentacaoResponseDTO buscar(@PathVariable UUID id) {
    return service.buscar(id);
  }
}
