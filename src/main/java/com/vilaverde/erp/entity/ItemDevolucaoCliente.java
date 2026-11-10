package com.vilaverde.erp.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.util.UUID;
import lombok.*;

@Entity
@Table(name = "item_devolucao_cliente")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ItemDevolucaoCliente {

  @Column(name="unidade_sigla_snapshot",length=10) private String unidadeSiglaSnapshot;
  @Column(name="unidade_origem") private String unidadeOrigem;
  @Column(name="nome_produto_snapshot") private String nomeProdutoSnapshot;
  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "id_devolucao_cliente", nullable = false)
  private DevolucaoCliente devolucaoCliente;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "id_item_venda", nullable = false)
  private ProdutoVenda itemVenda;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "id_produto", nullable = false)
  private Produto produto;

  @Column(nullable = false, precision = 14, scale = 3)
  private BigDecimal quantidade;

  @Column(name = "retorna_ao_estoque", nullable = false)
  private Boolean retornaAoEstoque = true;

  private String observacao;
}
