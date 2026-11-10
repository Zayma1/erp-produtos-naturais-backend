package com.vilaverde.erp.controller;
import com.vilaverde.erp.dto.transferencia.*;
import com.vilaverde.erp.entity.enums.StatusDocumento;
import com.vilaverde.erp.service.TransferenciaEstoqueService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import java.util.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
@RestController @RequestMapping("/api/transferencias-estoque") @RequiredArgsConstructor
public class TransferenciaEstoqueController {
 private final TransferenciaEstoqueService service;
 @Operation(operationId="listarTransferenciasEstoque") @GetMapping
 public List<TransferenciaResponseDTO> listar(@RequestParam(required=false) UUID idLojaOrigem,@RequestParam(required=false) UUID idLojaDestino,@RequestParam(required=false) StatusDocumento status) {return service.listar(idLojaOrigem,idLojaDestino,status);}
 @Operation(operationId="buscarTransferenciaEstoquePorId") @GetMapping("/{id}")
 public TransferenciaResponseDTO buscar(@PathVariable UUID id) {return service.buscar(id);}
 @Operation(operationId="criarTransferenciaEstoque") @PostMapping
 public ResponseEntity<TransferenciaResponseDTO> criar(@RequestHeader("Idempotency-Key") UUID chave,@Valid @RequestBody TransferenciaRequestDTO d) {return ResponseEntity.status(201).body(service.criar(chave,d));}
 @Operation(operationId="atualizarTransferenciaEstoque") @PutMapping("/{id}")
 public TransferenciaResponseDTO atualizar(@PathVariable UUID id,@Valid @RequestBody TransferenciaRequestDTO d) {return service.atualizar(id,d);}
 @Operation(operationId="concluirTransferenciaEstoque") @PostMapping("/{id}/concluir")
 public TransferenciaResponseDTO concluir(@PathVariable UUID id,@Valid @RequestBody TransferenciaAcaoDTO d) {return service.concluir(id,d.versao());}
 @Operation(operationId="cancelarTransferenciaEstoque") @PostMapping("/{id}/cancelar")
 public TransferenciaResponseDTO cancelar(@PathVariable UUID id,@Valid @RequestBody TransferenciaAcaoDTO d) {return service.cancelar(id,d.versao(),d.motivo());}
 @Operation(operationId="excluirTransferenciaEstoque") @DeleteMapping("/{id}")
 public void excluir(@PathVariable UUID id) {throw new org.springframework.security.access.AccessDeniedException("Use cancelamento para preservar o histórico");}
}
