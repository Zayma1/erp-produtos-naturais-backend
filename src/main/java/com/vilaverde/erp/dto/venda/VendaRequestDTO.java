package com.vilaverde.erp.dto.venda;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

public record VendaRequestDTO(
  Integer versao,
  String status,
  OffsetDateTime dataVenda,
  @NotNull UUID idCliente,
  UUID idUsuario,
  @NotNull(message = "Loja é obrigatória") UUID idLoja,
  @jakarta.validation.constraints.Size(max=255) String observacao,
  @NotEmpty(message = "A venda precisa de ao menos um item")
  List<@NotNull @Valid ProdutoVendaRequestDTO> itens
) {}
