package com.vilaverde.erp.dto.recebimento;
import com.vilaverde.erp.entity.ItemRecebimentoCompra;
import java.math.BigDecimal;
import java.util.UUID;
public record ItemRecebimentoResponseDTO(UUID id, UUID idItemCompra, UUID idProduto,
  UUID idProdutoSku,
  String skuCodigoSnapshot,
  String skuApresentacaoSnapshot,
  String unidadeSiglaSnapshot, BigDecimal quantidade, String observacao) {
  public static ItemRecebimentoResponseDTO fromEntity(ItemRecebimentoCompra i) {
    return new ItemRecebimentoResponseDTO(i.getId(),i.getItemCompra().getId(),i.getProdutoSku().getProduto().getId(),
      i.getProdutoSku().getId(),
      i.getSkuCodigoSnapshot(),
      i.getSkuApresentacaoSnapshot(),
      i.getUnidadeSiglaSnapshot(),i.getQuantidade(),i.getObservacao());
  }
}
