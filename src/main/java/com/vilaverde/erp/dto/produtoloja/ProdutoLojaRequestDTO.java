package com.vilaverde.erp.dto.produtoloja;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record ProdutoLojaRequestDTO(
        @NotNull(message = "Produto é obrigatório") UUID idProduto,
        @NotNull(message = "Loja é obrigatória") UUID idLoja
) {
}
