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
    @Column(nullable = false)
    private String nomeFantasia;

    private String razaoSocial;
    private String estado;
    private String cidade;
    private String bairro;
    private String complemento;
    private String cnpj;
    private String cpf;
}
