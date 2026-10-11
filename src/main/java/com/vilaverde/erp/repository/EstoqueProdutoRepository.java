package com.vilaverde.erp.repository;

import com.vilaverde.erp.entity.EstoqueProduto;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface EstoqueProdutoRepository extends JpaRepository<EstoqueProduto, UUID> {

    List<EstoqueProduto> findByProdutoIdAndLojaId(UUID idProduto, UUID idLoja);

    Optional<EstoqueProduto> findFirstByProdutoIdAndLojaId(UUID idProduto, UUID idLoja);

    List<EstoqueProduto> findByProdutoId(UUID idProduto);

    List<EstoqueProduto> findByLojaId(UUID idLoja);
}
