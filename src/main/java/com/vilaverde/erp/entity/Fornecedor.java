package com.vilaverde.erp.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Entity
@Table(name = "fornecedor")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Fornecedor {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @NotBlank(message = "Nome fantasia é obrigatório")
    @Column(name = "nomeFantasia", nullable = false)
    private String nomeFantasia;

    @NotBlank(message = "Razão social é obrigatória")
    @Column(name = "razaoSocial", nullable = false)
    private String razaoSocial;

    @NotBlank(message = "Estado é obrigatório")
    @Column(name = "estado", nullable = false)
    private String estado;

    @NotBlank(message = "Cidade é obrigatória")
    @Column(name = "cidade", nullable = false)
    private String cidade;

    @NotBlank(message = "Bairro é obrigatório")
    @Column(name = "bairro", nullable = false)
    private String bairro;

    @Column(name = "complemento")
    private String complemento;

    @Column(name = "cnpj")
    private String cnpj;

    @Column(name = "cpf")
    private String cpf;
}
