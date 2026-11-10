package com.vilaverde.erp.dto.ajuste;
import com.vilaverde.erp.entity.*;
import com.vilaverde.erp.entity.enums.*;
import java.time.OffsetDateTime;
import java.util.*;
import java.math.BigDecimal;
public record AjusteResponseDTO(UUID id, UUID idLoja, String lojaNome, UUID idUsuario, String usuarioNome, String justificativa, StatusDocumento status, Integer versao, boolean legado, OffsetDateTime criadoEm, OffsetDateTime atualizadoEm, OffsetDateTime concluidoEm, OffsetDateTime canceladoEm, UUID idModificador, UUID idConfirmador, UUID idCancelador, String motivoCancelamento, List<Item> itens) {
 public record Item(UUID id, UUID idProduto,
  UUID idProdutoSku, String skuCodigoSnapshot, String skuApresentacaoSnapshot, String produtoNome, String unidadeSigla, TipoMovimentacao tipo, BigDecimal quantidade, String observacao, BigDecimal saldoAntes, BigDecimal saldoDepois) {}
 private static UUID actor(Usuario u) {return u==null?null:u.getId();}
 public static AjusteResponseDTO fromEntity(AjusteEstoque a) {
 return new AjusteResponseDTO(a.getId(),a.getLoja().getId(),a.getLoja().getNomeFantasia(),actor(a.getUsuario()),a.getUsuario()==null?null:a.getUsuario().getNome(),a.getJustificativa(),a.getStatus(),a.getVersao(),a.isLegado(),a.getCriadoEm(),a.getAtualizadoEm(),a.getConcluidoEm(),a.getCanceladoEm(),actor(a.getModificador()),actor(a.getConfirmador()),actor(a.getCancelador()),a.getMotivoCancelamento(),a.getItens().stream().map(i->new Item(i.getId(),i.getProdutoSku().getProduto().getId(),
      i.getProdutoSku().getId(),i.getSkuCodigoSnapshot(),i.getSkuApresentacaoSnapshot(),i.getProdutoSku().getProduto().getNome(),i.getUnidadeSiglaSnapshot(),i.getTipo(),i.getQuantidade(),i.getObservacao(),i.getSaldoAntes(),i.getSaldoDepois())).toList());
 }
}
