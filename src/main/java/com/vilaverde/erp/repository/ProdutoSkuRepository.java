package com.vilaverde.erp.repository;

import com.vilaverde.erp.entity.ProdutoSku;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProdutoSkuRepository extends JpaRepository<ProdutoSku, UUID> {
  List<ProdutoSku> findByProdutoId(UUID idProduto);

  boolean existsBySku(String sku);

  boolean existsBySkuAndIdNot(String sku, UUID id);
}
