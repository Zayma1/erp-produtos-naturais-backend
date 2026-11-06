package com.vilaverde.erp.config;
import com.vilaverde.erp.repository.UsuarioRepository;
import com.vilaverde.erp.security.*;
import org.springframework.context.annotation.*;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.*;
import org.springframework.security.web.context.*;
import org.springframework.security.web.csrf.*;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
@Configuration
public class SecurityConfig {
  @Bean public PasswordEncoder passwordEncoder() { return new BCryptPasswordEncoder(12); }
  @Bean public SecurityContextRepository securityContextRepository() { return new HttpSessionSecurityContextRepository(); }
  @Bean @ConditionalOnWebApplication(type=ConditionalOnWebApplication.Type.SERVLET)
  public SecurityFilterChain securityFilterChain(HttpSecurity http, UsuarioRepository users,SecurityContextRepository contexts) throws Exception {
    http.csrf(c -> c.csrfTokenRequestHandler(new CsrfTokenRequestAttributeHandler()))
      .securityContext(c -> c.securityContextRepository(contexts))
      .exceptionHandling(e -> e.authenticationEntryPoint((req,res,ex) -> json(res,401,"Autenticação necessária"))
        .accessDeniedHandler((req,res,ex) -> json(res,(org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication()==null || org.springframework.security.authentication.AnonymousAuthenticationToken.class.isInstance(org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication())) ? 401 : 403,"Acesso negado")))
      .authorizeHttpRequests(a -> a
        .requestMatchers("/api/auth/csrf","/api/auth/login").permitAll()
        .requestMatchers("/api/auth/me","/api/auth/logout","/api/auth/alterar-senha").authenticated()
        .requestMatchers("/api/usuarios/**").hasRole("ADMIN")
        .requestMatchers(org.springframework.http.HttpMethod.GET,"/api/lojas/**").authenticated()
        .requestMatchers("/api/lojas/**").hasRole("ADMIN")
        .requestMatchers(org.springframework.http.HttpMethod.DELETE,"/api/compras/**").denyAll()
        .requestMatchers("/api/fornecedores/**","/api/compras/**","/api/recebimentos-compras/**").hasAnyRole("ADMIN","GER")
        .requestMatchers(org.springframework.http.HttpMethod.DELETE,"/api/vendas/**").denyAll()
        .requestMatchers(org.springframework.http.HttpMethod.DELETE,"/api/transferencias-estoque/**").denyAll()
        .requestMatchers(org.springframework.http.HttpMethod.DELETE,"/api/ajustes-estoque/**").denyAll()
        .requestMatchers(org.springframework.http.HttpMethod.DELETE,"/api/devolucoes-clientes/**","/api/devolucoes-fornecedores/**").denyAll()
        .requestMatchers("/api/devolucoes-clientes/**","/api/devolucoes-fornecedores/**").hasAnyRole("ADMIN","GER")
        .requestMatchers("/api/ajustes-estoque/**").hasAnyRole("ADMIN","GER")
        .requestMatchers("/api/transferencias-estoque/**").hasAnyRole("ADMIN","GER")
        .requestMatchers("/api/vendas/**").hasAnyRole("ADMIN","GER","FUNC")
        .requestMatchers(org.springframework.http.HttpMethod.GET,"/api/produtos/**","/api/categorias/**","/api/unidades-medida/**","/api/produtos-sku/**","/api/produtos-loja/**","/api/produtos-precos-loja/**","/api/clientes/**","/api/vendas/**","/api/estoques/**","/api/movimentacoes-estoque/**").authenticated()
        .requestMatchers("/api/produtos/**","/api/categorias/**","/api/unidades-medida/**","/api/produtos-sku/**","/api/produtos-loja/**","/api/produtos-precos-loja/**").hasAnyRole("ADMIN","GER")
        .requestMatchers(org.springframework.http.HttpMethod.POST,"/api/clientes","/api/vendas").authenticated()
        .requestMatchers(org.springframework.http.HttpMethod.PUT,"/api/clientes/*").authenticated()
        .requestMatchers(org.springframework.http.HttpMethod.DELETE,"/api/clientes/*").hasAnyRole("ADMIN","GER")
        .requestMatchers("/v3/api-docs/**").hasRole("ADMIN")
        .anyRequest().denyAll())
      .addFilterAfter(new SessionGuard(users),SecurityContextHolderFilter.class);
    return http.build();
  }
  private static void json(jakarta.servlet.http.HttpServletResponse res,int status,String message) throws java.io.IOException {
    res.setStatus(status);res.setCharacterEncoding("UTF-8");res.setContentType("application/json");res.getWriter().write("{\"message\":\""+message+"\"}");
  }
}
