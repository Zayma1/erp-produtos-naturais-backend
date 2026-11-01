package com.vilaverde.erp.repository;

import com.vilaverde.erp.entity.ItemAjusteEstoque;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ItemAjusteEstoqueRepository
  extends JpaRepository<ItemAjusteEstoque, UUID> {}
