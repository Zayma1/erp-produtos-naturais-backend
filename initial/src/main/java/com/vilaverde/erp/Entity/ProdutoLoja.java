package com.vilaverde.erp.Entity;

import jakarta.persistence.*;
import java.util.UUID;

@Entity
@Table(name = "produtoLoja")
public class ProdutoLoja {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id")
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "idProduto", referencedColumnName = "id", nullable = false)
    private Produto produto;

    @ManyToOne
    @JoinColumn(name = "idLoja", referencedColumnName = "id", nullable = false)
    private Loja loja;

    public ProdutoLoja() {
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

    public Loja getLoja() {
        return loja;
    }

    public void setLoja(Loja loja) {
        this.loja = loja;
    }
}
