package com.vilaverde.erp.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.util.UUID;
import lombok.*;

@Entity
@Table(name = "item_devolucao_fornecedor")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ItemDevolucaoFornecedor extends SkuSnapshot {

  @Column(name="unidade_sigla_snapshot",length=10) private String unidadeSiglaSnapshot;
  @Column(name="unidade_origem") private String unidadeOrigem;
  @Column(name="nome_produto_snapshot") private String nomeProdutoSnapshot;
  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "id_devolucao_fornecedor", nullable = false)
  private DevolucaoFornecedor devolucaoFornecedor;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "id_item_compra", nullable = false)
  private ItemCompra itemCompra;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "id_produto_sku", nullable = false)
  private ProdutoSku produtoSku;

  @Column(nullable = false, precision = 14, scale = 3)
  private BigDecimal quantidade;

  private String observacao;

  public void setProdutoSku(ProdutoSku sku) {
    if (produtoSku == null || !produtoSku.getId().equals(sku.getId())) capture(sku);
    produtoSku = sku;
  }
  public void setQuantidade(java.math.BigDecimal q) {produtoSku.validateQuantity(q);quantidade=q;}
}
