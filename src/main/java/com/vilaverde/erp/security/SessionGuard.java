package com.vilaverde.erp.security;
import com.vilaverde.erp.repository.UsuarioRepository;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import java.io.IOException;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.security.core.context.SecurityContextHolder;
public class SessionGuard extends OncePerRequestFilter {
  private final UsuarioRepository users;
  public SessionGuard(UsuarioRepository users) { this.users=users; }
  @Override protected void doFilterInternal(HttpServletRequest request,HttpServletResponse response,FilterChain chain) throws ServletException,IOException {
    var a=SecurityContextHolder.getContext().getAuthentication();
    if (a!=null && a.getPrincipal() instanceof AuthPrincipal p) {
      var session=request.getSession(false);
      var u=users.findById(p.id()).orElse(null);
      Long login=session==null ? null : (Long)session.getAttribute("authenticatedAt");
      if(u==null || !AuthPrincipal.of(u,false).enabled() || u.getAuthVersion()!=p.version() || login==null || System.currentTimeMillis()-login>=43200000L) {
        if(session!=null)session.invalidate(); SecurityContextHolder.clearContext();
        response.setStatus(401);response.setCharacterEncoding("UTF-8");response.setContentType("application/json");response.getWriter().write("{\"message\":\"Sessão expirada. Entre novamente.\"}");return;
      }
      if(p.alterarSenha() && !java.util.Set.of("/api/auth/me","/api/auth/csrf","/api/auth/logout","/api/auth/alterar-senha").contains(request.getRequestURI())) {
        response.setStatus(403);response.setCharacterEncoding("UTF-8");response.setContentType("application/json");response.getWriter().write("{\"message\":\"Altere sua senha antes de continuar.\",\"code\":\"PASSWORD_CHANGE_REQUIRED\"}");return;
      }
    }
    chain.doFilter(request,response);
  }
}
