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
@Table(name = "produto_sku")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ProdutoSku {
  @NotNull(message = "Unidade de medida é obrigatória")
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "id_unidade_medida", nullable = false)
  private UnidadeMedida unidadeMedida;

  @Enumerated(EnumType.STRING)
  @Column(name = "tipo_venda", nullable = false, length = 20)
  private TipoVenda tipoVenda = TipoVenda.UNIDADE;



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

  @NotBlank
  @Column(name = "descricao_apresentacao", nullable=false)
  private String descricaoApresentacao;

  @Column(name = "criado_em", nullable = false)
  private java.time.OffsetDateTime criadoEm = java.time.OffsetDateTime.now();

  public String getDisplayName() {return produto.getNome()+" / "+descricaoApresentacao+" / "+sku+" / "+unidadeMedida.getSigla();}
  public void validateQuantity(java.math.BigDecimal q) {
    if(q==null || q.signum()<=0 || q.stripTrailingZeros().scale()>3 || q.compareTo(new java.math.BigDecimal("100000000000"))>=0 || (tipoVenda==TipoVenda.UNIDADE && q.stripTrailingZeros().scale()>0))
      throw new com.vilaverde.erp.exception.InvalidInputException("Quantidade inválida para SKU "+sku+": "+(tipoVenda==TipoVenda.UNIDADE ? "informe quantidade inteira" : "até três casas decimais"));
  }
  public void requireActive() {
    if(status!=StatusProduto.ATIVO || produto.getStatus()!=StatusProduto.ATIVO) throw new com.vilaverde.erp.exception.InvalidInputException("SKU/produto inativo: "+sku);
  }
}
