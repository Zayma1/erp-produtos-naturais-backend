package com.vilaverde.erp.repository;

import com.vilaverde.erp.entity.ItemTransferencia;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ItemTransferenciaRepository
  extends JpaRepository<ItemTransferencia, UUID> {}
