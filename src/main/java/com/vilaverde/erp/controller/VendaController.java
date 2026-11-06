package com.vilaverde.erp.controller;

import com.vilaverde.erp.dto.venda.VendaRequestDTO;
import com.vilaverde.erp.dto.venda.VendaResponseDTO;
import com.vilaverde.erp.service.VendaService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/vendas")
@RequiredArgsConstructor
public class VendaController {

  private final VendaService vendaService;

  @Operation(operationId = "listarVendas")
  @GetMapping
  public ResponseEntity<List<VendaResponseDTO>> listar(
    @RequestParam(required = false) UUID idCliente,
    @RequestParam(required = false) UUID idUsuario,
    @RequestParam(required = false) UUID idLoja
  ) {
    return ResponseEntity.ok(vendaService.listar(idCliente, idUsuario, idLoja));
  }

  @Operation(operationId = "buscarVendaPorId")
  @GetMapping("/{id}")
  public ResponseEntity<VendaResponseDTO> buscarPorId(@PathVariable UUID id) {
    return ResponseEntity.ok(vendaService.buscarPorId(id));
  }

  @Operation(operationId = "criarVenda")
  @PostMapping
  public ResponseEntity<VendaResponseDTO> criar(
    @RequestHeader("Idempotency-Key") UUID chave,
    @Valid @RequestBody VendaRequestDTO dto
  ) {
    return ResponseEntity.status(HttpStatus.CREATED).body(
      vendaService.criar(chave, dto)
    );
  }

  @Operation(operationId = "atualizarVenda")
  @PutMapping("/{id}")
  public ResponseEntity<VendaResponseDTO> atualizar(
    @PathVariable UUID id,
    @Valid @RequestBody VendaRequestDTO dto
  ) {
    return ResponseEntity.ok(vendaService.atualizar(id, dto));
  }

  @Operation(operationId = "concluirVenda")
  @PostMapping("/{id}/concluir")
  public ResponseEntity<VendaResponseDTO> concluir(@PathVariable UUID id,
    @Valid @RequestBody com.vilaverde.erp.dto.venda.VendaAcaoDTO dto) {
    return ResponseEntity.ok(vendaService.concluir(id,dto.versao()));
  }
  @Operation(operationId = "cancelarVenda")
  @PostMapping("/{id}/cancelar")
  public ResponseEntity<VendaResponseDTO> cancelar(@PathVariable UUID id,
    @Valid @RequestBody com.vilaverde.erp.dto.venda.VendaAcaoDTO dto) {
    return ResponseEntity.ok(vendaService.cancelar(id,dto.versao(),dto.motivo()));
  }
  @Operation(operationId = "excluirVenda")
  @DeleteMapping("/{id}")
  public ResponseEntity<Void> excluir(@PathVariable UUID id) {
    vendaService.excluir(id);
    return ResponseEntity.noContent().build();
  }
}
