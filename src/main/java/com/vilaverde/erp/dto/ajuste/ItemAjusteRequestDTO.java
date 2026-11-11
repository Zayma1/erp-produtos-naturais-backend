package com.vilaverde.erp.dto.ajuste;

import com.vilaverde.erp.entity.enums.TipoMovimentacao;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.UUID;

public record ItemAjusteRequestDTO(
  UUID id,
  @NotNull UUID idProdutoSku,
  @NotNull TipoMovimentacao tipo,
  @NotNull @DecimalMin("0.001") BigDecimal quantidade,
  String observacao
) {}
