package com.vilaverde.erp.repository;

import com.vilaverde.erp.entity.ItemDevolucaoFornecedor;
import java.util.*;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ItemDevolucaoFornecedorRepository
  extends JpaRepository<ItemDevolucaoFornecedor, UUID>
{
  List<ItemDevolucaoFornecedor> findByDevolucaoFornecedorId(UUID id);
}
