package com.vilaverde.erp.repository;

import com.vilaverde.erp.entity.ProdutoVenda;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProdutoVendaRepository
  extends JpaRepository<ProdutoVenda, UUID> {}
