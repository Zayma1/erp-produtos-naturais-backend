package com.vilaverde.erp.dto.estoque;

import com.vilaverde.erp.entity.EstoqueProduto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
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
        BigDecimal quantidadeRecebida,
        BigDecimal quantidadeAtual,
        String motivo,
        LocalDateTime dataMovimentacao
) {
    public static EstoqueResponseDTO fromEntity(EstoqueProduto e) {
        return new EstoqueResponseDTO(
                e.getId(),
                e.getProduto().getId(),
                e.getProduto().getNome(),
                e.getLoja().getId(),
                e.getLoja().getNomeFantasia(),
                e.getFornecedor() != null ? e.getFornecedor().getId() : null,
                e.getFornecedor() != null ? e.getFornecedor().getNomeFantasia() : null,
                e.getTransacao().getTipo().name(),
                e.getTransacao().getNome(),
                e.getQuantidadeAnterior(),
                e.getQuantidadeRecebida(),
                e.getQuantidadeAtual(),
                e.getMotivo(),
                e.getDataMovimentacao()
        );
    }
}
