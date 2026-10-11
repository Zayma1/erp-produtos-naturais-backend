package com.vilaverde.erp.repository;

import com.vilaverde.erp.entity.Venda;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface VendaRepository extends JpaRepository<Venda, UUID> {

    @Override
    @EntityGraph(attributePaths = {"cliente", "usuario", "loja", "itens", "itens.produto"})
    List<Venda> findAll();

    @Override
    @EntityGraph(attributePaths = {"cliente", "usuario", "loja", "itens", "itens.produto"})
    Optional<Venda> findById(UUID id);

    @EntityGraph(attributePaths = {"cliente", "usuario", "loja", "itens", "itens.produto"})
    List<Venda> findByLojaId(UUID idLoja);

    @EntityGraph(attributePaths = {"cliente", "usuario", "loja", "itens", "itens.produto"})
    List<Venda> findByClienteId(UUID idCliente);

    @EntityGraph(attributePaths = {"cliente", "usuario", "loja", "itens", "itens.produto"})
    List<Venda> findByUsuarioId(UUID idUsuario);
}
