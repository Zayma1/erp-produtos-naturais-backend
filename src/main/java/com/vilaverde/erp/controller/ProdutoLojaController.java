package com.vilaverde.erp.controller;

import com.vilaverde.erp.dto.produtoloja.ProdutoLojaRequestDTO;
import com.vilaverde.erp.dto.produtoloja.ProdutoLojaResponseDTO;
import com.vilaverde.erp.service.ProdutoLojaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/produtos-loja")
@RequiredArgsConstructor
public class ProdutoLojaController {

    private final ProdutoLojaService produtoLojaService;

    @GetMapping
    public ResponseEntity<List<ProdutoLojaResponseDTO>> listar(
            @RequestParam(required = false) UUID idProduto,
            @RequestParam(required = false) UUID idLoja
    ) {
        return ResponseEntity.ok(produtoLojaService.listar(idProduto, idLoja));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProdutoLojaResponseDTO> buscarPorId(@PathVariable UUID id) {
        return ResponseEntity.ok(produtoLojaService.buscarPorId(id));
    }

    @PostMapping
    public ResponseEntity<ProdutoLojaResponseDTO> criar(@Valid @RequestBody ProdutoLojaRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(produtoLojaService.criar(dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluir(@PathVariable UUID id) {
        produtoLojaService.excluir(id);
        return ResponseEntity.noContent().build();
    }
}
