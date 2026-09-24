package com.vilaverde.erp.controller;

import com.vilaverde.erp.entity.Fornecedor;
import com.vilaverde.erp.service.FornecedorService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/fornecedores")
@RequiredArgsConstructor
public class FornecedorController {

    private final FornecedorService service;

    @GetMapping
    public List<Fornecedor> listar() {
        return service.listarTodos();
    }

    @GetMapping("/{id}")
    public Fornecedor buscar(@PathVariable UUID id) {
        return service.buscarPorId(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Fornecedor criar(@Valid @RequestBody Fornecedor fornecedor) {
        return service.criar(fornecedor);
    }

    @PutMapping("/{id}")
    public Fornecedor atualizar(@PathVariable UUID id, @Valid @RequestBody Fornecedor fornecedor) {
        return service.atualizar(id, fornecedor);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void excluir(@PathVariable UUID id) {
        service.excluir(id);
    }
}
