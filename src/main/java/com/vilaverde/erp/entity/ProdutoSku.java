package com.vilaverde.erp.entity;

import com.vilaverde.erp.entity.enums.StatusProduto;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "produtoSku")
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
    @JoinColumn(name = "idProduto", nullable = false)
    private Produto produto;

    @NotNull(message = "Preço é obrigatório")
    @Column(name = "preco", nullable = false, precision = 12, scale = 2)
    private BigDecimal preco;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private StatusProduto status = StatusProduto.ATIVO;

    @NotBlank(message = "SKU é obrigatório")
    @Column(name = "sku", nullable = false, unique = true)
    private String sku;

    @Column(name = "ean13")
    private String ean13;

    @Column(name = "dun14")
    private String dun14;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "idloja")
    private Loja loja;
}
