package com.vilaverde.erp.dto.produtosku;

import com.vilaverde.erp.entity.enums.StatusProduto;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.UUID;

public record ProdutoSkuRequestDTO(
  @NotNull(message = "Produto é obrigatório") UUID idProduto,
  StatusProduto status,
  @NotBlank(message = "SKU é obrigatório") String sku,
  @jakarta.validation.constraints.Pattern(regexp="(?:|[0-9]{13})") String ean13,
  @jakarta.validation.constraints.Pattern(regexp="(?:|[0-9]{14})") String dun14,
  @NotNull UUID idUnidadeMedida,
  @NotNull com.vilaverde.erp.entity.enums.TipoVenda tipoVenda,
  @NotBlank String descricaoApresentacao
) {}
