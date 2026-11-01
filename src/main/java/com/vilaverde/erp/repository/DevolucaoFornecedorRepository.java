package com.vilaverde.erp.repository;

import com.vilaverde.erp.entity.DevolucaoFornecedor;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DevolucaoFornecedorRepository
  extends JpaRepository<DevolucaoFornecedor, UUID> {}
