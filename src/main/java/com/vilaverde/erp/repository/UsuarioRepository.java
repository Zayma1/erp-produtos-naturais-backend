package com.vilaverde.erp.repository;

import com.vilaverde.erp.entity.Usuario;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UsuarioRepository extends JpaRepository<Usuario, UUID> {
  java.util.Optional<Usuario> findByEmail(String email);
  boolean existsByEmail(String email);
  java.util.Optional<Usuario> findByUsuarioIgnoreCase(String usuario);
  boolean existsByUsuarioIgnoreCaseAndIdNot(String usuario, UUID id);

  boolean existsByEmailAndIdNot(String email, UUID id);
}
