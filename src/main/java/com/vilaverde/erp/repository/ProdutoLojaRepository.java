package com.vilaverde.erp.repository;

import com.vilaverde.erp.entity.ProdutoLoja;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProdutoLojaRepository
  extends JpaRepository<ProdutoLoja, UUID>, org.springframework.data.jpa.repository.JpaSpecificationExecutor<ProdutoLoja>
{
  List<ProdutoLoja> findByProdutoSkuId(UUID idProduto);

  List<ProdutoLoja> findByLojaId(UUID idLoja);

  boolean existsByProdutoSkuIdAndLojaId(UUID idProduto, UUID idLoja);
}
