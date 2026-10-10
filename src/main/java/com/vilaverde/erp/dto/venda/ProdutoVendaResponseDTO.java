package com.vilaverde.erp.dto.venda;

import com.vilaverde.erp.entity.ProdutoVenda;

import java.math.BigDecimal;
import java.util.UUID;

public record ProdutoVendaResponseDTO(
        UUID id,
        UUID idProduto,
        String nomeProduto,
        BigDecimal quantidade,
        BigDecimal precoUnitario,
        BigDecimal desconto
) {
    public static ProdutoVendaResponseDTO fromEntity(ProdutoVenda item) {
        return new ProdutoVendaResponseDTO(
                item.getId(),
                item.getProduto().getId(),
                item.getProduto().getNome(),
                item.getQuantidade(),
                item.getPrecoUnitario(),
                item.getDesconto()
        );
    }
}
