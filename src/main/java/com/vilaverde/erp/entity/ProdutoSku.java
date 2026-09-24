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
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_loja", nullable = false)
    private Loja loja;

    @NotNull(message = "Preço é obrigatório")
    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal preco;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private StatusProduto status = StatusProduto.ATIVO;

    @NotBlank(message = "SKU é obrigatório")
    @Column(nullable = false, unique = true)
    private String sku;

    @Column(length = 13)
    private String ean13;

    @Column(length = 14)
    private String dun14;
}
