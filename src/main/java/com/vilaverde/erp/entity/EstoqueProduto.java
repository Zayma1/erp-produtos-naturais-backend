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
@Table(
  name = "estoque_produto",
  uniqueConstraints = @UniqueConstraint(
    columnNames = { "id_produto", "id_loja" }
  )
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class EstoqueProduto {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @NotNull
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "id_produto", nullable = false)
  private Produto produto;

  @NotNull
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "id_loja", nullable = false)
  private Loja loja;

  @Column(
    name = "quantidade_atual",
    nullable = false,
    precision = 14,
    scale = 3
  )
  private BigDecimal quantidadeAtual = BigDecimal.ZERO;

  @Column(name = "atualizado_em", nullable = false)
  private java.time.OffsetDateTime atualizadoEm =
    java.time.OffsetDateTime.now();
}
