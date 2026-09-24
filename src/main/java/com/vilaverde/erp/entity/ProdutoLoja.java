package com.vilaverde.erp.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Entity
@Table(name = "produto_loja", uniqueConstraints = @UniqueConstraint(columnNames = {"id_produto", "id_loja"}))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ProdutoLoja {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_produto", nullable = false)
    private Produto produto;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_loja", nullable = false)
    private Loja loja;
}
