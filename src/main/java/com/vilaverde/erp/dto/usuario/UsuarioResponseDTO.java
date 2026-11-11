package com.vilaverde.erp.dto.usuario;
import com.vilaverde.erp.entity.Usuario;
import com.vilaverde.erp.entity.enums.*;
import java.time.OffsetDateTime;
import java.util.*;
public record UsuarioResponseDTO(UUID id,String nome,String usuario,String email,CargoUsuario cargo,StatusCadastro status,
  OffsetDateTime criadoEm,Set<UUID> lojas,boolean alterarSenha,boolean revisado) {
  public static UsuarioResponseDTO fromEntity(Usuario u) { return new UsuarioResponseDTO(u.getId(),u.getNome(),u.getUsuario(),u.getEmail(),u.getCargo(),u.getStatus(),u.getCriadoEm(),Set.copyOf(u.getLojas()),u.isAlterarSenha(),u.isRevisado()); }
}
