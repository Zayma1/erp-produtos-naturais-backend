package com.vilaverde.erp.repository;

import com.vilaverde.erp.entity.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UsuarioRepository extends JpaRepository<Usuario, UUID> {
  java.util.Optional<Usuario> findByEmail(String email);
  boolean existsByEmail(String email);

    boolean existsByEmailAndIdNot(String email, UUID id);

    UserDetails findByUuid(UUID uuid);
}
