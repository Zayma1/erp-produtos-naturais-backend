package com.vilaverde.erp.entity;

import com.vilaverde.erp.entity.enums.StatusCadastro;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(
  name = "produto_loja",
  uniqueConstraints = @UniqueConstraint(
    name = "uq_produto_loja_produto_loja",
    columnNames = { "id_produto", "id_loja" }
  )
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ProdutoLoja {

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

  @Enumerated(EnumType.STRING)
  @Column(name = "status", nullable = false, length = 20)
  private StatusCadastro status = StatusCadastro.ATIVO;

  @Column(name = "criado_em", nullable = false)
  private java.time.OffsetDateTime criadoEm = java.time.OffsetDateTime.now();
}
