package com.vilaverde.erp.repository;

import com.vilaverde.erp.entity.Loja;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface LojaRepository extends JpaRepository<Loja, UUID>, org.springframework.data.jpa.repository.JpaSpecificationExecutor<Loja> {}
