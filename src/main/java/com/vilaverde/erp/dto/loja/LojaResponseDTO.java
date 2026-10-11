package com.vilaverde.erp.dto.loja;

import com.vilaverde.erp.entity.Loja;

import java.util.UUID;

public record LojaResponseDTO(
        UUID id,
        String nomeFantasia,
        String razaoSocial,
        String estado,
        String cidade,
        String bairro,
        String numero,
        String complemento,
        String cnpj
) {
    public static LojaResponseDTO fromEntity(Loja loja) {
        return new LojaResponseDTO(
                loja.getId(),
                loja.getNomeFantasia(),
                loja.getRazaoSocial(),
                loja.getEstado(),
                loja.getCidade(),
                loja.getBairro(),
                loja.getNumero(),
                loja.getComplemento(),
                loja.getCnpj()
        );
    }
}
