package com.vilaverde.erp.entity;

import com.vilaverde.erp.entity.enums.StatusCompra;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.*;
import lombok.*;

@Entity
@Table(name = "compra")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Compra {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "id_fornecedor", nullable = false)
  private Fornecedor fornecedor;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "id_loja", nullable = false)
  private Loja loja;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "id_usuario")
  private Usuario usuario;

  @Column(name = "data_compra", nullable = false)
  private OffsetDateTime dataCompra;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 30)
  private StatusCompra status = StatusCompra.RASCUNHO;

  @Column(name = "valor_total", nullable = false, precision = 14, scale = 2)
  private BigDecimal valorTotal = BigDecimal.ZERO;

  private String observacao;

  @Column(name = "criado_em", nullable = false)
  private OffsetDateTime criadoEm;
}
