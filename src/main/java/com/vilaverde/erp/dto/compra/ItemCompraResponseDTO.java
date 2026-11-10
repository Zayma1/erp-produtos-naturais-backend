package com.vilaverde.erp.dto.compra;

import com.vilaverde.erp.entity.ItemCompra;
import java.math.BigDecimal;
import java.util.UUID;

public record ItemCompraResponseDTO(
  BigDecimal quantidadeDevolvida,
  BigDecimal quantidadeDevolvivel,
  boolean devolucaoLegadaPendente,
  UUID id,
  UUID idProduto,
  UUID idProdutoSku,
  String skuCodigoSnapshot,
  String skuApresentacaoSnapshot,
  String unidadeSiglaSnapshot,
  BigDecimal quantidade,
  BigDecimal custoUnitario,
  BigDecimal desconto,
  BigDecimal totalItem,
  BigDecimal quantidadeRecebida
) {
  public static ItemCompraResponseDTO fromEntity(ItemCompra e) {
    return new ItemCompraResponseDTO(
      e.getQuantidadeDevolvida()==null ? BigDecimal.ZERO : e.getQuantidadeDevolvida(),
      e.isDevolucaoLegadaPendente() ? null : e.getQuantidadeRecebida().subtract(e.getQuantidadeDevolvida()==null ? BigDecimal.ZERO : e.getQuantidadeDevolvida()),
      e.isDevolucaoLegadaPendente(),
      e.getId(),
      e.getProdutoSku().getProduto().getId(),
      e.getProdutoSku().getId(),
      e.getSkuCodigoSnapshot(),
      e.getSkuApresentacaoSnapshot(),
      e.getUnidadeSiglaSnapshot(),
      e.getQuantidade(),
      e.getCustoUnitario(),
      e.getDesconto(),
      e.getTotalItem(),
      e.getQuantidadeRecebida()
    );
  }
}
