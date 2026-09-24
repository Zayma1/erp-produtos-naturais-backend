package com.vilaverde.erp.controller;

import com.vilaverde.erp.dto.estoque.EstoqueResponseDTO;
import com.vilaverde.erp.dto.estoque.MovimentacaoRequestDTO;
import com.vilaverde.erp.service.EstoqueService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/estoque")
@RequiredArgsConstructor
public class EstoqueController {

    private final EstoqueService service;

    @GetMapping
    public List<EstoqueResponseDTO> listar() {
        return service.listarTodos();
    }

    @GetMapping("/produto/{idProduto}")
    public List<EstoqueResponseDTO> historico(@PathVariable UUID idProduto) {
        return service.historicoPorProduto(idProduto);
    }

    @GetMapping("/saldo")
    public BigDecimal saldo(@RequestParam UUID idProduto, @RequestParam UUID idLoja) {
        return service.saldoAtual(idProduto, idLoja);
    }

    @PostMapping("/movimentacao")
    @ResponseStatus(HttpStatus.CREATED)
    public EstoqueResponseDTO registrarMovimentacao(@Valid @RequestBody MovimentacaoRequestDTO dto) {
        return service.registrarMovimentacao(dto);
    }
}
