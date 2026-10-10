package com.vilaverde.erp.controller;

import com.vilaverde.erp.dto.venda.VendaRequestDTO;
import com.vilaverde.erp.dto.venda.VendaResponseDTO;
import com.vilaverde.erp.service.VendaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/vendas")
@RequiredArgsConstructor
public class VendaController {

    private final VendaService vendaService;

    @GetMapping
    public ResponseEntity<List<VendaResponseDTO>> listar(
            @RequestParam(required = false) UUID idCliente,
            @RequestParam(required = false) UUID idUsuario,
            @RequestParam(required = false) UUID idLoja
    ) {
        return ResponseEntity.ok(vendaService.listar(idCliente, idUsuario, idLoja));
    }

    @GetMapping("/{id}")
    public ResponseEntity<VendaResponseDTO> buscarPorId(@PathVariable UUID id) {
        return ResponseEntity.ok(vendaService.buscarPorId(id));
    }

    @PostMapping
    public ResponseEntity<VendaResponseDTO> criar(@Valid @RequestBody VendaRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(vendaService.criar(dto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<VendaResponseDTO> atualizar(
            @PathVariable UUID id,
            @Valid @RequestBody VendaRequestDTO dto
    ) {
        return ResponseEntity.ok(vendaService.atualizar(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluir(@PathVariable UUID id) {
        vendaService.excluir(id);
        return ResponseEntity.noContent().build();
    }
}
