package com.vilaverde.erp.controller;

import com.vilaverde.erp.entity.ProdutoLoja;
import com.vilaverde.erp.service.ProdutoLojaService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/produto-lojas")
@RequiredArgsConstructor
public class ProdutoLojaController {

    private final ProdutoLojaService service;

    @GetMapping
    public List<ProdutoLoja> listar(@RequestParam(required = false) UUID idProduto) {
        if (idProduto != null) {
            return service.listarPorProduto(idProduto);
        }
        return service.listarTodos();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ProdutoLoja vincular(@RequestParam UUID idProduto, @RequestParam UUID idLoja) {
        return service.vincular(idProduto, idLoja);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void desvincular(@PathVariable UUID id) {
        service.desvincular(id);
    }
}
