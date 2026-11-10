package com.vilaverde.erp.dto.transferencia;
import com.vilaverde.erp.entity.ItemTransferencia;
import java.util.UUID;
import java.math.BigDecimal;
public record ItemTransferenciaResponseDTO(UUID id,UUID idProduto,String nomeProduto,BigDecimal quantidade,String unidadeSiglaSnapshot) {
 public static ItemTransferenciaResponseDTO fromEntity(ItemTransferencia i) {return new ItemTransferenciaResponseDTO(i.getId(),i.getProduto().getId(),i.getProduto().getNome(),i.getQuantidade(),i.getUnidadeSiglaSnapshot());}
}
