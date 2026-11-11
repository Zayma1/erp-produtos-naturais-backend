package com.vilaverde.erp.dto.produto;

import com.vilaverde.erp.entity.ProdutoPrecoLoja;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

public record ProdutoPrecoLojaResponseDTO(
  UUID id,
  UUID idProduto,
  UUID idProdutoSku,
  UUID idLoja,
  BigDecimal preco,
  OffsetDateTime dataInicio,
  OffsetDateTime dataFim,
  OffsetDateTime criadoEm
) {
  public static ProdutoPrecoLojaResponseDTO fromEntity(ProdutoPrecoLoja e) {
    return new ProdutoPrecoLojaResponseDTO(
      e.getId(),
      e.getProdutoSku().getProduto().getId(),
      e.getProdutoSku().getId(),
      e.getLoja().getId(),
      e.getPreco(),
      e.getDataInicio(),
      e.getDataFim(),
      e.getCriadoEm()
    );
  }
}
