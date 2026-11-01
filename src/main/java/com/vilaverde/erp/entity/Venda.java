package com.vilaverde.erp.entity;

import com.vilaverde.erp.entity.enums.StatusVenda;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "venda")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Venda {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @Column(name = "data_venda", nullable = false)
  private OffsetDateTime dataVenda = OffsetDateTime.now();

  @Enumerated(EnumType.STRING)
  @Column(name = "status", nullable = false, length = 20)
  private StatusVenda status = StatusVenda.ABERTA;

  @Column(name = "subtotal", nullable = false, precision = 14, scale = 2)
  private BigDecimal subtotal = BigDecimal.ZERO;

  @Column(name = "desconto_total", nullable = false, precision = 14, scale = 2)
  private BigDecimal descontoTotal = BigDecimal.ZERO;

  @Column(name = "valor_total", nullable = false, precision = 14, scale = 2)
  private BigDecimal valorTotal = BigDecimal.ZERO;

  @Column(name = "observacao")
  private String observacao;

  @Column(name = "criado_em", nullable = false)
  private OffsetDateTime criadoEm = OffsetDateTime.now();

  @NotNull
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "id_cliente")
  private Cliente cliente;

  @NotNull
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "id_usuario")
  private Usuario usuario;

  @NotNull
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "id_loja", nullable = false)
  private Loja loja;

  @OneToMany(
    mappedBy = "venda",
    cascade = CascadeType.ALL,
    orphanRemoval = true
  )
  private List<ProdutoVenda> itens = new ArrayList<>();
}
