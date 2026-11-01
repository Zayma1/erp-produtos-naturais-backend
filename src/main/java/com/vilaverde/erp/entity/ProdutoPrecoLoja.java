package com.vilaverde.erp.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;
import lombok.*;

@Entity
@Table(name = "produto_preco_loja")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ProdutoPrecoLoja {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "id_produto", nullable = false)
  private Produto produto;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "id_loja", nullable = false)
  private Loja loja;

  @Column(nullable = false, precision = 12, scale = 2)
  private BigDecimal preco;

  @Column(name = "data_inicio", nullable = false)
  private OffsetDateTime dataInicio;

  @Column(name = "data_fim")
  private OffsetDateTime dataFim;

  @Column(name = "criado_em", nullable = false)
  private OffsetDateTime criadoEm;
}
