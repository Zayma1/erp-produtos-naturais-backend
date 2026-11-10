package com.vilaverde.erp.dto.produtoloja;

import com.vilaverde.erp.entity.ProdutoLoja;
import java.util.UUID;

public record ProdutoLojaResponseDTO(
  UUID id,
  UUID idProduto,
  UUID idProdutoSku,
  String nomeProduto,
  UUID idLoja,
  String nomeLoja
) {
  public static ProdutoLojaResponseDTO fromEntity(ProdutoLoja vinculo) {
    return new ProdutoLojaResponseDTO(
      vinculo.getId(),
      vinculo.getProdutoSku().getProduto().getId(),
      vinculo.getProdutoSku().getId(),
      vinculo.getProdutoSku().getProduto().getNome(),
      vinculo.getLoja().getId(),
      vinculo.getLoja().getNomeFantasia()
    );
  }
}
