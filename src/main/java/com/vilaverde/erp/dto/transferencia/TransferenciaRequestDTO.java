package com.vilaverde.erp.dto.transferencia;

import com.vilaverde.erp.entity.enums.StatusDocumento;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

public record TransferenciaRequestDTO(
  @NotNull UUID idLojaOrigem,
  @NotNull UUID idLojaDestino,
  UUID idUsuario,
  OffsetDateTime dataTransferencia,
  StatusDocumento status,
  String observacao,
  Integer versao,
  @NotEmpty @Valid List<ItemTransferenciaRequestDTO> itens
) {}
