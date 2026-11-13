package com.vilaverde.erp.repository;

import com.vilaverde.erp.entity.UnidadeMedida;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

public interface UnidadeMedidaRepository
  extends JpaRepository<UnidadeMedida, UUID> {
    @Query("select e from UnidadeMedida e where e.nome = :nome")
    Optional<UnidadeMedida> findByName(String nome);
}
