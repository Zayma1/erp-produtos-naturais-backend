package com.vilaverde.erp.dto.venda;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.util.UUID;

public record ProdutoVendaRequestDTO(
        @NotNull(message = "Produto é obrigatório") UUID idProduto,
        @NotNull(message = "Quantidade é obrigatória")
        @DecimalMin(value = "0.001", message = "Quantidade deve ser maior que zero") BigDecimal quantidade,
        @NotNull(message = "Preço unitário é obrigatório") BigDecimal precoUnitario,
        BigDecimal desconto
) {
}
