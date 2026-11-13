package com.vilaverde.erp.security;
import com.vilaverde.erp.entity.Usuario;
import com.vilaverde.erp.entity.enums.CargoUsuario;
import com.vilaverde.erp.repository.UsuarioRepository;
import com.vilaverde.erp.exception.ResourceNotFoundException;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.access.AccessDeniedException;
@Component
@RequiredArgsConstructor
public class AccessScope {
  private final UsuarioRepository users;
  public AuthPrincipal principal() {
    var a=SecurityContextHolder.getContext().getAuthentication();
    if(a==null || !(a.getPrincipal() instanceof AuthPrincipal p)) throw new AccessDeniedException("Autenticação necessária");
    return p;
  }
  public boolean permits(UUID loja) { var p=principal(); return p.cargo()==CargoUsuario.ADMIN || p.lojas().contains(loja); }
  public <T> java.util.List<T> rows(org.springframework.data.jpa.repository.JpaSpecificationExecutor<T> repository,String path) {
    var p=principal();
    return repository.findAll((org.springframework.data.jpa.domain.Specification<T>)(root,query,cb) -> {
      if(p.cargo()==CargoUsuario.ADMIN)return cb.conjunction();
      jakarta.persistence.criteria.Path<?> field=root;
      for(String part:path.split("\\."))field=field.get(part);
      return p.lojas().isEmpty() ? cb.disjunction() : field.in(p.lojas());
    });
  }
  public void store(UUID loja) { if (!permits(loja)) throw new ResourceNotFoundException("Recurso não encontrado"); }
  public Usuario actor(UUID submitted) {
    var p=principal();
    if(submitted!=null && !p.id().equals(submitted)) throw new AccessDeniedException("Operador deve ser o usuário autenticado");
    return users.findById(p.id()).orElseThrow();
  }
  public Usuario actor() { return actor(null); }
}
