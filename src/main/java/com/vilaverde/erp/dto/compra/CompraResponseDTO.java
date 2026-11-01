package com.vilaverde.erp.dto.compra;

import com.vilaverde.erp.entity.Compra;
import com.vilaverde.erp.entity.enums.StatusCompra;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

public record CompraResponseDTO(
  UUID id,
  UUID idFornecedor,
  UUID idLoja,
  UUID idUsuario,
  OffsetDateTime dataCompra,
  StatusCompra status,
  BigDecimal valorTotal,
  String observacao,
  List<ItemCompraResponseDTO> itens
) {
  public static CompraResponseDTO fromEntity(
    Compra e,
    List<ItemCompraResponseDTO> itens
  ) {
    return new CompraResponseDTO(
      e.getId(),
      e.getFornecedor().getId(),
      e.getLoja().getId(),
      e.getUsuario() == null ? null : e.getUsuario().getId(),
      e.getDataCompra(),
      e.getStatus(),
      e.getValorTotal(),
      e.getObservacao(),
      itens
    );
  }
}
