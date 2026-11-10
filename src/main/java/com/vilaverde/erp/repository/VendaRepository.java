package com.vilaverde.erp.repository;

import com.vilaverde.erp.entity.Venda;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VendaRepository extends JpaRepository<Venda, UUID>, org.springframework.data.jpa.repository.JpaSpecificationExecutor<Venda> {
  @org.springframework.data.jpa.repository.Lock(jakarta.persistence.LockModeType.PESSIMISTIC_WRITE)
  @org.springframework.data.jpa.repository.Query("select v from Venda v where v.id=:id")
  Optional<Venda> lockById(UUID id);

  @Override
  @EntityGraph(
    attributePaths = { "cliente", "usuario", "loja", "itens", "itens.produto" }
  )
  List<Venda> findAll();

  @Override
  @EntityGraph(
    attributePaths = { "cliente", "usuario", "loja", "itens", "itens.produto" }
  )
  Optional<Venda> findById(UUID id);

  @EntityGraph(
    attributePaths = { "cliente", "usuario", "loja", "itens", "itens.produto" }
  )
  List<Venda> findByLojaId(UUID idLoja);

  @EntityGraph(
    attributePaths = { "cliente", "usuario", "loja", "itens", "itens.produto" }
  )
  List<Venda> findByClienteId(UUID idCliente);

  @EntityGraph(
    attributePaths = { "cliente", "usuario", "loja", "itens", "itens.produto" }
  )
  List<Venda> findByUsuarioId(UUID idUsuario);
}
