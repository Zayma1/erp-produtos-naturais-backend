package com.vilaverde.erp.entity;

import com.vilaverde.erp.entity.enums.TipoMovimentacao;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;
import lombok.*;

@Entity
@Table(name = "movimentacao_estoque")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MovimentacaoEstoque extends SkuSnapshot {

  @ManyToOne(fetch=FetchType.LAZY)
  @JoinColumn(name="id_item_transferencia")
  private ItemTransferencia itemTransferencia;

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "id_produto_sku", nullable = false)
  private ProdutoSku produtoSku;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "id_loja", nullable = false)
  private Loja loja;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 30)
  private TipoMovimentacao tipo;

  @Column(nullable = false, precision = 14, scale = 3)
  private BigDecimal quantidade;

  @Column(name = "data_movimentacao", nullable = false)
  private OffsetDateTime dataMovimentacao;

  private String observacao;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "id_usuario")
  private Usuario usuario;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "id_movimentacao_origem")
  private MovimentacaoEstoque movimentacaoOrigem;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "id_venda")
  private Venda venda;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "id_compra")
  private Compra compra;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "id_transferencia")
  private TransferenciaEstoque transferencia;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "id_ajuste_estoque")
  private AjusteEstoque ajusteEstoque;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "id_devolucao_cliente")
  private DevolucaoCliente devolucaoCliente;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "id_devolucao_fornecedor")
  private DevolucaoFornecedor devolucaoFornecedor;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "id_item_venda")
  private ProdutoVenda itemVenda;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "id_item_ajuste_estoque")
  private ItemAjusteEstoque itemAjusteEstoque;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "id_item_devolucao_cliente")
  private ItemDevolucaoCliente itemDevolucaoCliente;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "id_item_devolucao_fornecedor")
  private ItemDevolucaoFornecedor itemDevolucaoFornecedor;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "id_item_recebimento_compra")
  private ItemRecebimentoCompra itemRecebimentoCompra;

  public void setProdutoSku(ProdutoSku sku) {
    if (produtoSku == null || !produtoSku.getId().equals(sku.getId())) capture(sku);
    produtoSku = sku;
  }
  public void setQuantidade(java.math.BigDecimal q) {produtoSku.validateQuantity(q);quantidade=q;}
}
