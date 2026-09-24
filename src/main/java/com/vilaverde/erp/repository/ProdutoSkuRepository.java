package com.vilaverde.erp.repository;

import com.vilaverde.erp.entity.ProdutoSku;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ProdutoSkuRepository extends JpaRepository<ProdutoSku, UUID> {
    List<ProdutoSku> findByProdutoId(UUID idProduto);
}
