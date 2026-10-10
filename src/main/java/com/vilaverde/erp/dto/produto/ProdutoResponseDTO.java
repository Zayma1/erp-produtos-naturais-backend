package com.vilaverde.erp.dto.produto;

import com.vilaverde.erp.entity.Produto;
import com.vilaverde.erp.entity.enums.StatusProduto;

import java.util.UUID;

public record ProdutoResponseDTO(
        UUID id,
        String nome,
        UUID idTipo,
        String nomeTipo,
        UUID idUnidadeMedida,
        String siglaUnidadeMedida,
        StatusProduto status
) {
    public static ProdutoResponseDTO fromEntity(Produto p) {
        return new ProdutoResponseDTO(
                p.getId(),
                p.getNome(),
                p.getTipo() != null ? p.getTipo().getId() : null,
                p.getTipo() != null ? p.getTipo().getNome() : null,
                p.getUnidadeMedida() != null ? p.getUnidadeMedida().getId() : null,
                p.getUnidadeMedida() != null ? p.getUnidadeMedida().getSigla() : null,
                p.getStatus()
        );
    }
}
