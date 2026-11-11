package com.vilaverde.erp.dto.recebimento;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.util.UUID;
public record ItemRecebimentoRequestDTO(
  UUID id, @NotNull UUID idItemCompra, @NotNull UUID idProdutoSku,
  @NotNull @DecimalMin("0.001") @Digits(integer=11, fraction=3) BigDecimal quantidade,
  @Size(max=255) String observacao
) {}
