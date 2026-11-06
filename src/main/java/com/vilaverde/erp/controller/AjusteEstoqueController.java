package com.vilaverde.erp.controller;
import com.vilaverde.erp.dto.ajuste.*;
import com.vilaverde.erp.entity.enums.StatusDocumento;
import com.vilaverde.erp.service.AjusteEstoqueService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import java.util.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
@RestController @RequestMapping("/api/ajustes-estoque") @RequiredArgsConstructor
public class AjusteEstoqueController {
 private final AjusteEstoqueService service;
 @Operation(operationId="listarAjustesEstoque") @GetMapping
 public List<AjusteResponseDTO> listar(@RequestParam(required=false) UUID idLoja,@RequestParam(required=false) StatusDocumento status) {return service.listar(idLoja,status);}
 @Operation(operationId="buscarAjusteEstoquePorId") @GetMapping("/{id}")
 public AjusteResponseDTO buscar(@PathVariable UUID id) {return service.buscar(id);}
 @Operation(operationId="criarAjusteEstoque") @PostMapping
 public ResponseEntity<AjusteResponseDTO> criar(@RequestHeader("Idempotency-Key") UUID chave,@Valid @RequestBody AjusteRequestDTO d) {return ResponseEntity.status(201).body(service.criar(chave,d));}
 @Operation(operationId="atualizarAjusteEstoque") @PutMapping("/{id}")
 public AjusteResponseDTO atualizar(@PathVariable UUID id,@Valid @RequestBody AjusteRequestDTO d) {return service.atualizar(id,d);}
 @Operation(operationId="concluirAjusteEstoque") @PostMapping("/{id}/concluir")
 public AjusteResponseDTO concluir(@PathVariable UUID id,@Valid @RequestBody AjusteAcaoDTO d) {return service.concluir(id,d.versao());}
 @Operation(operationId="cancelarAjusteEstoque") @PostMapping("/{id}/cancelar")
 public AjusteResponseDTO cancelar(@PathVariable UUID id,@Valid @RequestBody AjusteAcaoDTO d) {return service.cancelar(id,d.versao(),d.motivo());}
 @Operation(operationId="excluirAjusteEstoque") @DeleteMapping("/{id}")
 public void excluir(@PathVariable UUID id) {throw new org.springframework.security.access.AccessDeniedException("Use cancelamento para preservar o histórico");}
}
