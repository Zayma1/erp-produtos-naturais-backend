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
  BigDecimal quantidadeAtual,
  java.time.OffsetDateTime atualizadoEm
) {
  public static EstoqueResponseDTO fromEntity(EstoqueProduto e) {
    return new EstoqueResponseDTO(
      e.getId(),
      e.getProduto().getId(),
      e.getProduto().getNome(),
      e.getLoja().getId(),
      e.getLoja().getNomeFantasia(),
      e.getQuantidadeAtual(),
      e.getAtualizadoEm()
    );
  }
}
