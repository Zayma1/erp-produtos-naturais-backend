package com.vilaverde.erp.dto.venda;

import com.vilaverde.erp.entity.Venda;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public record VendaResponseDTO(
        UUID id,
        BigDecimal precoVenda,
        LocalDate dataVenda,
        UUID idCliente,
        String nomeCliente,
        UUID idUsuario,
        String nomeUsuario,
        UUID idLoja,
        String nomeLoja,
        List<ProdutoVendaResponseDTO> itens
) {
    public static VendaResponseDTO fromEntity(Venda venda) {
        return new VendaResponseDTO(
                venda.getId(),
                venda.getPrecoVenda(),
                venda.getDataVenda(),
                venda.getCliente().getId(),
                venda.getCliente().getNome(),
                venda.getUsuario().getId(),
                venda.getUsuario().getNome(),
                venda.getLoja().getId(),
                venda.getLoja().getNomeFantasia(),
                venda.getItens().stream().map(ProdutoVendaResponseDTO::fromEntity).toList()
        );
    }
}
