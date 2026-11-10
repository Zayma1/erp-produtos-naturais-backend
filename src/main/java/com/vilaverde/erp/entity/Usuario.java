package com.vilaverde.erp.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import java.time.OffsetDateTime;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "usuario")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Usuario {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @NotBlank(message = "Nome é obrigatório")
  @Column(name = "nome", nullable = false)
  private String nome;

  @NotBlank(message = "E-mail é obrigatório")
  @Column(name = "email", nullable = false, unique = true)
  private String email;

  @NotBlank(message = "Senha é obrigatória")
  @Column(name = "senha_hash", nullable = false)
  private String senhaHash;

  @Enumerated(EnumType.STRING)
  @Column(name = "cargo", nullable = false)
  private com.vilaverde.erp.entity.enums.CargoUsuario cargo = com.vilaverde.erp.entity.enums.CargoUsuario.FUNC;

  @ElementCollection(fetch = FetchType.EAGER)
  @CollectionTable(name="usuario_loja", joinColumns=@JoinColumn(name="id_usuario"))
  @Column(name="id_loja", nullable=false)
  private java.util.Set<UUID> lojas = new java.util.HashSet<>();

  @Column(name="alterar_senha", nullable=false)
  private boolean alterarSenha = true;

  @Column(name="revisado", nullable=false)
  private boolean revisado = false;

  @Column(name="auth_version", nullable=false)
  private long authVersion = 0;


  @Enumerated(EnumType.STRING)
  @Column(name = "status", nullable = false, length = 20)
  private com.vilaverde.erp.entity.enums.StatusCadastro status =
    com.vilaverde.erp.entity.enums.StatusCadastro.ATIVO;

  @Column(name = "criado_em", nullable = false)
  private OffsetDateTime criadoEm = OffsetDateTime.now();
}
