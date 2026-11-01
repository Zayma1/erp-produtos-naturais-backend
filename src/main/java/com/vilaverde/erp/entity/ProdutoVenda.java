package com.vilaverde.erp.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "produto_venda")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ProdutoVenda {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @NotNull
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "id_venda", nullable = false)
  private Venda venda;

  @NotNull
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "id_produto", nullable = false)
  private Produto produto;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "id_produto_sku")
  private ProdutoSku produtoSku;

  @NotNull
  @Column(name = "quantidade", nullable = false, precision = 14, scale = 3)
  private BigDecimal quantidade;

  @NotNull
  @Column(name = "preco_unitario", nullable = false, precision = 12, scale = 2)
  private BigDecimal precoUnitario;

  @Column(name = "desconto", nullable = false, precision = 14, scale = 2)
  private BigDecimal desconto = BigDecimal.ZERO;

  @Column(name = "total_item", nullable = false, precision = 14, scale = 2)
  private BigDecimal totalItem = BigDecimal.ZERO;

  @Column(name = "unidade_sigla_snapshot", nullable = false, length = 10)
  private String unidadeSiglaSnapshot;
}
