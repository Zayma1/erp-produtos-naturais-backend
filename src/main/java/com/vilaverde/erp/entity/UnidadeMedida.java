package com.vilaverde.erp.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Entity
@Table(name = "unidade_medida")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UnidadeMedida {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @NotBlank(message = "Sigla é obrigatória")
    @Column(nullable = false, length = 10)
    private String sigla;

    @NotBlank(message = "Nome é obrigatório")
    @Column(nullable = false)
    private String nome;
}
