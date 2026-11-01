package com.vilaverde.erp.repository;

import com.vilaverde.erp.entity.Cliente;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ClienteRepository extends JpaRepository<Cliente, UUID> {}
