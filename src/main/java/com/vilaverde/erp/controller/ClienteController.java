package com.vilaverde.erp.controller;

import com.vilaverde.erp.dto.cliente.ClienteRequestDTO;
import com.vilaverde.erp.dto.cliente.ClienteResponseDTO;
import com.vilaverde.erp.service.ClienteService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/clientes")
@RequiredArgsConstructor
public class ClienteController {

  private final ClienteService clienteService;

  @Operation(operationId = "listarClientes")
  @GetMapping
  public ResponseEntity<List<ClienteResponseDTO>> listar() {
    return ResponseEntity.ok(clienteService.listar());
  }

  @Operation(operationId = "buscarClientePorId")
  @GetMapping("/{id}")
  public ResponseEntity<ClienteResponseDTO> buscarPorId(@PathVariable UUID id) {
    return ResponseEntity.ok(clienteService.buscarPorId(id));
  }

  @Operation(operationId = "criarCliente")
  @PostMapping
  public ResponseEntity<ClienteResponseDTO> criar(
    @Valid @RequestBody ClienteRequestDTO dto
  ) {
    return ResponseEntity.status(HttpStatus.CREATED).body(
      clienteService.criar(dto)
    );
  }

  @Operation(operationId = "atualizarCliente")
  @PutMapping("/{id}")
  public ResponseEntity<ClienteResponseDTO> atualizar(
    @PathVariable UUID id,
    @Valid @RequestBody ClienteRequestDTO dto
  ) {
    return ResponseEntity.ok(clienteService.atualizar(id, dto));
  }

  @Operation(operationId = "excluirCliente")
  @DeleteMapping("/{id}")
  public ResponseEntity<Void> excluir(@PathVariable UUID id) {
    clienteService.excluir(id);
    return ResponseEntity.noContent().build();
  }
}
