package com.vilaverde.erp.dto.devolucao;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.UUID;

public record ItemDevolucaoFornecedorRequestDTO(
  @NotNull UUID idItemCompra,
  @NotNull UUID idProduto,
  @NotNull @DecimalMin("0.001") BigDecimal quantidade,
  String observacao
) {}
