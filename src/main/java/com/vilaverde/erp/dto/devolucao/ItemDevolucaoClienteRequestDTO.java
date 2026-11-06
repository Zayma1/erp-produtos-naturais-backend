package com.vilaverde.erp.dto.devolucao;
import java.util.UUID;
import java.math.BigDecimal;
public record ItemDevolucaoClienteRequestDTO(UUID id,UUID idItemVenda,UUID idProduto,BigDecimal quantidade,String observacao, Boolean retornaAoEstoque) {}
