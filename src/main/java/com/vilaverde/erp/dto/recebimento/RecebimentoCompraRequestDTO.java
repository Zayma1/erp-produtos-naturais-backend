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
  @NotNull UUID idUsuario,
  @jakarta.validation.constraints.PositiveOrZero Integer versao,
  OffsetDateTime dataRecebimento,
  StatusDocumento status,
  @jakarta.validation.constraints.Size(max=255) String observacao,
  @NotEmpty List<@NotNull @Valid ItemRecebimentoRequestDTO> itens
) {}
