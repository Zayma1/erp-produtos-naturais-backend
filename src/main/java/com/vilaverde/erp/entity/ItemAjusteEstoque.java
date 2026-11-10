package com.vilaverde.erp.entity;

import com.vilaverde.erp.entity.enums.TipoMovimentacao;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.util.UUID;
import lombok.*;

@Entity
@Table(name = "item_ajuste_estoque")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ItemAjusteEstoque {

  @Column(name="unidade_sigla_snapshot") private String unidadeSiglaSnapshot;
  @Column(name="saldo_antes",precision=14, scale=3) private BigDecimal saldoAntes;
  @Column(name="saldo_depois",precision=14, scale=3) private BigDecimal saldoDepois;

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "id_ajuste_estoque", nullable = false)
  private AjusteEstoque ajusteEstoque;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "id_produto", nullable = false)
  private Produto produto;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 30)
  private TipoMovimentacao tipo;

  @Column(nullable = false, precision = 14, scale = 3)
  private BigDecimal quantidade;

  private String observacao;
}
