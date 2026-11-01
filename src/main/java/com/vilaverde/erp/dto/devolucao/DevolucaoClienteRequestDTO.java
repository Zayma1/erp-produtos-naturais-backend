package com.vilaverde.erp.dto.devolucao;

import com.vilaverde.erp.entity.enums.StatusDocumento;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

public record DevolucaoClienteRequestDTO(
  @NotNull UUID idVenda,
  UUID idUsuario,
  OffsetDateTime dataDevolucao,
  StatusDocumento status,
  String motivo,
  String observacao,
  @NotEmpty @Valid List<ItemDevolucaoClienteRequestDTO> itens
) {}
