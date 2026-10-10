package com.vilaverde.erp.dto.categoria;

import jakarta.validation.constraints.NotBlank;

public record CategoriaRequestDTO(
        @NotBlank(message = "Nome da categoria é obrigatório") String nome
) {
}
