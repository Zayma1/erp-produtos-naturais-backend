package com.vilaverde.erp.dto.transferencia;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.UUID;

public record ItemTransferenciaRequestDTO(
  @NotNull UUID idProduto,
  @NotNull @DecimalMin("0.001") BigDecimal quantidade
) {}
