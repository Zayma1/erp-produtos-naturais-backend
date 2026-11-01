package com.vilaverde.erp.dto.ajuste;

import com.vilaverde.erp.entity.AjusteEstoque;
import com.vilaverde.erp.entity.enums.StatusDocumento;
import java.time.OffsetDateTime;
import java.util.UUID;

public record AjusteResponseDTO(
  UUID id,
  UUID idLoja,
  UUID idUsuario,
  String justificativa,
  StatusDocumento status,
  OffsetDateTime criadoEm,
  OffsetDateTime concluidoEm
) {
  public static AjusteResponseDTO fromEntity(AjusteEstoque e) {
    return new AjusteResponseDTO(
      e.getId(),
      e.getLoja().getId(),
      e.getUsuario().getId(),
      e.getJustificativa(),
      e.getStatus(),
      e.getCriadoEm(),
      e.getConcluidoEm()
    );
  }
}
