package com.vilaverde.erp.dto.venda;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.UUID;

public record ProdutoVendaRequestDTO(
  UUID id,
  @NotNull(message = "Produto é obrigatório") UUID idProduto,
  @NotNull(message = "Quantidade é obrigatória")
  @DecimalMin(value = "0.001", message = "Quantidade deve ser maior que zero")
  @Digits(integer=11,fraction=3) BigDecimal quantidade,
  @NotNull(message = "Preço unitário é obrigatório") @DecimalMin("0") @Digits(integer=10,fraction=2) BigDecimal precoUnitario,
  @DecimalMin("0") @Digits(integer=12,fraction=2) BigDecimal desconto
) {}
