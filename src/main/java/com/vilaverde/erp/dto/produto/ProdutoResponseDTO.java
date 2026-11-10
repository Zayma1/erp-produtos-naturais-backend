package com.vilaverde.erp.dto.produto;

import com.vilaverde.erp.entity.Produto;
import com.vilaverde.erp.entity.enums.StatusProduto;
import com.vilaverde.erp.entity.enums.TipoVenda;
import java.util.UUID;

public record ProdutoResponseDTO(
  UUID id,
  String nome,
  UUID idCategoria,
  String nomeCategoria,
  String descricao,
  StatusProduto status
) {
  public static ProdutoResponseDTO fromEntity(Produto p) {
    return new ProdutoResponseDTO(
      p.getId(),
      p.getNome(),
      p.getCategoria() != null ? p.getCategoria().getId() : null,
      p.getCategoria() != null ? p.getCategoria().getNome() : null,
      p.getDescricao(),
      p.getStatus()
    );
  }
}
