package com.vilaverde.erp.Entity;


import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "usuario")
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id")
    private UUID id;

    @Column(name = "nome", nullable = false)
    private String nome;

    @Column(name = "email", nullable = false)
    private String email;

    @Column(name = "senha", nullable = false)
    private String senha;

    @Column(name = "criacaoTimestamp", nullable = false)
    private LocalDateTime criacaoTimestamp;

    @Column(name = "dataCriacao", nullable = false)
    private LocalDate dataCriacao;

    @Column(name = "cargo", nullable = false)
    private String cargo;

    public Usuario() {
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

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getSenha() {
        return senha;
    }

    public void setSenha(String senha) {
        this.senha = senha;
    }

    public LocalDateTime getCriacaoTimestamp() {
        return criacaoTimestamp;
    }

    public void setCriacaoTimestamp(LocalDateTime criacaoTimestamp) {
        this.criacaoTimestamp = criacaoTimestamp;
    }

    public LocalDate getDataCriacao() {
        return dataCriacao;
    }

    public void setDataCriacao(LocalDate dataCriacao) {
        this.dataCriacao = dataCriacao;
    }

    public String getCargo() {
        return cargo;
    }

    public void setCargo(String cargo) {
        this.cargo = cargo;
    }
}