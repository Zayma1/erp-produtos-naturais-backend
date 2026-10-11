package com.vilaverde.erp.dto.unidademedida;

import com.vilaverde.erp.entity.UnidadeMedida;

import java.util.UUID;

public record UnidadeMedidaResponseDTO(
        UUID id,
        String sigla,
        String nome
) {
    public static UnidadeMedidaResponseDTO fromEntity(UnidadeMedida unidade) {
        return new UnidadeMedidaResponseDTO(unidade.getId(), unidade.getSigla(), unidade.getNome());
    }
}
