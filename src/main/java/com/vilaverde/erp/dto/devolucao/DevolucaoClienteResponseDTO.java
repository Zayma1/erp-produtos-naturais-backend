package com.vilaverde.erp.dto.devolucao;

import com.vilaverde.erp.entity.DevolucaoCliente;
import com.vilaverde.erp.entity.enums.StatusDocumento;
import java.time.OffsetDateTime;
import java.util.UUID;

public record DevolucaoClienteResponseDTO(
  UUID id,
  UUID idVenda,
  UUID idUsuario,
  StatusDocumento status,
  OffsetDateTime dataDevolucao,
  String motivo,
  String observacao
) {
  public static DevolucaoClienteResponseDTO fromEntity(DevolucaoCliente e) {
    return new DevolucaoClienteResponseDTO(
      e.getId(),
      e.getVenda().getId(),
      e.getUsuario() == null ? null : e.getUsuario().getId(),
      e.getStatus(),
      e.getDataDevolucao(),
      e.getMotivo(),
      e.getObservacao()
    );
  }
}
