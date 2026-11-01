package com.vilaverde.erp.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "fornecedor")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Fornecedor {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @NotBlank(message = "Nome fantasia é obrigatório")
  @Column(name = "nome_fantasia", nullable = false)
  private String nomeFantasia;

  @NotBlank(message = "Razão social é obrigatória")
  @Column(name = "razao_social")
  private String razaoSocial;

  @NotBlank(message = "Estado é obrigatório")
  @Column(name = "estado", length = 2)
  private String estado;

  @NotBlank(message = "Cidade é obrigatória")
  @Column(name = "cidade")
  private String cidade;

  @NotBlank(message = "Bairro é obrigatório")
  @Column(name = "bairro")
  private String bairro;

  private String logradouro;
  private String numero;
  private String cep;
  private String email;
  private String telefone;

  @Enumerated(EnumType.STRING)
  @Column(name = "status", nullable = false, length = 20)
  private com.vilaverde.erp.entity.enums.StatusCadastro status =
    com.vilaverde.erp.entity.enums.StatusCadastro.ATIVO;

  @Column(name = "complemento")
  private String complemento;

  @Column(name = "cnpj")
  private String cnpj;

  @Column(name = "cpf")
  private String cpf;
}
