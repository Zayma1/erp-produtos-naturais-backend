package com.vilaverde.erp.dto.fornecedor;

import jakarta.validation.constraints.NotBlank;

public record FornecedorRequestDTO(
        @NotBlank(message = "Nome fantasia é obrigatório") String nomeFantasia,
        @NotBlank(message = "Razão social é obrigatória") String razaoSocial,
        @NotBlank(message = "Estado é obrigatório") String estado,
        @NotBlank(message = "Cidade é obrigatória") String cidade,
        @NotBlank(message = "Bairro é obrigatório") String bairro,
        String complemento,
        String cnpj,
        String cpf
) {
}
