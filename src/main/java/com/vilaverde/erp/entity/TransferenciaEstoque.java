package com.vilaverde.erp.entity;

import com.vilaverde.erp.entity.enums.StatusDocumento;
import jakarta.persistence.*;
import java.time.OffsetDateTime;
import java.util.UUID;
import lombok.*;

@Entity
@Table(name = "transferencia_estoque")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TransferenciaEstoque {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "id_loja_origem", nullable = false)
  private Loja lojaOrigem;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "id_loja_destino", nullable = false)
  private Loja lojaDestino;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "id_usuario")
  private Usuario usuario;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 20)
  private StatusDocumento status = StatusDocumento.RASCUNHO;

  @Column(name = "data_transferencia")
  private OffsetDateTime dataTransferencia;

  private String observacao;

  @Column(name = "criado_em", nullable = false)
  private OffsetDateTime criadoEm;
}
