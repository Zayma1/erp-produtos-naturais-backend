package com.vilaverde.erp.dto.transacao;

import com.vilaverde.erp.entity.enums.TipoTransacao;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record TransacaoRequestDTO(
        @NotBlank(message = "Sigla é obrigatória") String sigla,
        @NotBlank(message = "Nome é obrigatório") String nome,
        @NotNull(message = "Tipo da transação é obrigatório") TipoTransacao tipo
) {
}
