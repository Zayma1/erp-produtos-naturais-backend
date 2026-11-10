package com.vilaverde.erp.dto.venda;

import com.vilaverde.erp.entity.ProdutoVenda;
import java.math.BigDecimal;
import java.util.UUID;

public record ProdutoVendaResponseDTO(
  BigDecimal quantidadeDevolvida,
  BigDecimal quantidadeDevolvivel,
  boolean devolucaoLegadaPendente,
  UUID id,
  UUID idProduto,
  UUID idProdutoSku,
  String skuCodigoSnapshot,
  String skuApresentacaoSnapshot,
  String nomeProduto,
  BigDecimal quantidade,
  BigDecimal precoUnitario,
  BigDecimal desconto,
  BigDecimal totalItem,
  String unidadeSiglaSnapshot
) {
  public static ProdutoVendaResponseDTO fromEntity(ProdutoVenda item) {
    return new ProdutoVendaResponseDTO(
      item.getQuantidadeDevolvida()==null ? BigDecimal.ZERO : item.getQuantidadeDevolvida(),
      item.isDevolucaoLegadaPendente() ? null : item.getQuantidade().subtract(item.getQuantidadeDevolvida()==null ? BigDecimal.ZERO : item.getQuantidadeDevolvida()),
      item.isDevolucaoLegadaPendente(),
      item.getId(),
      item.getProdutoSku().getProduto().getId(),
      item.getProdutoSku().getId(),
      item.getSkuCodigoSnapshot(),
      item.getSkuApresentacaoSnapshot(),
      item.getProdutoSku().getProduto().getNome(),
      item.getQuantidade(),
      item.getPrecoUnitario(),
      item.getDesconto(),
      item.getTotalItem(),
      item.getUnidadeSiglaSnapshot()
    );
  }
}
