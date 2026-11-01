package com.vilaverde.erp.repository;

import com.vilaverde.erp.entity.Fornecedor;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FornecedorRepository extends JpaRepository<Fornecedor, UUID> {}
