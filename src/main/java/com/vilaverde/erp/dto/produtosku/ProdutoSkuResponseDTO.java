package com.vilaverde.erp.dto.produtosku;

import com.vilaverde.erp.entity.ProdutoSku;
import com.vilaverde.erp.entity.enums.StatusProduto;

import java.math.BigDecimal;
import java.util.UUID;

public record ProdutoSkuResponseDTO(
        UUID id,
        UUID idProduto,
        String nomeProduto,
        UUID idLoja,
        String nomeLoja,
        BigDecimal preco,
        StatusProduto status,
        String sku,
        String ean13,
        String dun14
) {
    public static ProdutoSkuResponseDTO fromEntity(ProdutoSku s) {
        return new ProdutoSkuResponseDTO(
                s.getId(),
                s.getProduto().getId(),
                s.getProduto().getNome(),
                s.getLoja().getId(),
                s.getLoja().getNomeFantasia(),
                s.getPreco(),
                s.getStatus(),
                s.getSku(),
                s.getEan13(),
                s.getDun14()
        );
    }
}
