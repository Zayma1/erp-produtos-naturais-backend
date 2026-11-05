package com.vilaverde.erp.dto.recebimento;

import com.vilaverde.erp.entity.RecebimentoCompra;
import com.vilaverde.erp.entity.enums.StatusDocumento;
import java.time.OffsetDateTime;
import java.util.UUID;

public record RecebimentoCompraResponseDTO(
  UUID id,
  UUID idCompra,
  UUID idUsuario,
  StatusDocumento status,
  OffsetDateTime dataRecebimento,
  String observacao,
  Integer versao,
  OffsetDateTime concluidoEm,
  java.util.List<ItemRecebimentoResponseDTO> itens
) {
  public static RecebimentoCompraResponseDTO fromEntity(RecebimentoCompra e, java.util.List<ItemRecebimentoResponseDTO> itens) {
    return new RecebimentoCompraResponseDTO(
      e.getId(),
      e.getCompra().getId(),
      e.getUsuario() == null ? null : e.getUsuario().getId(),
      e.getStatus(),
      e.getDataRecebimento(),
      e.getObservacao(), e.getVersao(), e.getConcluidoEm(), itens
    );
  }
}
