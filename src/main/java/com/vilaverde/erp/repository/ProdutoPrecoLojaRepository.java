package com.vilaverde.erp.repository;

import com.vilaverde.erp.entity.ProdutoPrecoLoja;
import java.util.*;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProdutoPrecoLojaRepository
  extends JpaRepository<ProdutoPrecoLoja, UUID>, org.springframework.data.jpa.repository.JpaSpecificationExecutor<ProdutoPrecoLoja>
{
  List<ProdutoPrecoLoja> findByProdutoSkuIdAndLojaId(UUID idProduto, UUID idLoja);
}
