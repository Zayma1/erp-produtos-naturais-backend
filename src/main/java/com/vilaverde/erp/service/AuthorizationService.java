package com.vilaverde.erp.service;
import com.vilaverde.erp.repository.UsuarioRepository;
import com.vilaverde.erp.security.AuthPrincipal;
import java.util.Locale;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.security.core.userdetails.*;
@Service @RequiredArgsConstructor
public class AuthorizationService implements UserDetailsService {
  private final UsuarioRepository users;
  public static String normalize(String email) { return email == null ? "" : email.strip().toLowerCase(Locale.ROOT); }
  @Override public AuthPrincipal loadUserByUsername(String email) {
    return AuthPrincipal.of(users.findByEmail(normalize(email)).orElseThrow(() -> new UsernameNotFoundException("Credenciais inválidas")),true);
  }
}
