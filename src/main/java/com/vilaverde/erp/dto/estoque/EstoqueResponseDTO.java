package com.vilaverde.erp.dto.estoque;

import com.vilaverde.erp.entity.EstoqueProduto;

import java.math.BigDecimal;
import java.util.UUID;

public record EstoqueResponseDTO(
        UUID id,
        UUID idProduto,
        String nomeProduto,
        UUID idLoja,
        String nomeLoja,
        UUID idFornecedor,
        String nomeFornecedor,
        String tipoTransacao,
        String nomeTransacao,
        BigDecimal quantidadeAnterior,
        Long quantidadeRecebida,
        BigDecimal quantidadeAtual
) {
    public static EstoqueResponseDTO fromEntity(EstoqueProduto e) {
        return new EstoqueResponseDTO(
                e.getId(),
                e.getProduto().getId(),
                e.getProduto().getNome(),
                e.getLoja().getId(),
                e.getLoja().getNomeFantasia(),
                e.getFornecedor().getId(),
                e.getFornecedor().getNomeFantasia(),
                e.getTransacao().getTipo().name(),
                e.getTransacao().getNome(),
                e.getQuantidadeAnterior(),
                e.getQuantidadeRecebida(),
                e.getQuantidadeAtual()
        );
    }
}
