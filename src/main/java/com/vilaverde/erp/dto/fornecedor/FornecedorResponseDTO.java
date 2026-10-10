package com.vilaverde.erp.dto.fornecedor;

import com.vilaverde.erp.entity.Fornecedor;

import java.util.UUID;

public record FornecedorResponseDTO(
        UUID id,
        String nomeFantasia,
        String razaoSocial,
        String estado,
        String cidade,
        String bairro,
        String complemento,
        String cnpj,
        String cpf
) {
    public static FornecedorResponseDTO fromEntity(Fornecedor fornecedor) {
        return new FornecedorResponseDTO(
                fornecedor.getId(),
                fornecedor.getNomeFantasia(),
                fornecedor.getRazaoSocial(),
                fornecedor.getEstado(),
                fornecedor.getCidade(),
                fornecedor.getBairro(),
                fornecedor.getComplemento(),
                fornecedor.getCnpj(),
                fornecedor.getCpf()
        );
    }
}
