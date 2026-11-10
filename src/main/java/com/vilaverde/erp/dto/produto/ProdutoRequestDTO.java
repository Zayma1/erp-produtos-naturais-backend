package com.vilaverde.erp.dto.produto;

import com.vilaverde.erp.entity.enums.StatusProduto;
import com.vilaverde.erp.entity.enums.TipoVenda;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record ProdutoRequestDTO(
  @NotBlank(message = "Nome do produto é obrigatório") String nome,
  @NotNull(message = "Categoria é obrigatória") UUID idCategoria,
  String descricao,
  StatusProduto status
) {}
