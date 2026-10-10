package com.vilaverde.erp.dto.usuario;

import com.vilaverde.erp.entity.Usuario;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public record UsuarioResponseDTO(
        UUID id,
        String nome,
        String email,
        LocalDateTime criacaoTimestamp,
        LocalDate dataCriacao,
        String cargo
) {
    public static UsuarioResponseDTO fromEntity(Usuario usuario) {
        return new UsuarioResponseDTO(
                usuario.getId(),
                usuario.getNome(),
                usuario.getEmail(),
                usuario.getCriacaoTimestamp(),
                usuario.getDataCriacao(),
                usuario.getCargo()
        );
    }
}
