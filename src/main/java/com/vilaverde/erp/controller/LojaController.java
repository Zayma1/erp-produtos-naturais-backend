package com.vilaverde.erp.controller;

import com.vilaverde.erp.entity.Loja;
import com.vilaverde.erp.service.LojaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/lojas")
@RequiredArgsConstructor
public class LojaController {

    private final LojaService service;

    @GetMapping
    public List<Loja> listar() {
        return service.listarTodas();
    }

    @GetMapping("/{id}")
    public Loja buscar(@PathVariable UUID id) {
        return service.buscarPorId(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Loja criar(@Valid @RequestBody Loja loja) {
        return service.criar(loja);
    }

    @PutMapping("/{id}")
    public Loja atualizar(@PathVariable UUID id, @Valid @RequestBody Loja loja) {
        return service.atualizar(id, loja);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void excluir(@PathVariable UUID id) {
        service.excluir(id);
    }
}
