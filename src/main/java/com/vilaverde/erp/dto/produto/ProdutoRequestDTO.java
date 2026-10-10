package com.vilaverde.erp.dto.produto;

import com.vilaverde.erp.entity.enums.StatusProduto;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record ProdutoRequestDTO(
        @NotBlank(message = "Nome do produto é obrigatório") String nome,
        @NotNull(message = "Tipo/categoria é obrigatório") UUID idTipo,
        @NotNull(message = "Unidade de medida é obrigatória") UUID idUnidadeMedida,
        StatusProduto status
) {
}
