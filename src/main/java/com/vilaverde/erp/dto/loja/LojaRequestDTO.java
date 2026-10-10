package com.vilaverde.erp.dto.loja;

import jakarta.validation.constraints.NotBlank;

public record LojaRequestDTO(
        @NotBlank(message = "Nome fantasia é obrigatório") String nomeFantasia,
        @NotBlank(message = "Razão social é obrigatória") String razaoSocial,
        @NotBlank(message = "Estado é obrigatório") String estado,
        @NotBlank(message = "Cidade é obrigatória") String cidade,
        @NotBlank(message = "Bairro é obrigatório") String bairro,
        @NotBlank(message = "Número é obrigatório") String numero,
        String complemento,
        @NotBlank(message = "CNPJ é obrigatório") String cnpj
) {
}
