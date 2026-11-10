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
    return "admin".equals(normalized) ? DEFAULT_ADMIN_EMAIL : normalized;
  }
  @Override public AuthPrincipal loadUserByUsername(String email) {
    return AuthPrincipal.of(users.findByEmail(loginEmail(email)).orElseThrow(() -> new UsernameNotFoundException("Credenciais inválidas")),true);
  }
}
