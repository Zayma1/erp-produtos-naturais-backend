package com.vilaverde.erp.dto.usuario;
import jakarta.validation.constraints.*;
import com.vilaverde.erp.entity.enums.*;
import java.util.*;
public record UsuarioRequestDTO(@NotBlank String nome,@NotBlank @Pattern(regexp="[A-Za-z0-9._-]{3,32}") String usuario,@NotBlank @Email @Size(max=254) String email,
  @Size(min=8,max=72) String senha,@NotNull CargoUsuario cargo,@NotNull StatusCadastro status,@NotNull Set<UUID> lojas) {
  public UsuarioRequestDTO(String nome,String email,String senha,CargoUsuario cargo,StatusCadastro status,Set<UUID> lojas) {
    this(nome,com.vilaverde.erp.service.AuthorizationService.suggest(email),email,senha,cargo,status,lojas);
  }
}
