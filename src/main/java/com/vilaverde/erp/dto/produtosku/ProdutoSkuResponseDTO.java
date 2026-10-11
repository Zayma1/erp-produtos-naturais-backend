package com.vilaverde.erp.dto.produtosku;

import com.vilaverde.erp.entity.ProdutoSku;
import com.vilaverde.erp.entity.enums.StatusProduto;

import java.math.BigDecimal;
import java.util.UUID;

public record ProdutoSkuResponseDTO(
        UUID id,
        UUID idProduto,
        String nomeProduto,
        BigDecimal preco,
        StatusProduto status,
        String sku,
        String ean13,
        String dun14,
        UUID idLoja,
        String nomeLoja
) {
    public static ProdutoSkuResponseDTO fromEntity(ProdutoSku s) {
        return new ProdutoSkuResponseDTO(
                s.getId(),
                s.getProduto().getId(),
                s.getProduto().getNome(),
                s.getPreco(),
                s.getStatus(),
                s.getSku(),
                s.getEan13(),
                s.getDun14(),
                s.getLoja() != null ? s.getLoja().getId() : null,
                s.getLoja() != null ? s.getLoja().getNomeFantasia() : null
        );
    }
}
