package com.vilaverde.erp.repository;

import com.vilaverde.erp.entity.Usuario;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UsuarioRepository extends JpaRepository<Usuario, UUID> {
  boolean existsByEmail(String email);

  boolean existsByEmailAndIdNot(String email, UUID id);
}
