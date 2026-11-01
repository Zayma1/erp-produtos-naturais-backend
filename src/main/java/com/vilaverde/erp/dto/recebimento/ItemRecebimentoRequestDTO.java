package com.vilaverde.erp.dto.recebimento;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.UUID;

public record ItemRecebimentoRequestDTO(
  @NotNull UUID idItemCompra,
  @NotNull UUID idProduto,
  @NotNull @DecimalMin("0.001") BigDecimal quantidade,
  String observacao
) {}
