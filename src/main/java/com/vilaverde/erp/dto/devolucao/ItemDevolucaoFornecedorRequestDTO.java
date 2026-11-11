package com.vilaverde.erp.dto.devolucao;
import java.util.UUID;
import java.math.BigDecimal;
public record ItemDevolucaoFornecedorRequestDTO(UUID id,UUID idItemCompra,UUID idProdutoSku,BigDecimal quantidade,String observacao) {}
