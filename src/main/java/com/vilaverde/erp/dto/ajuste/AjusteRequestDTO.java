package com.vilaverde.erp.dto.ajuste;

import com.vilaverde.erp.entity.enums.StatusDocumento;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import java.util.UUID;

public record AjusteRequestDTO(
  @NotNull UUID idLoja,
  UUID idUsuario,
  @NotNull String justificativa,
  StatusDocumento status,
  Integer versao,
  @NotEmpty @Valid List<ItemAjusteRequestDTO> itens
) {}
