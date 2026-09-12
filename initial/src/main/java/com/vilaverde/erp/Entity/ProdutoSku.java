package com.vilaverde.erp.Entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "produtoSku")
public class ProdutoSku {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id")
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "idProduto", referencedColumnName = "id", nullable = false)
    private Produto produto;

    @Column(name = "preco", nullable = false, precision = 8, scale = 2)
    private BigDecimal preco;

    @Column(name = "status", nullable = false)
    private String status;

    @Column(name = "sku", nullable = false, unique = true)
    private String sku;

    @Column(name = "ean13", nullable = false)
    private String ean13;

    @Column(name = "dun14", nullable = false)
    private String dun14;

    @ManyToOne
    @JoinColumn(name = "idloja", referencedColumnName = "id", nullable = false)
    private Loja loja;

    public ProdutoSku() {
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public Produto getProduto() {
        return produto;
    }

    public void setProduto(Produto produto) {
        this.produto = produto;
    }

    public BigDecimal getPreco() {
        return preco;
    }

    public void setPreco(BigDecimal preco) {
        this.preco = preco;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getSku() {
        return sku;
    }

    public void setSku(String sku) {
        this.sku = sku;
    }

    public String getEan13() {
        return ean13;
    }

    public void setEan13(String ean13) {
        this.ean13 = ean13;
    }

    public String getDun14() {
        return dun14;
    }

    public void setDun14(String dun14) {
        this.dun14 = dun14;
    }

    public Loja getLoja() {
        return loja;
    }

    public void setLoja(Loja loja) {
        this.loja = loja;
    }
}