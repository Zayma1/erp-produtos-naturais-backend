package com.vilaverde.erp.entity;

import com.vilaverde.erp.entity.enums.StatusProduto;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Entity
@Table(name = "produto")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Produto {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @NotBlank(message = "Nome do produto é obrigatório")
    @Column(name = "nome", nullable = false)
    private String nome;

    @NotNull(message = "Tipo/categoria é obrigatório")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "idTipo", nullable = false)
    private Categoria tipo;

    @NotNull(message = "Unidade de medida é obrigatória")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "idUnidadeMedida", nullable = false)
    private UnidadeMedida unidadeMedida;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private StatusProduto status = StatusProduto.ATIVO;
}
