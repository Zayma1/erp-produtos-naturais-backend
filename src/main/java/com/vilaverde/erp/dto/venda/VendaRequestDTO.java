package com.vilaverde.erp.dto.venda;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

public record VendaRequestDTO(
  OffsetDateTime dataVenda,
  UUID idCliente,
  UUID idUsuario,
  @NotNull(message = "Loja é obrigatória") UUID idLoja,
  String observacao,
  @NotEmpty(message = "A venda precisa de ao menos um item")
  @Valid
  List<ProdutoVendaRequestDTO> itens
) {}
