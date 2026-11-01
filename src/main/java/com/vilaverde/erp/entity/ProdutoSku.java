package com.vilaverde.erp.entity;

import com.vilaverde.erp.entity.enums.StatusProduto;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "produto_sku")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ProdutoSku {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @NotNull
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "id_produto", nullable = false)
  private Produto produto;

  @NotNull
  @Enumerated(EnumType.STRING)
  @Column(name = "status", nullable = false)
  private StatusProduto status = StatusProduto.ATIVO;

  @NotBlank(message = "SKU é obrigatório")
  @Column(name = "sku", nullable = false, unique = true, length = 80)
  private String sku;

  @Column(name = "ean13")
  private String ean13;

  @Column(name = "dun14")
  private String dun14;

  @Column(name = "descricao_apresentacao")
  private String descricaoApresentacao;

  @Column(name = "criado_em", nullable = false)
  private java.time.OffsetDateTime criadoEm = java.time.OffsetDateTime.now();
}
