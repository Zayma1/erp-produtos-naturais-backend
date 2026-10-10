package com.vilaverde.erp.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Entity
@Table(name = "loja")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Loja {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @NotBlank(message = "Nome fantasia é obrigatório")
    @Column(name = "nomeFantasia", nullable = false, length = 150)
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

    @NotBlank(message = "Número é obrigatório")
    @Column(name = "numero", nullable = false)
    private String numero;

    @Column(name = "complemento")
    private String complemento;

    @NotBlank(message = "CNPJ é obrigatório")
    @Column(name = "cnpj", nullable = false, unique = true)
    private String cnpj;
}
