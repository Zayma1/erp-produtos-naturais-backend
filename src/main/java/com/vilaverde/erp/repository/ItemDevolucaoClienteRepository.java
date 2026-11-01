package com.vilaverde.erp.repository;

import com.vilaverde.erp.entity.ItemDevolucaoCliente;
import java.util.*;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ItemDevolucaoClienteRepository
  extends JpaRepository<ItemDevolucaoCliente, UUID>
{
  List<ItemDevolucaoCliente> findByDevolucaoClienteId(UUID id);
}
