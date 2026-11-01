package com.vilaverde.erp.dto.produto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

public record ProdutoPrecoLojaRequestDTO(
  @NotNull UUID idProduto,
  @NotNull UUID idLoja,
  @NotNull @DecimalMin("0.00") BigDecimal preco,
  OffsetDateTime dataInicio,
  OffsetDateTime dataFim
) {}
