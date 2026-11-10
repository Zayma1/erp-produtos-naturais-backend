package com.vilaverde.erp.dto.estoque;

import com.vilaverde.erp.entity.EstoqueProduto;
import java.math.BigDecimal;
import java.util.UUID;

public record EstoqueResponseDTO(
  UUID id,
  UUID idProduto,
  UUID idProdutoSku,
  String sku,
  String descricaoApresentacao,
  String siglaUnidadeMedida,
  String nomeProduto,
  UUID idLoja,
  String nomeLoja,
  BigDecimal quantidadeAtual,
  java.time.OffsetDateTime atualizadoEm
) {
  public static EstoqueResponseDTO fromEntity(EstoqueProduto e) {
    return new EstoqueResponseDTO(
      e.getId(),
      e.getProdutoSku().getProduto().getId(),
      e.getProdutoSku().getId(),
      e.getProdutoSku().getSku(),
      e.getProdutoSku().getDescricaoApresentacao(),
      e.getProdutoSku().getUnidadeMedida().getSigla(),
      e.getProdutoSku().getProduto().getNome(),
      e.getLoja().getId(),
      e.getLoja().getNomeFantasia(),
      e.getQuantidadeAtual(),
      e.getAtualizadoEm()
    );
  }
}
