package com.vilaverde.erp.dto.produtoloja;

import com.vilaverde.erp.entity.ProdutoLoja;

import java.util.UUID;

public record ProdutoLojaResponseDTO(
        UUID id,
        UUID idProduto,
        String nomeProduto,
        UUID idLoja,
        String nomeLoja
) {
    public static ProdutoLojaResponseDTO fromEntity(ProdutoLoja vinculo) {
        return new ProdutoLojaResponseDTO(
                vinculo.getId(),
                vinculo.getProduto().getId(),
                vinculo.getProduto().getNome(),
                vinculo.getLoja().getId(),
                vinculo.getLoja().getNomeFantasia()
        );
    }
}
