package com.vilaverde.erp.dto.transferencia;
import com.vilaverde.erp.entity.TransferenciaEstoque;
import com.vilaverde.erp.entity.enums.StatusDocumento;
import java.util.*;
import java.time.OffsetDateTime;
public record TransferenciaResponseDTO(
 UUID id,
 Integer versao,
 boolean legado,
 StatusDocumento status,
 UUID idLojaOrigem,
 String nomeLojaOrigem,
 UUID idLojaDestino,
 String nomeLojaDestino,
 UUID idUsuario,
 String nomeUsuario,
 UUID idModificador,
 String nomeModificador,
 UUID idConfirmador,
 String nomeConfirmador,
 UUID idCancelador,
 String nomeCancelador,
 OffsetDateTime dataTransferencia,
 OffsetDateTime criadoEm,
 OffsetDateTime atualizadoEm,
 OffsetDateTime concluidoEm,
 OffsetDateTime canceladoEm,
 String observacao,
 String motivoCancelamento,
 List<ItemTransferenciaResponseDTO> itens) {
 public static TransferenciaResponseDTO fromEntity(TransferenciaEstoque e) { return fromEntity(e,true); }
 public static TransferenciaResponseDTO summary(TransferenciaEstoque e) { return fromEntity(e,false); }
 private static TransferenciaResponseDTO fromEntity(TransferenciaEstoque e,boolean detail) { return new TransferenciaResponseDTO(
e.getId(),
e.getVersao(),
e.isLegado(),
e.getStatus(),
e.getLojaOrigem()==null ? null : e.getLojaOrigem().getId(),
e.getLojaOrigem()==null ? null : e.getLojaOrigem().getNomeFantasia(),
e.getLojaDestino()==null ? null : e.getLojaDestino().getId(),
e.getLojaDestino()==null ? null : e.getLojaDestino().getNomeFantasia(),
e.getUsuario()==null ? null : e.getUsuario().getId(),
e.getUsuario()==null ? null : e.getUsuario().getNome(),
e.getModificador()==null ? null : e.getModificador().getId(),
e.getModificador()==null ? null : e.getModificador().getNome(),
e.getConfirmador()==null ? null : e.getConfirmador().getId(),
e.getConfirmador()==null ? null : e.getConfirmador().getNome(),
e.getCancelador()==null ? null : e.getCancelador().getId(),
e.getCancelador()==null ? null : e.getCancelador().getNome(),
e.getDataTransferencia(),
e.getCriadoEm(),
e.getAtualizadoEm(),
e.getConcluidoEm(),
e.getCanceladoEm(),
e.getObservacao(),
e.getMotivoCancelamento(),
detail ? e.getItens().stream().map(ItemTransferenciaResponseDTO::fromEntity).toList() : null); }
}
