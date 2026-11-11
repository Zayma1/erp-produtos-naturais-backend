package com.vilaverde.erp.repository;

import com.vilaverde.erp.entity.EstoqueProduto;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EstoqueProdutoRepository
  extends JpaRepository<EstoqueProduto, UUID>, org.springframework.data.jpa.repository.JpaSpecificationExecutor<EstoqueProduto>
{
  List<EstoqueProduto> findByProdutoSkuIdAndLojaId(UUID idProduto, UUID idLoja);

  Optional<EstoqueProduto> findFirstByProdutoSkuIdAndLojaId(
    UUID idProduto,
    UUID idLoja
  );

  List<EstoqueProduto> findByProdutoSkuId(UUID idProduto);

  List<EstoqueProduto> findByLojaId(UUID idLoja);
}
