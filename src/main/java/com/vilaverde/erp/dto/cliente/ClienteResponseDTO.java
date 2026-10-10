package com.vilaverde.erp.dto.cliente;

import com.vilaverde.erp.entity.Cliente;

import java.time.LocalDate;
import java.util.UUID;

public record ClienteResponseDTO(
        UUID id,
        String nome,
        String cpf,
        LocalDate dataNascimento
) {
    public static ClienteResponseDTO fromEntity(Cliente cliente) {
        return new ClienteResponseDTO(
                cliente.getId(),
                cliente.getNome(),
                cliente.getCpf(),
                cliente.getDataNascimento()
        );
    }
}
