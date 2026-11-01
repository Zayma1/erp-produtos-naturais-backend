package com.vilaverde.erp.dto.compra;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.UUID;

public record ItemCompraRequestDTO(
  UUID id,
  @NotNull UUID idProduto,
  @NotNull @DecimalMin("0.001") BigDecimal quantidade,
  @NotNull @DecimalMin("0.00") BigDecimal custoUnitario,
  @DecimalMin("0.00") BigDecimal desconto
) {}
