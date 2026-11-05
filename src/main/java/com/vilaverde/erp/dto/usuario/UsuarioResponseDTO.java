package com.vilaverde.erp.dto.usuario;

import com.vilaverde.erp.entity.Usuario;
import com.vilaverde.erp.entity.enums.CargoUsuario;

import java.time.OffsetDateTime;
import java.util.UUID;

public record UsuarioResponseDTO(
  UUID id,
  String nome,
  String email,
  CargoUsuario cargo,
  com.vilaverde.erp.entity.enums.StatusCadastro status,
  OffsetDateTime criadoEm
) {
  public static UsuarioResponseDTO fromEntity(Usuario usuario) {
    return new UsuarioResponseDTO(
      usuario.getId(),
      usuario.getNome(),
      usuario.getEmail(),
      usuario.getCargo(),
      usuario.getStatus(),
      usuario.getCriadoEm()
    );
  }
}
