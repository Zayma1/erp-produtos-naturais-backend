package com.vilaverde.erp.controller;
import com.vilaverde.erp.security.*;
import com.vilaverde.erp.service.*;
import jakarta.servlet.http.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.util.*;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.security.web.csrf.*;
import io.swagger.v3.oas.annotations.Operation;
@RestController @RequestMapping("/api/auth") @RequiredArgsConstructor
public class AuthController {
  private final LoginService login;
  private final AccessScope scope;
  private final UsuarioService users;
  private final SecurityContextRepository contexts;
  public record Login(@NotBlank @Size(max=254) String email,@NotBlank @Size(max=72) String senha) {}
  public record Password(@NotBlank String senhaAtual,@NotBlank @Size(min=8,max=72) String novaSenha) {}
  public record Identity(UUID id,String nome,String email,String cargo,Set<UUID> lojas,Set<String> permissions,boolean alterarSenha) {
    public static Identity of(AuthPrincipal p) { return new Identity(p.id(),p.nome(),p.email(),p.cargo().name(),p.lojas(),p.permissions(),p.alterarSenha()); }
  }
  public record Csrf(String token,String headerName) {}
  public record Failure(String message) {}
  @GetMapping("/csrf") @Operation(operationId="obterCsrf")
  public Csrf csrf(@io.swagger.v3.oas.annotations.Parameter(hidden=true) CsrfToken token) { return new Csrf(token.getToken(),token.getHeaderName()); }
  @PostMapping("/login") @Operation(operationId="login")
  @io.swagger.v3.oas.annotations.responses.ApiResponses({
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode="200",description="Sessão autenticada",content=@io.swagger.v3.oas.annotations.media.Content(schema=@io.swagger.v3.oas.annotations.media.Schema(implementation=Identity.class))),
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode="401",description="Credenciais inválidas",content=@io.swagger.v3.oas.annotations.media.Content(schema=@io.swagger.v3.oas.annotations.media.Schema(implementation=Failure.class))),
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode="429",description="Bloqueio temporário de tentativas",content=@io.swagger.v3.oas.annotations.media.Content(schema=@io.swagger.v3.oas.annotations.media.Schema(implementation=Failure.class)))
  })
  public ResponseEntity<?> login(@Valid @RequestBody Login d,HttpServletRequest req,HttpServletResponse res) {
    var r=login.login(d.email(),d.senha());
    if(r.status()!=200) return ResponseEntity.status(r.status()).body(Map.of("message",r.status()==429 ? "Muitas tentativas. Aguarde 15 minutos." : "E-mail ou senha inválidos."));
    req.getSession();req.changeSessionId();req.getSession().setAttribute("authenticatedAt",System.currentTimeMillis());
    var context=SecurityContextHolder.createEmptyContext();
    context.setAuthentication(UsernamePasswordAuthenticationToken.authenticated(r.principal(),null,r.principal().getAuthorities()));
    SecurityContextHolder.setContext(context);contexts.saveContext(context,req,res);
    new HttpSessionCsrfTokenRepository().saveToken(null,req,res);
    return ResponseEntity.ok(Identity.of(r.principal()));
  }
  @GetMapping("/me") @Operation(operationId="minhaConta") public Identity me() { return Identity.of(scope.principal()); }
  @org.springframework.web.bind.annotation.ResponseStatus(org.springframework.http.HttpStatus.NO_CONTENT)
  @PostMapping("/logout") @Operation(operationId="logout") public ResponseEntity<Void> logout(HttpServletRequest req) {
    var s=req.getSession(false);if(s!=null)s.invalidate();SecurityContextHolder.clearContext();return ResponseEntity.noContent().build();
  }
  @org.springframework.web.bind.annotation.ResponseStatus(org.springframework.http.HttpStatus.NO_CONTENT)
  @PostMapping("/alterar-senha") @Operation(operationId="alterarSenha") public ResponseEntity<Void> password(@Valid @RequestBody Password d,HttpServletRequest req) {
    users.changePassword(scope.principal().id(),d.senhaAtual(),d.novaSenha());
    var s=req.getSession(false);if(s!=null)s.invalidate();SecurityContextHolder.clearContext();return ResponseEntity.noContent().build();
  }
}
