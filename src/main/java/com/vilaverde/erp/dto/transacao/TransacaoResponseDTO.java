package com.vilaverde.erp.dto.transacao;

import com.vilaverde.erp.entity.Transacao;
import com.vilaverde.erp.entity.enums.TipoTransacao;

import java.util.UUID;

public record TransacaoResponseDTO(
        UUID id,
        String sigla,
        String nome,
        TipoTransacao tipo
) {
    public static TransacaoResponseDTO fromEntity(Transacao transacao) {
        return new TransacaoResponseDTO(
                transacao.getId(),
                transacao.getSigla(),
                transacao.getNome(),
                transacao.getTipo()
        );
    }
}
