package com.vilaverde.erp.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.util.UUID;
import lombok.*;

@Entity
@Table(name = "item_recebimento_compra")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ItemRecebimentoCompra {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "id_recebimento_compra", nullable = false)
  private RecebimentoCompra recebimentoCompra;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "id_item_compra", nullable = false)
  private ItemCompra itemCompra;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "id_produto", nullable = false)
  private Produto produto;

  @Column(nullable = false, precision = 14, scale = 3)
  private BigDecimal quantidade;

  private String observacao;
}
