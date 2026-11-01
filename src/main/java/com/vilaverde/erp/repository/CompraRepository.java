package com.vilaverde.erp.repository;

import com.vilaverde.erp.entity.Compra;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CompraRepository extends JpaRepository<Compra, UUID> {}
