package com.vilaverde.erp.repository;

import com.vilaverde.erp.entity.Compra;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CompraRepository extends JpaRepository<Compra, UUID>, org.springframework.data.jpa.repository.JpaSpecificationExecutor<Compra> {
  @org.springframework.data.jpa.repository.Lock(jakarta.persistence.LockModeType.PESSIMISTIC_WRITE)
  @org.springframework.data.jpa.repository.Query("select e from Compra e where e.id = :id")
  java.util.Optional<Compra> lockById(UUID id);
}
