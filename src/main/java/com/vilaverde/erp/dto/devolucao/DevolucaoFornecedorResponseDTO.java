package com.vilaverde.erp.dto.devolucao;

import com.vilaverde.erp.entity.DevolucaoFornecedor;
import com.vilaverde.erp.entity.enums.StatusDocumento;
import java.time.OffsetDateTime;
import java.util.UUID;

public record DevolucaoFornecedorResponseDTO(
  UUID id,
  UUID idCompra,
  UUID idUsuario,
  StatusDocumento status,
  OffsetDateTime dataDevolucao,
  String motivo,
  String observacao
) {
  public static DevolucaoFornecedorResponseDTO fromEntity(
    DevolucaoFornecedor e
  ) {
    return new DevolucaoFornecedorResponseDTO(
      e.getId(),
      e.getCompra().getId(),
      e.getUsuario() == null ? null : e.getUsuario().getId(),
      e.getStatus(),
      e.getDataDevolucao(),
      e.getMotivo(),
      e.getObservacao()
    );
  }
}
