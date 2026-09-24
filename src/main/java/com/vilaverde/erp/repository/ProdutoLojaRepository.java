package com.vilaverde.erp.repository;

import com.vilaverde.erp.entity.ProdutoLoja;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ProdutoLojaRepository extends JpaRepository<ProdutoLoja, UUID> {
    List<ProdutoLoja> findByProdutoId(UUID idProduto);
    List<ProdutoLoja> findByLojaId(UUID idLoja);
}
