package com.vilaverde.erp.dto.compra;

import com.vilaverde.erp.entity.ItemCompra;
import java.math.BigDecimal;
import java.util.UUID;

public record ItemCompraResponseDTO(
  UUID id,
  UUID idProduto,
  BigDecimal quantidade,
  BigDecimal custoUnitario,
  BigDecimal desconto,
  BigDecimal totalItem,
  BigDecimal quantidadeRecebida
) {
  public static ItemCompraResponseDTO fromEntity(ItemCompra e) {
    return new ItemCompraResponseDTO(
      e.getId(),
      e.getProduto().getId(),
      e.getQuantidade(),
      e.getCustoUnitario(),
      e.getDesconto(),
      e.getTotalItem(),
      e.getQuantidadeRecebida()
    );
  }
}
