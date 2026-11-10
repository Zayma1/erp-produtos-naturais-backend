package com.vilaverde.erp.repository;

import com.vilaverde.erp.entity.RecebimentoCompra;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RecebimentoCompraRepository
  extends JpaRepository<RecebimentoCompra, UUID>, org.springframework.data.jpa.repository.JpaSpecificationExecutor<RecebimentoCompra> {
  @org.springframework.data.jpa.repository.Lock(jakarta.persistence.LockModeType.PESSIMISTIC_WRITE)
  @org.springframework.data.jpa.repository.Query("select e from RecebimentoCompra e where e.id = :id")
  java.util.Optional<RecebimentoCompra> lockById(UUID id);

  java.util.List<RecebimentoCompra> findByCompraId(UUID idCompra);
  boolean existsByCompraId(UUID idCompra);
  @org.springframework.data.jpa.repository.Query("select r.compra.id from RecebimentoCompra r where r.id = :id")
  java.util.Optional<UUID> findCompraIdById(UUID id);
}
