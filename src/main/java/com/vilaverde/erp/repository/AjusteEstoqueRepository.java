package com.vilaverde.erp.repository;

import com.vilaverde.erp.entity.AjusteEstoque;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AjusteEstoqueRepository
  extends JpaRepository<AjusteEstoque, UUID> {}
