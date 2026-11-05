package com.vilaverde.erp.dto.recebimento;
import com.vilaverde.erp.entity.ItemRecebimentoCompra;
import java.math.BigDecimal;
import java.util.UUID;
public record ItemRecebimentoResponseDTO(UUID id, UUID idItemCompra, UUID idProduto, BigDecimal quantidade, String observacao) {
  public static ItemRecebimentoResponseDTO fromEntity(ItemRecebimentoCompra i) {
    return new ItemRecebimentoResponseDTO(i.getId(),i.getItemCompra().getId(),i.getProduto().getId(),i.getQuantidade(),i.getObservacao());
  }
}
