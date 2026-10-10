package com.vilaverde.erp.controller;

import com.vilaverde.erp.dto.loja.LojaRequestDTO;
import com.vilaverde.erp.dto.loja.LojaResponseDTO;
import com.vilaverde.erp.service.LojaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/lojas")
@RequiredArgsConstructor
public class LojaController {

    private final LojaService lojaService;

    @GetMapping
    public ResponseEntity<List<LojaResponseDTO>> listar() {
        return ResponseEntity.ok(lojaService.listar());
    }

    @GetMapping("/{id}")
    public ResponseEntity<LojaResponseDTO> buscarPorId(@PathVariable UUID id) {
        return ResponseEntity.ok(lojaService.buscarPorId(id));
    }

    @PostMapping
    public ResponseEntity<LojaResponseDTO> criar(@Valid @RequestBody LojaRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(lojaService.criar(dto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<LojaResponseDTO> atualizar(
            @PathVariable UUID id,
            @Valid @RequestBody LojaRequestDTO dto
    ) {
        return ResponseEntity.ok(lojaService.atualizar(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluir(@PathVariable UUID id) {
        lojaService.excluir(id);
        return ResponseEntity.noContent().build();
    }
}
