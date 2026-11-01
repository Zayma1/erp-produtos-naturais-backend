package com.vilaverde.erp.repository;

import com.vilaverde.erp.entity.TransferenciaEstoque;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TransferenciaEstoqueRepository
  extends JpaRepository<TransferenciaEstoque, UUID> {}
