package com.vilaverde.erp.repository;

import com.vilaverde.erp.entity.DevolucaoCliente;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DevolucaoClienteRepository
  extends JpaRepository<DevolucaoCliente, UUID> {}
