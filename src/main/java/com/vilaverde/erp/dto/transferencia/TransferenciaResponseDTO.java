package com.vilaverde.erp.dto.transferencia;

import com.vilaverde.erp.entity.TransferenciaEstoque;
import com.vilaverde.erp.entity.enums.StatusDocumento;
import java.time.OffsetDateTime;
import java.util.UUID;

public record TransferenciaResponseDTO(
  UUID id,
  UUID idLojaOrigem,
  UUID idLojaDestino,
  UUID idUsuario,
  StatusDocumento status,
  OffsetDateTime dataTransferencia,
  String observacao
) {
  public static TransferenciaResponseDTO fromEntity(TransferenciaEstoque e) {
    return new TransferenciaResponseDTO(
      e.getId(),
      e.getLojaOrigem().getId(),
      e.getLojaDestino().getId(),
      e.getUsuario() == null ? null : e.getUsuario().getId(),
      e.getStatus(),
      e.getDataTransferencia(),
      e.getObservacao()
    );
  }
}
