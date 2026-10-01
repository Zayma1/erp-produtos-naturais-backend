package com.vilaverde.erp.dto.produtosku;

import com.vilaverde.erp.entity.enums.StatusProduto;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.util.UUID;

public record ProdutoSkuRequestDTO(
        @NotNull(message = "Produto é obrigatório") UUID idProduto,
        @NotNull(message = "Loja é obrigatória") UUID idLoja,
        @NotNull(message = "Preço é obrigatório") BigDecimal preco,
        StatusProduto status,
        @NotBlank(message = "SKU é obrigatório") String sku,
        String ean13,
        String dun14
) {
}
