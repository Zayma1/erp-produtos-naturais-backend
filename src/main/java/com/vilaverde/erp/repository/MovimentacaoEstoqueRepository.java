package com.vilaverde.erp.repository;

import com.vilaverde.erp.entity.MovimentacaoEstoque;
import java.util.*;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MovimentacaoEstoqueRepository
  extends JpaRepository<MovimentacaoEstoque, UUID>
{
  List<MovimentacaoEstoque> findByProdutoIdAndLojaId(
    UUID idProduto,
    UUID idLoja
  );
}
