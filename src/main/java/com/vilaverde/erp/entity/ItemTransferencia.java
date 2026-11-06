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
public class ItemTransferencia {

  @Column(name="unidade_sigla_snapshot", length=10)
  private String unidadeSiglaSnapshot;

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "id_transferencia", nullable = false)
  private TransferenciaEstoque transferencia;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "id_produto", nullable = false)
  private Produto produto;

  @Column(nullable = false, precision = 14, scale = 3)
  private BigDecimal quantidade;
}
