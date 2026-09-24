package com.vilaverde.erp.dto.estoque;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.util.UUID;

public record MovimentacaoRequestDTO(
        @NotNull(message = "Produto é obrigatório") UUID idProduto,
        @NotNull(message = "Loja é obrigatória") UUID idLoja,
        UUID idFornecedor,
        @NotNull(message = "Transação (tipo de movimentação) é obrigatória") UUID idTransacao,
        @NotNull(message = "Quantidade é obrigatória")
        @DecimalMin(value = "0.001", message = "Quantidade deve ser maior que zero") BigDecimal quantidade,
        String motivo
) {
}
