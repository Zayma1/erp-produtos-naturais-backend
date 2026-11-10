package com.vilaverde.erp.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "loja")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Loja {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @NotBlank(message = "Nome fantasia é obrigatório")
  @Column(name = "nome_fantasia", nullable = false, length = 150)
  private String nomeFantasia;

  @NotBlank(message = "Razão social é obrigatória")
  @Column(name = "razao_social", nullable = false)
  private String razaoSocial;

  @NotBlank(message = "Estado é obrigatório")
  @Column(name = "estado", nullable = false)
  private String estado;

  @NotBlank(message = "Cidade é obrigatória")
  @Column(name = "cidade", nullable = false)
  private String cidade;

  @NotBlank(message = "Bairro é obrigatório")
  @Column(name = "bairro")
  private String bairro;

  @NotBlank(message = "Número é obrigatório")
  @Column(name = "numero")
  private String numero;

  @Column(name = "complemento")
  private String complemento;

  @NotBlank(message = "CNPJ é obrigatório")
  @Column(name = "cnpj", nullable = false, length = 18)
  private String cnpj;

  private String logradouro;
  private String cep;

  @Enumerated(EnumType.STRING)
  @Column(name = "status", nullable = false, length = 20)
  private com.vilaverde.erp.entity.enums.StatusLoja status =
    com.vilaverde.erp.entity.enums.StatusLoja.ATIVA;

  @Column(name = "criado_em", nullable = false)
  private java.time.OffsetDateTime criadoEm = java.time.OffsetDateTime.now();
}
