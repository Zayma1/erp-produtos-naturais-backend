package com.vilaverde.erp.repository;

import com.vilaverde.erp.entity.ItemRecebimentoCompra;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ItemRecebimentoCompraRepository
  extends JpaRepository<ItemRecebimentoCompra, UUID> { java.util.List<ItemRecebimentoCompra> findByRecebimentoCompraId(UUID id); }
