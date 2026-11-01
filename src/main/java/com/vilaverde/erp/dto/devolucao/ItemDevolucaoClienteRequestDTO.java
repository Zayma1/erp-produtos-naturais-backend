package com.vilaverde.erp.dto.devolucao;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.UUID;

public record ItemDevolucaoClienteRequestDTO(
  @NotNull UUID idItemVenda,
  @NotNull UUID idProduto,
  @NotNull @DecimalMin("0.001") BigDecimal quantidade,
  String observacao
) {}
