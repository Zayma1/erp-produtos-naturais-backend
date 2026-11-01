package com.vilaverde.erp.dto.produto;

import com.vilaverde.erp.entity.enums.StatusProduto;
import com.vilaverde.erp.entity.enums.TipoVenda;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record ProdutoRequestDTO(
  @NotBlank(message = "Nome do produto é obrigatório") String nome,
  @NotNull(message = "Categoria é obrigatória") UUID idCategoria,
  @NotNull(message = "Unidade de medida é obrigatória") UUID idUnidadeMedida,
  String descricao,
  TipoVenda tipoVenda,
  StatusProduto status
) {}
