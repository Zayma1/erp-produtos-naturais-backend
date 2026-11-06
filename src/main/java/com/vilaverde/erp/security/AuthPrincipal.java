package com.vilaverde.erp.security;
import com.vilaverde.erp.entity.Usuario;
import com.vilaverde.erp.entity.enums.*;
import java.util.*;
import org.springframework.security.core.*;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
public record AuthPrincipal(UUID id, String email, String nome, CargoUsuario cargo, Set<UUID> lojas,
  long version, boolean alterarSenha, boolean enabled, String password) implements UserDetails {
  public static AuthPrincipal of(Usuario u, boolean credential) {
    return new AuthPrincipal(u.getId(),u.getEmail(),u.getNome(),u.getCargo(),Set.copyOf(u.getLojas()),
      u.getAuthVersion(),u.isAlterarSenha(),u.isRevisado() && u.getStatus()==StatusCadastro.ATIVO,credential ? u.getSenhaHash() : null);
  }
  @Override public Collection<? extends GrantedAuthority> getAuthorities() { return List.of(new SimpleGrantedAuthority("ROLE_"+cargo.name())); }
  @Override public String getUsername() { return email; }
  @Override @com.fasterxml.jackson.annotation.JsonIgnore public String getPassword() { return password; }
  @Override public boolean isEnabled() { return enabled; }
  public Set<String> permissions() {
    var p = new HashSet<>(Set.of("catalog.read","customers.read","customers.write","sales.read","sales.create","inventory.read","overview.read"));
    if (cargo != CargoUsuario.FUNC) p.addAll(Set.of("catalog.write","suppliers.read","suppliers.write","purchases.read","purchases.write"));
    if (cargo == CargoUsuario.ADMIN) p.addAll(Set.of("users.manage","stores.manage"));
    return Set.copyOf(p);
  }
}
