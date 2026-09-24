package com.vilaverde.erp.controller;

import com.vilaverde.erp.entity.Transacao;
import com.vilaverde.erp.service.TransacaoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/transacoes")
@RequiredArgsConstructor
public class TransacaoController {

    private final TransacaoService service;

    @GetMapping
    public List<Transacao> listar() {
        return service.listarTodas();
    }

    @GetMapping("/{id}")
    public Transacao buscar(@PathVariable UUID id) {
        return service.buscarPorId(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Transacao criar(@Valid @RequestBody Transacao transacao) {
        return service.criar(transacao);
    }

    @PutMapping("/{id}")
    public Transacao atualizar(@PathVariable UUID id, @Valid @RequestBody Transacao transacao) {
        return service.atualizar(id, transacao);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void excluir(@PathVariable UUID id) {
        service.excluir(id);
    }
}
