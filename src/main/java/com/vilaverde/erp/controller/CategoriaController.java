package com.vilaverde.erp.controller;

import com.vilaverde.erp.entity.Categoria;
import com.vilaverde.erp.service.CategoriaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/categorias")
@RequiredArgsConstructor
public class CategoriaController {

    private final CategoriaService service;

    @GetMapping
    public List<Categoria> listar() {
        return service.listarTodas();
    }

    @GetMapping("/{id}")
    public Categoria buscar(@PathVariable UUID id) {
        return service.buscarPorId(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Categoria criar(@Valid @RequestBody Categoria categoria) {
        return service.criar(categoria);
    }

    @PutMapping("/{id}")
    public Categoria atualizar(@PathVariable UUID id, @Valid @RequestBody Categoria categoria) {
        return service.atualizar(id, categoria);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void excluir(@PathVariable UUID id) {
        service.excluir(id);
    }
}
