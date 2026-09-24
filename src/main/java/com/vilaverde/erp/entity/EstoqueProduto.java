package com.vilaverde.erp.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "estoque_produto")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class EstoqueProduto {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_produto", nullable = false)
    private Produto produto;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_loja", nullable = false)
    private Loja loja;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_fornecedor")
    private Fornecedor fornecedor;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_transacao", nullable = false)
    private Transacao transacao;

    @Column(nullable = false, precision = 14, scale = 3)
    private BigDecimal quantidadeAnterior;

    @Column(nullable = false, precision = 14, scale = 3)
    private BigDecimal quantidadeRecebida;

    @Column(nullable = false, precision = 14, scale = 3)
    private BigDecimal quantidadeAtual;

    private String motivo;

    @Column(nullable = false)
    private LocalDateTime dataMovimentacao = LocalDateTime.now();
}
