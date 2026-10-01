package com.vilaverde.erp.repository;

import com.vilaverde.erp.entity.UnidadeMedida;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface UnidadeMedidaRepository extends JpaRepository<UnidadeMedida, UUID> {
}
