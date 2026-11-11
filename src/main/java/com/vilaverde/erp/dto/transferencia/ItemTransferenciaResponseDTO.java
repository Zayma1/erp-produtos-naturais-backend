package com.vilaverde.erp.dto.transferencia;
import com.vilaverde.erp.entity.ItemTransferencia;
import java.util.UUID;
import java.math.BigDecimal;
public record ItemTransferenciaResponseDTO(UUID id,UUID idProduto,
  UUID idProdutoSku,
  String skuCodigoSnapshot,
  String skuApresentacaoSnapshot,String nomeProduto,BigDecimal quantidade,String unidadeSiglaSnapshot) {
 public static ItemTransferenciaResponseDTO fromEntity(ItemTransferencia i) {return new ItemTransferenciaResponseDTO(i.getId(),i.getProdutoSku().getProduto().getId(),
      i.getProdutoSku().getId(),
      i.getSkuCodigoSnapshot(),
      i.getSkuApresentacaoSnapshot(),i.getProdutoSku().getProduto().getNome(),i.getQuantidade(),i.getUnidadeSiglaSnapshot());}
}
