package com.vilaverde.erp.entity;

import com.vilaverde.erp.entity.enums.StatusDocumento;
import jakarta.persistence.*;
import java.time.OffsetDateTime;
import java.util.UUID;
import lombok.*;

@Entity
@Table(name = "ajuste_estoque")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AjusteEstoque {

  @Version
  private Integer versao;
  @Column(name="atualizado_em")
  private OffsetDateTime atualizadoEm;
  @Column(name="cancelado_em")
  private OffsetDateTime canceladoEm;
  @Column(name="motivo_cancelamento")
  private String motivoCancelamento;
  private boolean legado;
  @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name="id_modificador")
  private Usuario modificador;
  @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name="id_confirmador")
  private Usuario confirmador;
  @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name="id_cancelador")
  private Usuario cancelador;

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "id_loja", nullable = false)
  private Loja loja;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "id_usuario", nullable = false)
  private Usuario usuario;

  @Column(nullable = false)
  private String justificativa;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 20)
  private StatusDocumento status = StatusDocumento.RASCUNHO;

  @Column(name = "criado_em", nullable = false)
  private OffsetDateTime criadoEm = OffsetDateTime.now();

  @Column(name = "concluido_em")
  private OffsetDateTime concluidoEm;
  @OneToMany(mappedBy="ajusteEstoque", cascade=CascadeType.ALL, orphanRemoval=true)
  private java.util.List<ItemAjusteEstoque> itens = new java.util.ArrayList<>();
}
