package com.vilaverde.erp.service;
import com.vilaverde.erp.repository.UsuarioRepository;
import com.vilaverde.erp.security.AuthPrincipal;
import java.time.*;
import java.util.*;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
@Service @RequiredArgsConstructor
public class LoginService {
  private final UsuarioRepository users;
  private final PasswordEncoder encoder;
  private final JdbcTemplate jdbc;
  private final AuthorizationService authorization;
  // A valid dummy hash equalizes missing and disabled account checks.
  private final String dummy = new org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder(12).encode(UUID.randomUUID().toString());
  public record Result(int status, AuthPrincipal principal) {}
  @Transactional public Result login(String email,String password) {
    email=AuthorizationService.loginEmail(email);
    var u=authorization.find(email).orElse(null);
    email=u==null ? email : u.getEmail();
    jdbc.update("insert into auth_attempt(email,started_at,failures) values (?,now(),0) on conflict do nothing",email);
    var r=jdbc.queryForMap("select * from auth_attempt where email=? for update",email);
    var now=Instant.now();
    Instant start=((java.sql.Timestamp)r.get("started_at")).toInstant();
    int failures=((Number)r.get("failures")).intValue();
    if(start.plusSeconds(900).isAfter(now) && failures>=5) return new Result(429,null);
    if(!start.plusSeconds(900).isAfter(now)) {failures=0;jdbc.update("update auth_attempt set started_at=now(),failures=0 where email=?",email);}
    boolean valid=password.getBytes(java.nio.charset.StandardCharsets.UTF_8).length<=72 && encoder.matches(password,u!=null && u.isRevisado() ? u.getSenhaHash() : dummy);
    if(u==null || !AuthPrincipal.of(u,false).enabled() || !valid) {
      jdbc.update("update auth_attempt set failures=? where email=?",failures+1,email);
      return new Result(failures+1>=5 ? 429 : 401,null);
    }
    jdbc.update("delete from auth_attempt where email=?",email);
    return new Result(200,AuthPrincipal.of(u,false));
  }
}
