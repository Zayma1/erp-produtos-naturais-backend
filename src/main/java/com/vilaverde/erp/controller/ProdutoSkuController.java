package com.vilaverde.erp.controller;

import com.vilaverde.erp.dto.produtosku.ProdutoSkuRequestDTO;
import com.vilaverde.erp.dto.produtosku.ProdutoSkuResponseDTO;
import com.vilaverde.erp.service.ProdutoSkuService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/produto-skus")
@RequiredArgsConstructor
public class ProdutoSkuController {

    private final ProdutoSkuService service;

    @GetMapping
    public List<ProdutoSkuResponseDTO> listar(@RequestParam(required = false) UUID idProduto) {
        if (idProduto != null) {
            return service.listarPorProduto(idProduto);
        }
        return service.listarTodos();
    }

    @GetMapping("/{id}")
    public ProdutoSkuResponseDTO buscar(@PathVariable UUID id) {
        return service.buscarPorId(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ProdutoSkuResponseDTO criar(@Valid @RequestBody ProdutoSkuRequestDTO dto) {
        return service.criar(dto);
    }

    @PutMapping("/{id}")
    public ProdutoSkuResponseDTO atualizar(@PathVariable UUID id, @Valid @RequestBody ProdutoSkuRequestDTO dto) {
        return service.atualizar(id, dto);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void excluir(@PathVariable UUID id) {
        service.excluir(id);
    }
}
