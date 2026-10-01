package com.vilaverde.erp.dto.produto;

import com.vilaverde.erp.entity.Produto;
import com.vilaverde.erp.entity.enums.StatusProduto;

import java.util.UUID;

public record ProdutoResponseDTO(
        UUID id,
        String nome,
        UUID idCategoria,
        String nomeCategoria,
        UUID idUnidadeMedida,
        String siglaUnidadeMedida,
        StatusProduto status
) {
    public static ProdutoResponseDTO fromEntity(Produto p) {
        return new ProdutoResponseDTO(
                p.getId(),
                p.getNome(),
                p.getCategoria() != null ? p.getCategoria().getId() : null,
                p.getCategoria() != null ? p.getCategoria().getNome() : null,
                p.getUnidadeMedida() != null ? p.getUnidadeMedida().getId() : null,
                p.getUnidadeMedida() != null ? p.getUnidadeMedida().getSigla() : null,
                p.getStatus()
        );
    }
}
