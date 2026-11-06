package com.vilaverde.erp.controller;

import com.vilaverde.erp.dto.usuario.UsuarioRequestDTO;
import com.vilaverde.erp.dto.usuario.UsuarioResponseDTO;
import com.vilaverde.erp.service.UsuarioService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/usuarios")
@RequiredArgsConstructor
public class UsuarioController {

  private final UsuarioService usuarioService;

  @Operation(operationId = "listarUsuarios")
  @GetMapping
  public ResponseEntity<List<UsuarioResponseDTO>> listar() {
    return ResponseEntity.ok(usuarioService.listar());
  }

  @Operation(operationId = "buscarUsuarioPorId")
  @GetMapping("/{id}")
  public ResponseEntity<UsuarioResponseDTO> buscarPorId(@PathVariable UUID id) {
    return ResponseEntity.ok(usuarioService.buscarPorId(id));
  }

  @Operation(operationId = "criarUsuario")
  @PostMapping
  public ResponseEntity<UsuarioResponseDTO> criar(
    @Valid @RequestBody UsuarioRequestDTO dto
  ) {
    return ResponseEntity.status(HttpStatus.CREATED).body(
      usuarioService.criar(dto)
    );
  }

  @Operation(operationId = "atualizarUsuario")
  @PutMapping("/{id}")
  public ResponseEntity<UsuarioResponseDTO> atualizar(
    @PathVariable UUID id,
    @Valid @RequestBody UsuarioRequestDTO dto
  ) {
    return ResponseEntity.ok(usuarioService.atualizar(id, dto));
  }

  public record Reset(@jakarta.validation.constraints.NotBlank @jakarta.validation.constraints.Size(min=12,max=72) String senha) {}
  @PostMapping("/{id}/senha-temporaria")
  @Operation(operationId="senhaTemporaria")
  public ResponseEntity<Void> reset(@PathVariable UUID id,@Valid @RequestBody Reset d) {
    usuarioService.reset(id,d.senha());return ResponseEntity.noContent().build();
  }

  @Operation(operationId = "excluirUsuario")
  @DeleteMapping("/{id}")
  public ResponseEntity<Void> excluir(@PathVariable UUID id) {
    usuarioService.excluir(id);
    return ResponseEntity.noContent().build();
  }
}
