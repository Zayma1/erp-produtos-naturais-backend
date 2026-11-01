package com.vilaverde.erp.repository;

import com.vilaverde.erp.entity.ProdutoPrecoLoja;
import java.util.*;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProdutoPrecoLojaRepository
  extends JpaRepository<ProdutoPrecoLoja, UUID>
{
  List<ProdutoPrecoLoja> findByProdutoIdAndLojaId(UUID idProduto, UUID idLoja);
}
