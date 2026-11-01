package com.vilaverde.erp.repository;

import com.vilaverde.erp.entity.ProdutoLoja;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProdutoLojaRepository
  extends JpaRepository<ProdutoLoja, UUID>
{
  List<ProdutoLoja> findByProdutoId(UUID idProduto);

  List<ProdutoLoja> findByLojaId(UUID idLoja);

  boolean existsByProdutoIdAndLojaId(UUID idProduto, UUID idLoja);
}
