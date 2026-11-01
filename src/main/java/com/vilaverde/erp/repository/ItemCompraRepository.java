package com.vilaverde.erp.repository;

import com.vilaverde.erp.entity.ItemCompra;
import java.util.*;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ItemCompraRepository extends JpaRepository<ItemCompra, UUID> {
  List<ItemCompra> findByCompraId(UUID idCompra);
}
