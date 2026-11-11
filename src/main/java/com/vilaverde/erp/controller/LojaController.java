package com.vilaverde.erp.controller;

import com.vilaverde.erp.dto.loja.LojaRequestDTO;
import com.vilaverde.erp.dto.loja.LojaResponseDTO;
import com.vilaverde.erp.service.LojaService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/lojas")
@RequiredArgsConstructor
public class LojaController {

  private final LojaService lojaService;

  @Operation(operationId = "listarLojas")
  @GetMapping
  public ResponseEntity<List<LojaResponseDTO>> listar() {
    return ResponseEntity.ok(lojaService.listar());
  }

  @Operation(operationId = "buscarLojaPorId")
  @GetMapping("/{id}")
  public ResponseEntity<LojaResponseDTO> buscarPorId(@PathVariable UUID id) {
    return ResponseEntity.ok(lojaService.buscarPorId(id));
  }

  @Operation(operationId = "criarLoja")
  @io.swagger.v3.oas.annotations.responses.ApiResponses({@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode="400",description="Formato, tamanho ou combinação de campos inválida",content=@io.swagger.v3.oas.annotations.media.Content(schema=@io.swagger.v3.oas.annotations.media.Schema(implementation=com.vilaverde.erp.exception.ApiError.class))),@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode="409",description="Documento ou nome único já cadastrado",content=@io.swagger.v3.oas.annotations.media.Content(schema=@io.swagger.v3.oas.annotations.media.Schema(implementation=com.vilaverde.erp.exception.ApiError.class))),@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode="503",description="Catálogo IBGE indisponível sem cache utilizável",content=@io.swagger.v3.oas.annotations.media.Content(schema=@io.swagger.v3.oas.annotations.media.Schema(implementation=com.vilaverde.erp.exception.ApiError.class)))})
  @PostMapping
  public ResponseEntity<LojaResponseDTO> criar(
    @Valid @RequestBody LojaRequestDTO dto
  ) {
    return ResponseEntity.status(HttpStatus.CREATED).body(
      lojaService.criar(dto)
    );
  }

  @Operation(operationId = "atualizarLoja")
  @io.swagger.v3.oas.annotations.responses.ApiResponses({@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode="400",description="Formato, tamanho ou combinação de campos inválida",content=@io.swagger.v3.oas.annotations.media.Content(schema=@io.swagger.v3.oas.annotations.media.Schema(implementation=com.vilaverde.erp.exception.ApiError.class))),@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode="409",description="Documento ou nome único já cadastrado",content=@io.swagger.v3.oas.annotations.media.Content(schema=@io.swagger.v3.oas.annotations.media.Schema(implementation=com.vilaverde.erp.exception.ApiError.class))),@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode="503",description="Catálogo IBGE indisponível sem cache utilizável",content=@io.swagger.v3.oas.annotations.media.Content(schema=@io.swagger.v3.oas.annotations.media.Schema(implementation=com.vilaverde.erp.exception.ApiError.class)))})
  @PutMapping("/{id}")
  public ResponseEntity<LojaResponseDTO> atualizar(
    @PathVariable UUID id,
    @Valid @RequestBody LojaRequestDTO dto
  ) {
    return ResponseEntity.ok(lojaService.atualizar(id, dto));
  }

  @Operation(operationId = "excluirLoja")
  @DeleteMapping("/{id}")
  public ResponseEntity<Void> excluir(@PathVariable UUID id) {
    lojaService.excluir(id);
    return ResponseEntity.noContent().build();
  }
}
