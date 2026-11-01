package com.vilaverde.erp.dto.recebimento;

import com.vilaverde.erp.entity.enums.StatusDocumento;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

public record RecebimentoCompraRequestDTO(
  @NotNull UUID idCompra,
  UUID idUsuario,
  OffsetDateTime dataRecebimento,
  StatusDocumento status,
  String observacao,
  @NotEmpty @Valid List<ItemRecebimentoRequestDTO> itens
) {}
