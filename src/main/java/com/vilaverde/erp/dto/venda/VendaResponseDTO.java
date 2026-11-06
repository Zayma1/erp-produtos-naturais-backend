package com.vilaverde.erp.dto.venda;

import com.vilaverde.erp.entity.Venda;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

public record VendaResponseDTO(
  UUID id,
  Integer versao,
  String observacao,
  OffsetDateTime criadoEm,
  OffsetDateTime concluidoEm,
  OffsetDateTime canceladoEm,
  String motivoCancelamento,
  UUID idModificador,
  String nomeModificador,
  UUID idConfirmador,
  String nomeConfirmador,
  UUID idCancelador,
  String nomeCancelador,
  boolean legado,
  OffsetDateTime dataVenda,
  com.vilaverde.erp.entity.enums.StatusVenda status,
  BigDecimal subtotal,
  BigDecimal descontoTotal,
  BigDecimal valorTotal,
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
      venda.getVersao(), venda.getObservacao(), venda.getCriadoEm(),
      venda.getConcluidoEm(), venda.getCanceladoEm(), venda.getMotivoCancelamento(),
      venda.getModificador()==null ? null : venda.getModificador().getId(),
      venda.getModificador()==null ? null : venda.getModificador().getNome(),
      venda.getConfirmador()==null ? null : venda.getConfirmador().getId(),
      venda.getConfirmador()==null ? null : venda.getConfirmador().getNome(),
      venda.getCancelador()==null ? null : venda.getCancelador().getId(),
      venda.getCancelador()==null ? null : venda.getCancelador().getNome(), venda.isLegado(),
      venda.getDataVenda(),
      venda.getStatus(),
      venda.getSubtotal(),
      venda.getDescontoTotal(),
      venda.getValorTotal(),
      venda.getCliente() != null ? venda.getCliente().getId() : null,
      venda.getCliente() != null ? venda.getCliente().getNome() : null,
      venda.getUsuario() != null ? venda.getUsuario().getId() : null,
      venda.getUsuario() != null ? venda.getUsuario().getNome() : null,
      venda.getLoja().getId(),
      venda.getLoja().getNomeFantasia(),
      venda
        .getItens()
        .stream()
        .map(ProdutoVendaResponseDTO::fromEntity)
        .toList()
    );
  }
}
