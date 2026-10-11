package com.vilaverde.erp.dto.venda;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public record VendaRequestDTO(
        @NotNull(message = "Data da venda é obrigatória") LocalDate dataVenda,
        @NotNull(message = "Cliente é obrigatório") UUID idCliente,
        @NotNull(message = "Usuário é obrigatório") UUID idUsuario,
        @NotNull(message = "Loja é obrigatória") UUID idLoja,
        @NotEmpty(message = "A venda precisa de ao menos um item")
        @Valid List<ProdutoVendaRequestDTO> itens
) {
}
