package com.vilaverde.erp.controller;
import com.vilaverde.erp.dto.devolucao.*;
import com.vilaverde.erp.service.DevolucaoFornecedorService;
import io.swagger.v3.oas.annotations.Operation;
import java.util.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
@RestController @RequestMapping("/api/devolucoes-fornecedores") @RequiredArgsConstructor
public class DevolucaoFornecedorController {
 private final DevolucaoFornecedorService service;
 @GetMapping @Operation(operationId="listarDevolucoesFornecedores")
 public List<DevolucaoFornecedorResponseDTO> listar(@RequestParam(required=false) UUID idCompra) {return service.listar(idCompra);}
 @GetMapping("/{id}") @Operation(operationId="buscarDevolucaoFornecedorPorId")
 public DevolucaoFornecedorResponseDTO buscar(@PathVariable UUID id) {return service.buscar(id);}
 @PostMapping @Operation(operationId="criarDevolucaoFornecedor")
 public ResponseEntity<DevolucaoFornecedorResponseDTO> criar(@RequestHeader("Idempotency-Key") UUID key,@RequestBody DevolucaoFornecedorRequestDTO d) {return ResponseEntity.status(201).body(service.criar(key,d));}
 @PutMapping("/{id}") @Operation(operationId="atualizarDevolucaoFornecedor")
 public DevolucaoFornecedorResponseDTO atualizar(@PathVariable UUID id,@RequestBody DevolucaoFornecedorRequestDTO d) {return service.atualizar(id,d);}
 @PostMapping("/{id}/concluir") @Operation(operationId="concluirDevolucaoFornecedor")
 public DevolucaoFornecedorResponseDTO concluir(@PathVariable UUID id,@RequestBody DevolucaoAcaoDTO d) {return service.concluir(id,d.versao());}
 @PostMapping("/{id}/cancelar") @Operation(operationId="cancelarDevolucaoFornecedor")
 public DevolucaoFornecedorResponseDTO cancelar(@PathVariable UUID id,@RequestBody DevolucaoAcaoDTO d) {return service.cancelar(id,d.versao(),d.motivo());}
}
