package com.vilaverde.erp.Entity;

import jakarta.persistence.*;
import java.util.UUID;

@Entity
@Table(name = "loja")
public class Loja {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id")
    private UUID id;

    @Column(name = "nomeFantasia", nullable = false)
    private String nomeFantasia;

    @Column(name = "razaoSocial", nullable = false)
    private String razaoSocial;

    @Column(name = "estado", nullable = false)
    private String estado;

    @Column(name = "cidade", nullable = false)
    private String cidade;

    @Column(name = "bairro", nullable = false)
    private String bairro;

    @Column(name = "numero", nullable = false)
    private String numero;

    @Column(name = "complemento", nullable = false)
    private String complemento;

    @Column(name = "cnpj", nullable = false)
    private String cnpj;

    public Loja() {
    }

    public Loja(
            String nomeFantasia,
            String razaoSocial,
            String estado,
            String cidade,
            String bairro,
            String numero,
            String complemento,
            String cnpj
    ) {
        this.nomeFantasia = nomeFantasia;
        this.razaoSocial = razaoSocial;
        this.estado = estado;
        this.cidade = cidade;
        this.bairro = bairro;
        this.numero = numero;
        this.complemento = complemento;
        this.cnpj = cnpj;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getNomeFantasia() {
        return nomeFantasia;
    }

    public void setNomeFantasia(String nomeFantasia) {
        this.nomeFantasia = nomeFantasia;
    }

    public String getRazaoSocial() {
        return razaoSocial;
    }

    public void setRazaoSocial(String razaoSocial) {
        this.razaoSocial = razaoSocial;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public String getCidade() {
        return cidade;
    }

    public void setCidade(String cidade) {
        this.cidade = cidade;
    }

    public String getBairro() {
        return bairro;
    }

    public void setBairro(String bairro) {
        this.bairro = bairro;
    }

    public String getNumero() {
        return numero;
    }

    public void setNumero(String numero) {
        this.numero = numero;
    }

    public String getComplemento() {
        return complemento;
    }

    public void setComplemento(String complemento) {
        this.complemento = complemento;
    }

    public String getCnpj() {
        return cnpj;
    }

    public void setCnpj(String cnpj) {
        this.cnpj = cnpj;
    }
}