package com.vilaverde.erp.entity;

import com.vilaverde.erp.entity.enums.StatusDocumento;
import jakarta.persistence.*;
import java.time.OffsetDateTime;
import java.util.UUID;
import lombok.*;

@Entity
@Table(name = "devolucao_cliente")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DevolucaoCliente {

  @Version private Integer versao;
  @Column(name="atualizado_em") private OffsetDateTime atualizadoEm;
  @Column(name="concluido_em") private OffsetDateTime concluidoEm;
  @Column(name="cancelado_em") private OffsetDateTime canceladoEm;
  @Column(name="motivo_cancelamento") private String motivoCancelamento;
  private boolean legado;
  @ManyToOne(fetch=FetchType.LAZY) @JoinColumn(name="id_modificador") private Usuario modificador;
  @ManyToOne(fetch=FetchType.LAZY) @JoinColumn(name="id_confirmador") private Usuario confirmador;
  @ManyToOne(fetch=FetchType.LAZY) @JoinColumn(name="id_cancelador") private Usuario cancelador;
  @OneToMany(mappedBy="devolucaoCliente",cascade=CascadeType.ALL,orphanRemoval=true)
  private java.util.List<ItemDevolucaoCliente> itens=new java.util.ArrayList<>();

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "id_venda", nullable = false)
  private Venda venda;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "id_usuario")
  private Usuario usuario;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 20)
  private StatusDocumento status = StatusDocumento.RASCUNHO;

  @Column(name = "data_devolucao")
  private OffsetDateTime dataDevolucao;

  private String motivo;
  private String observacao;

  @Column(name = "criado_em", nullable = false)
  private OffsetDateTime criadoEm=OffsetDateTime.now();
}
