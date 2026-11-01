package com.vilaverde.erp.repository;

import com.vilaverde.erp.entity.Categoria;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoriaRepository extends JpaRepository<Categoria, UUID> {
  boolean existsByNome(String nome);

  boolean existsByNomeAndIdNot(String nome, UUID id);
}
