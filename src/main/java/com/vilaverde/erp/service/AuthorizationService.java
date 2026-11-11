package com.vilaverde.erp.service;
import com.vilaverde.erp.repository.UsuarioRepository;
import com.vilaverde.erp.security.AuthPrincipal;
import java.util.Locale;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.security.core.userdetails.*;
@Service @RequiredArgsConstructor
public class AuthorizationService implements UserDetailsService {
  public static final String DEFAULT_ADMIN_EMAIL="admin@vilaverde.local";
  private final UsuarioRepository users;
  public static String normalize(String email) { return email == null ? "" : email.strip().toLowerCase(Locale.ROOT); }
  public static String loginEmail(String login) {
    String normalized=normalize(login);
    return normalized;
  }
  public static String suggest(String email) {
    String v=normalize(email).split("@",2)[0].replaceAll("[^a-z0-9._-]", "");
    if(v.length()<3)v="user";return v.substring(0,Math.min(32,v.length()));
  }
  public java.util.Optional<com.vilaverde.erp.entity.Usuario> find(String login) {
    String v=normalize(login);return v.contains("@")?users.findByEmail(v):users.findByUsuarioIgnoreCase(v);
  }
  @Override public AuthPrincipal loadUserByUsername(String email) {
    return AuthPrincipal.of(find(email).orElseThrow(() -> new UsernameNotFoundException("Credenciais inválidas")),true);
  }
}
