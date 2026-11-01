package com.vilaverde.erp.repository;

import com.vilaverde.erp.entity.Produto;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProdutoRepository extends JpaRepository<Produto, UUID> {}
