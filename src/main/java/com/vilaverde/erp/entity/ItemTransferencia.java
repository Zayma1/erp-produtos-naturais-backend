package com.vilaverde.erp.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.util.UUID;
import lombok.*;

@Entity
@Table(name = "item_transferencia")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ItemTransferencia extends SkuSnapshot {


  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "id_transferencia", nullable = false)
  private TransferenciaEstoque transferencia;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "id_produto_sku", nullable = false)
  private ProdutoSku produtoSku;

  @Column(nullable = false, precision = 14, scale = 3)
  private BigDecimal quantidade;

  public void setProdutoSku(ProdutoSku sku) {
    if (produtoSku == null || !produtoSku.getId().equals(sku.getId())) capture(sku);
    produtoSku = sku;
  }
  public void setQuantidade(java.math.BigDecimal q) {produtoSku.validateQuantity(q);quantidade=q;}
}
