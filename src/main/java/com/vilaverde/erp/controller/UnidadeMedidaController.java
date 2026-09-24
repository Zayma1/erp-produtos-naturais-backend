package com.vilaverde.erp.controller;

import com.vilaverde.erp.entity.UnidadeMedida;
import com.vilaverde.erp.service.UnidadeMedidaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/unidades-medida")
@RequiredArgsConstructor
public class UnidadeMedidaController {

    private final UnidadeMedidaService service;

    @GetMapping
    public List<UnidadeMedida> listar() {
        return service.listarTodas();
    }

    @GetMapping("/{id}")
    public UnidadeMedida buscar(@PathVariable UUID id) {
        return service.buscarPorId(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public UnidadeMedida criar(@Valid @RequestBody UnidadeMedida unidade) {
        return service.criar(unidade);
    }

    @PutMapping("/{id}")
    public UnidadeMedida atualizar(@PathVariable UUID id, @Valid @RequestBody UnidadeMedida unidade) {
        return service.atualizar(id, unidade);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void excluir(@PathVariable UUID id) {
        service.excluir(id);
    }
}
