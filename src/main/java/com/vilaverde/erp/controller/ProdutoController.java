package com.vilaverde.erp.controller;

import com.vilaverde.erp.dto.produto.ProdutoRequestDTO;
import com.vilaverde.erp.dto.produto.ProdutoResponseDTO;
import com.vilaverde.erp.service.ProdutoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/produtos")
@RequiredArgsConstructor
public class ProdutoController {

    private final ProdutoService service;

    @GetMapping
    public List<ProdutoResponseDTO> listar() {
        return service.listarTodos();
    }

    @GetMapping("/{id}")
    public ProdutoResponseDTO buscar(@PathVariable UUID id) {
        return service.buscarPorId(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ProdutoResponseDTO criar(@Valid @RequestBody ProdutoRequestDTO dto) {
        return service.criar(dto);
    }

    @PutMapping("/{id}")
    public ProdutoResponseDTO atualizar(@PathVariable UUID id, @Valid @RequestBody ProdutoRequestDTO dto) {
        return service.atualizar(id, dto);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void excluir(@PathVariable UUID id) {
        service.excluir(id);
    }
}
