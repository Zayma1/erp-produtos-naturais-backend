package com.vilaverde.erp.repository;

import com.vilaverde.erp.entity.UnidadeMedida;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UnidadeMedidaRepository
  extends JpaRepository<UnidadeMedida, UUID> {}
