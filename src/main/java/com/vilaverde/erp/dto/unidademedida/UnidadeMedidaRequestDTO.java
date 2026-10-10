package com.vilaverde.erp.dto.unidademedida;

import jakarta.validation.constraints.NotBlank;

public record UnidadeMedidaRequestDTO(
        @NotBlank(message = "Sigla é obrigatória") String sigla,
        @NotBlank(message = "Nome é obrigatório") String nome
) {
}
