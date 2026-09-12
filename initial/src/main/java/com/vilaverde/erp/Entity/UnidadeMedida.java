package com.vilaverde.erp.Entity;

import jakarta.persistence.*;
import java.util.UUID;

@Entity
@Table(name = "unidadeMedida")
public class UnidadeMedida {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id")
    private UUID id;

    @Column(name = "sigla", nullable = false)
    private String sigla;

    @Column(name = "nome", nullable = false)
    private String nome;

    public UnidadeMedida() {
    }

    public UnidadeMedida(String sigla, String nome) {
        this.sigla = sigla;
        this.nome = nome;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getSigla() {
        return sigla;
    }

    public void setSigla(String sigla) {
        this.sigla = sigla;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }
}