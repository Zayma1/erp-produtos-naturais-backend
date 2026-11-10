package com.vilaverde.erp.controller;

import com.vilaverde.erp.dto.recebimento.*;
import com.vilaverde.erp.service.RecebimentoCompraService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import java.util.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/recebimentos-compras")
@io.swagger.v3.oas.annotations.responses.ApiResponses({
  @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode="400", description="Dados inválidos", content=@io.swagger.v3.oas.annotations.media.Content(schema=@io.swagger.v3.oas.annotations.media.Schema(implementation=com.vilaverde.erp.exception.ApiError.class))),
  @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode="404", description="Registro inexistente", content=@io.swagger.v3.oas.annotations.media.Content(schema=@io.swagger.v3.oas.annotations.media.Schema(implementation=com.vilaverde.erp.exception.ApiError.class))),
  @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode="409", description="Versão, saldo ou estado em conflito", content=@io.swagger.v3.oas.annotations.media.Content(schema=@io.swagger.v3.oas.annotations.media.Schema(implementation=com.vilaverde.erp.exception.ApiError.class)))
})
@RequiredArgsConstructor
public class RecebimentoCompraController {

  private final RecebimentoCompraService service;

  @Operation(operationId = "listarRecebimentosCompra")
  @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode="200", description="Consulta do recebimento")
  @GetMapping
  public List<RecebimentoCompraResponseDTO> listar(@RequestParam(required = false) UUID idCompra) {
    return service.listar(idCompra);
  }

  @Operation(operationId = "buscarRecebimentoCompraPorId")
  @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode="200", description="Consulta do recebimento")
  @GetMapping("/{id}")
  public RecebimentoCompraResponseDTO buscar(@PathVariable UUID id) {
    return service.buscar(id);
  }

  @Operation(operationId = "criarRecebimentoCompra")
  @PostMapping
  @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode="201", description="Rascunho criado ou operação repetida")
  public ResponseEntity<RecebimentoCompraResponseDTO> criar(
    @RequestHeader("Idempotency-Key") UUID chave,
    @Valid @RequestBody RecebimentoCompraRequestDTO d
  ) {
    return ResponseEntity.status(HttpStatus.CREATED).body(service.criar(chave, d));
  }

  @Operation(operationId = "atualizarRecebimentoCompra")
  @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode="200", description="Rascunho substituído")
  @PutMapping("/{id}")
  public RecebimentoCompraResponseDTO atualizar(
    @PathVariable UUID id,
    @Valid @RequestBody RecebimentoCompraRequestDTO d
  ) {
    return service.atualizar(id, d);
  }

  @Operation(operationId = "concluirRecebimentoCompra")
  @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode="200", description="Recebimento confirmado")
  @PostMapping("/{id}/concluir")
  public RecebimentoCompraResponseDTO concluir(@PathVariable UUID id,
      @Valid @RequestBody ConcluirRecebimentoRequestDTO d) {
    return service.concluir(id, d.versao());
  }

  @Operation(operationId = "excluirRecebimentoCompra")
  @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode="204", description="Rascunho excluído")
  @DeleteMapping("/{id}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void excluir(@PathVariable UUID id, @RequestParam Integer versao) {
    service.excluir(id, versao);
  }
}
