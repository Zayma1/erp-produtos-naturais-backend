package com.vilaverde.erp.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.util.UUID;
import lombok.*;

@Entity
@Table(name = "item_compra")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ItemCompra extends SkuSnapshot {

  @org.hibernate.annotations.Formula("devolucao_fornecedor_quantidade(id)")
  private BigDecimal quantidadeDevolvida;
  @org.hibernate.annotations.Formula("devolucao_fornecedor_legada(id_compra)")
  private boolean devolucaoLegadaPendente;
  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "id_compra", nullable = false)
  private Compra compra;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "id_produto_sku", nullable = false)
  private ProdutoSku produtoSku;

  @Column(nullable = false, precision = 14, scale = 3)
  private BigDecimal quantidade;

  @Column(name = "custo_unitario", nullable = false, precision = 14, scale = 4)
  private BigDecimal custoUnitario;

  @Column(nullable = false, precision = 14, scale = 2)
  private BigDecimal desconto = BigDecimal.ZERO;

  @Column(name = "total_item", nullable = false, precision = 14, scale = 2)
  private BigDecimal totalItem = BigDecimal.ZERO;

  @Column(
    name = "quantidade_recebida",
    nullable = false,
    precision = 14,
    scale = 3
  )
  private BigDecimal quantidadeRecebida = BigDecimal.ZERO;

  public void setProdutoSku(ProdutoSku sku) {
    if (produtoSku == null || !produtoSku.getId().equals(sku.getId())) capture(sku);
    produtoSku = sku;
  }
  public void setQuantidade(java.math.BigDecimal q) {produtoSku.validateQuantity(q);quantidade=q;}
}
