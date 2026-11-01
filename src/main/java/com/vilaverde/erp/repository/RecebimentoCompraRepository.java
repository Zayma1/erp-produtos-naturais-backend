package com.vilaverde.erp.repository;

import com.vilaverde.erp.entity.RecebimentoCompra;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RecebimentoCompraRepository
  extends JpaRepository<RecebimentoCompra, UUID> {}
