package com.vilaverde.erp.controller;

import com.vilaverde.erp.dto.transacao.TransacaoRequestDTO;
import com.vilaverde.erp.dto.transacao.TransacaoResponseDTO;
import com.vilaverde.erp.service.TransacaoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/transacoes")
@RequiredArgsConstructor
public class TransacaoController {

    private final TransacaoService transacaoService;

    @GetMapping
    public ResponseEntity<List<TransacaoResponseDTO>> listar() {
        return ResponseEntity.ok(transacaoService.listar());
    }

    @GetMapping("/{id}")
    public ResponseEntity<TransacaoResponseDTO> buscarPorId(@PathVariable UUID id) {
        return ResponseEntity.ok(transacaoService.buscarPorId(id));
    }

    @PostMapping
    public ResponseEntity<TransacaoResponseDTO> criar(@Valid @RequestBody TransacaoRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(transacaoService.criar(dto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<TransacaoResponseDTO> atualizar(
            @PathVariable UUID id,
            @Valid @RequestBody TransacaoRequestDTO dto
    ) {
        return ResponseEntity.ok(transacaoService.atualizar(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluir(@PathVariable UUID id) {
        transacaoService.excluir(id);
        return ResponseEntity.noContent().build();
    }
}
