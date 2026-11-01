package com.vilaverde.erp.repository;

import com.vilaverde.erp.entity.Loja;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LojaRepository extends JpaRepository<Loja, UUID> {}
