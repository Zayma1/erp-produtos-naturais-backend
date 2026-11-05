package com.vilaverde.erp.entity;

import com.vilaverde.erp.entity.enums.CargoUsuario;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import java.time.OffsetDateTime;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.jspecify.annotations.Nullable;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

@Entity
@Table(name = "usuario")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Usuario implements UserDetails {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @NotBlank(message = "Nome é obrigatório")
  @Column(name = "nome", nullable = false)
  private String nome;

  @NotBlank(message = "E-mail é obrigatório")
  @Column(name = "email", nullable = false, unique = true)
  private String email;

  @NotBlank(message = "Senha é obrigatória")
  @Column(name = "senha_hash", nullable = false)
  private String senhaHash;

  @Enumerated(EnumType.STRING)
  @Column(name = "status", nullable = false, length = 20)
  private com.vilaverde.erp.entity.enums.StatusCadastro status =
    com.vilaverde.erp.entity.enums.StatusCadastro.ATIVO;

  @Column(name = "criado_em", nullable = false)
  private OffsetDateTime criadoEm = OffsetDateTime.now();
    @NotBlank
    @Column(name = "cargo", nullable = false)
    private CargoUsuario cargo = CargoUsuario.FUNC;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        if(this.cargo == CargoUsuario.ADMIN)
            return List.of(
                    new SimpleGrantedAuthority("ROLE_ADMIN"),
                    new SimpleGrantedAuthority("ROLE_MENAGER"),
                    new SimpleGrantedAuthority("ROLE_USER")
            );
        else if(this.cargo == CargoUsuario.GER)
            return List.of(
                    new SimpleGrantedAuthority("ROLE_MENAGER"),
                    new SimpleGrantedAuthority("ROLE_USER")
            );
        else
            return List.of (new SimpleGrantedAuthority("ROLE_USER"));
    }

    @Override
    public @Nullable String getPassword() {
        return this.senhaHash;
    }

    @Override
    public String getUsername() {
        return nome;
    }

    @Override
    public boolean isAccountNonExpired() {
        return UserDetails.super.isAccountNonExpired();
    }

    @Override
    public boolean isAccountNonLocked() {
        return UserDetails.super.isAccountNonLocked();
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return UserDetails.super.isCredentialsNonExpired();
    }

    @Override
    public boolean isEnabled() {
        return UserDetails.super.isEnabled();
    }
}
