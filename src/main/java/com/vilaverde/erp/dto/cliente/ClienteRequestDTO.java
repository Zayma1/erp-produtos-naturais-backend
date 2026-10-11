package com.vilaverde.erp.dto.cliente;

import jakarta.validation.constraints.NotBlank;

import java.time.LocalDate;

public record ClienteRequestDTO(
        @NotBlank(message = "Nome é obrigatório") String nome,
        String cpf,
        LocalDate dataNascimento
) {
}
