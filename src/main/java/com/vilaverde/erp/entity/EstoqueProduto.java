package com.vilaverde.erp.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "estoqueProduto")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class EstoqueProduto {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "idProduto", nullable = false)
    private Produto produto;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "idLoja", nullable = false)
    private Loja loja;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "idFornecedor", nullable = false)
    private Fornecedor fornecedor;

    @Column(name = "quantidadeAtual", nullable = false, precision = 12, scale = 3)
    private BigDecimal quantidadeAtual = BigDecimal.ZERO;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "idTransacao", nullable = false)
    private Transacao transacao;

    @Column(name = "quantidadeAnterior", nullable = false, precision = 12, scale = 3)
    private BigDecimal quantidadeAnterior = BigDecimal.ZERO;

    @Column(name = "quantidadeRecebida", nullable = false)
    private Long quantidadeRecebida = 0L;
}
