package com.vilaverde.erp.controller;
import com.vilaverde.erp.dto.devolucao.*;
import com.vilaverde.erp.service.DevolucaoClienteService;
import io.swagger.v3.oas.annotations.Operation;
import java.util.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
@RestController @RequestMapping("/api/devolucoes-clientes") @RequiredArgsConstructor
public class DevolucaoClienteController {
 private final DevolucaoClienteService service;
 @GetMapping @Operation(operationId="listarDevolucoesClientes")
 public List<DevolucaoClienteResponseDTO> listar(@RequestParam(required=false) UUID idVenda) {return service.listar(idVenda);}
 @GetMapping("/{id}") @Operation(operationId="buscarDevolucaoClientePorId")
 public DevolucaoClienteResponseDTO buscar(@PathVariable UUID id) {return service.buscar(id);}
 @PostMapping @Operation(operationId="criarDevolucaoCliente")
 public ResponseEntity<DevolucaoClienteResponseDTO> criar(@RequestHeader("Idempotency-Key") UUID key,@RequestBody DevolucaoClienteRequestDTO d) {return ResponseEntity.status(201).body(service.criar(key,d));}
 @PutMapping("/{id}") @Operation(operationId="atualizarDevolucaoCliente")
 public DevolucaoClienteResponseDTO atualizar(@PathVariable UUID id,@RequestBody DevolucaoClienteRequestDTO d) {return service.atualizar(id,d);}
 @PostMapping("/{id}/concluir") @Operation(operationId="concluirDevolucaoCliente")
 public DevolucaoClienteResponseDTO concluir(@PathVariable UUID id,@RequestBody DevolucaoAcaoDTO d) {return service.concluir(id,d.versao());}
 @PostMapping("/{id}/cancelar") @Operation(operationId="cancelarDevolucaoCliente")
 public DevolucaoClienteResponseDTO cancelar(@PathVariable UUID id,@RequestBody DevolucaoAcaoDTO d) {return service.cancelar(id,d.versao(),d.motivo());}
}
