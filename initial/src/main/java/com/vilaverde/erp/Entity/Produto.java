package com.vilaverde.erp.Entity;

import jakarta.persistence.*;
import java.util.UUID;

@Entity
@Table(name = "produto")
public class Produto {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id")
    private UUID id;

    @Column(name = "nome", nullable = false)
    private String nome;

    @ManyToOne
    @JoinColumn(name = "idTipo", referencedColumnName = "id", nullable = false)
    private Categoria categoria;

    @ManyToOne
    @JoinColumn(name = "idUnidadeMedida", referencedColumnName = "id", nullable = false)
    private UnidadeMedida unidadeMedida;

    @Column(name = "status", nullable = false)
    private String status;

    public Produto() {
    }

    public Produto(
            String nome,
            Categoria categoria,
            UnidadeMedida unidadeMedida,
            String status
    ) {
        this.nome = nome;
        this.categoria = categoria;
        this.unidadeMedida = unidadeMedida;
        this.status = status;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public Categoria getCategoria() {
        return categoria;
    }

    public void setCategoria(Categoria categoria) {
        this.categoria = categoria;
    }

    public UnidadeMedida getUnidadeMedida() {
        return unidadeMedida;
    }

    public void setUnidadeMedida(UnidadeMedida unidadeMedida) {
        this.unidadeMedida = unidadeMedida;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}