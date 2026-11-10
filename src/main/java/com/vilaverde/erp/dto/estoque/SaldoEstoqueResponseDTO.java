package com.vilaverde.erp.dto.estoque;

import java.math.BigDecimal;
import java.util.UUID;

public record SaldoEstoqueResponseDTO(
  UUID idProduto,
  UUID idProdutoSku,
  UUID idLoja,
  BigDecimal quantidadeAtual
) {}
