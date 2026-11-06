package com.vilaverde.erp.dto.usuario;
import jakarta.validation.constraints.*;
import com.vilaverde.erp.entity.enums.*;
import java.util.*;
public record UsuarioRequestDTO(@NotBlank String nome,@NotBlank @Email @Size(max=254) String email,
  @Size(min=8,max=72) String senha,@NotNull CargoUsuario cargo,@NotNull StatusCadastro status,@NotNull Set<UUID> lojas) {}
