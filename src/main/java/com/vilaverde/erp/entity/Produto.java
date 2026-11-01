package com.vilaverde.erp.entity;

import com.vilaverde.erp.entity.enums.StatusProduto;
import com.vilaverde.erp.entity.enums.TipoVenda;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "produto")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Produto {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @NotBlank(message = "Nome do produto é obrigatório")
  @Column(name = "nome", nullable = false)
  private String nome;

  @Column(name = "descricao")
  private String descricao;

  @NotNull(message = "Tipo/categoria é obrigatório")
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "id_categoria", nullable = false)
  private Categoria categoria;

  @NotNull(message = "Unidade de medida é obrigatória")
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "id_unidade_medida", nullable = false)
  private UnidadeMedida unidadeMedida;

  @Enumerated(EnumType.STRING)
  @Column(name = "tipo_venda", nullable = false, length = 20)
  private TipoVenda tipoVenda = TipoVenda.UNIDADE;

  @Column(name = "criado_em", nullable = false)
  private java.time.OffsetDateTime criadoEm = java.time.OffsetDateTime.now();

  @NotNull
  @Enumerated(EnumType.STRING)
  @Column(name = "status", nullable = false)
  private StatusProduto status = StatusProduto.ATIVO;
}
