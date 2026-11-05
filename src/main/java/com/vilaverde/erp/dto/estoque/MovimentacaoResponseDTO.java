package com.vilaverde.erp.dto.estoque;

import com.vilaverde.erp.entity.MovimentacaoEstoque;
import com.vilaverde.erp.entity.enums.TipoMovimentacao;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

public record MovimentacaoResponseDTO(
  UUID id,
  UUID idProduto,
  UUID idLoja,
  TipoMovimentacao tipo,
  BigDecimal quantidade,
  OffsetDateTime dataMovimentacao,
  String observacao,
  UUID idUsuario,
  UUID idVenda,
  UUID idCompra,
  UUID idTransferencia,
  UUID idAjusteEstoque,
  UUID idRecebimentoCompra
) {
  public static MovimentacaoResponseDTO fromEntity(MovimentacaoEstoque e) {
    return new MovimentacaoResponseDTO(
      e.getId(),
      e.getProduto().getId(),
      e.getLoja().getId(),
      e.getTipo(),
      e.getQuantidade(),
      e.getDataMovimentacao(),
      e.getObservacao(),
      e.getUsuario() == null ? null : e.getUsuario().getId(),
      e.getVenda() == null ? null : e.getVenda().getId(),
      e.getCompra() == null ? null : e.getCompra().getId(),
      e.getTransferencia() == null ? null : e.getTransferencia().getId(),
      e.getAjusteEstoque() == null ? null : e.getAjusteEstoque().getId(),
      e.getItemRecebimentoCompra() == null ? null : e.getItemRecebimentoCompra().getRecebimentoCompra().getId()
    );
  }
}
