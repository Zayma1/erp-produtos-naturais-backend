package com.vilaverde.erp.repository;

import com.vilaverde.erp.entity.Loja;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface LojaRepository extends JpaRepository<Loja, UUID> {
}
