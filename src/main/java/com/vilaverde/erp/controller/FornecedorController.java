package com.vilaverde.erp.controller;

import com.vilaverde.erp.dto.fornecedor.FornecedorRequestDTO;
import com.vilaverde.erp.dto.fornecedor.FornecedorResponseDTO;
import com.vilaverde.erp.service.FornecedorService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/fornecedores")
@RequiredArgsConstructor
public class FornecedorController {

  private final FornecedorService fornecedorService;

  @Operation(operationId = "listarFornecedores")
  @GetMapping
  public ResponseEntity<List<FornecedorResponseDTO>> listar() {
    return ResponseEntity.ok(fornecedorService.listar());
  }

  @Operation(operationId = "buscarFornecedorPorId")
  @GetMapping("/{id}")
  public ResponseEntity<FornecedorResponseDTO> buscarPorId(
    @PathVariable UUID id
  ) {
    return ResponseEntity.ok(fornecedorService.buscarPorId(id));
  }

  @Operation(operationId = "criarFornecedor")
  @io.swagger.v3.oas.annotations.responses.ApiResponses({@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode="400",description="Formato, tamanho ou combinação de campos inválida",content=@io.swagger.v3.oas.annotations.media.Content(schema=@io.swagger.v3.oas.annotations.media.Schema(implementation=com.vilaverde.erp.exception.ApiError.class))),@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode="409",description="Documento ou nome único já cadastrado",content=@io.swagger.v3.oas.annotations.media.Content(schema=@io.swagger.v3.oas.annotations.media.Schema(implementation=com.vilaverde.erp.exception.ApiError.class))),@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode="503",description="Catálogo IBGE indisponível sem cache utilizável",content=@io.swagger.v3.oas.annotations.media.Content(schema=@io.swagger.v3.oas.annotations.media.Schema(implementation=com.vilaverde.erp.exception.ApiError.class)))})
  @PostMapping
  public ResponseEntity<FornecedorResponseDTO> criar(
    @Valid @RequestBody FornecedorRequestDTO dto
  ) {
    return ResponseEntity.status(HttpStatus.CREATED).body(
      fornecedorService.criar(dto)
    );
  }

  @Operation(operationId = "atualizarFornecedor")
  @io.swagger.v3.oas.annotations.responses.ApiResponses({@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode="400",description="Formato, tamanho ou combinação de campos inválida",content=@io.swagger.v3.oas.annotations.media.Content(schema=@io.swagger.v3.oas.annotations.media.Schema(implementation=com.vilaverde.erp.exception.ApiError.class))),@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode="409",description="Documento ou nome único já cadastrado",content=@io.swagger.v3.oas.annotations.media.Content(schema=@io.swagger.v3.oas.annotations.media.Schema(implementation=com.vilaverde.erp.exception.ApiError.class))),@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode="503",description="Catálogo IBGE indisponível sem cache utilizável",content=@io.swagger.v3.oas.annotations.media.Content(schema=@io.swagger.v3.oas.annotations.media.Schema(implementation=com.vilaverde.erp.exception.ApiError.class)))})
  @PutMapping("/{id}")
  public ResponseEntity<FornecedorResponseDTO> atualizar(
    @PathVariable UUID id,
    @Valid @RequestBody FornecedorRequestDTO dto
  ) {
    return ResponseEntity.ok(fornecedorService.atualizar(id, dto));
  }

  @Operation(operationId = "excluirFornecedor")
  @DeleteMapping("/{id}")
  public ResponseEntity<Void> excluir(@PathVariable UUID id) {
    fornecedorService.excluir(id);
    return ResponseEntity.noContent().build();
  }
}
