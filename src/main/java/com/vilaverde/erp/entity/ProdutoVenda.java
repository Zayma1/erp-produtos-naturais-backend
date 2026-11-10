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
public class ProdutoVenda extends SkuSnapshot {

  @org.hibernate.annotations.Formula("devolucao_cliente_quantidade(id)")
  private BigDecimal quantidadeDevolvida;
  @org.hibernate.annotations.Formula("devolucao_cliente_legada(id_venda)")
  private boolean devolucaoLegadaPendente;
  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @NotNull
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "id_venda", nullable = false)
  private Venda venda;

  @NotNull
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "id_produto_sku", nullable = false)
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


  public void setProdutoSku(ProdutoSku sku) {
    if (produtoSku == null || !produtoSku.getId().equals(sku.getId())) capture(sku);
    produtoSku = sku;
  }
  public void setQuantidade(java.math.BigDecimal q) {produtoSku.validateQuantity(q);quantidade=q;}
}
