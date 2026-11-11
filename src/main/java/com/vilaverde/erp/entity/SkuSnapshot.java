package com.vilaverde.erp.entity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
@MappedSuperclass @Getter @Setter
public abstract class SkuSnapshot {
 @Column(name="sku_codigo_snapshot",nullable=false,length=80) private String skuCodigoSnapshot;
 @Column(name="sku_apresentacao_snapshot",nullable=false) private String skuApresentacaoSnapshot;
 @Column(name="unidade_sigla_snapshot",nullable=false,length=10) private String unidadeSiglaSnapshot;
 protected void capture(ProdutoSku s) {skuCodigoSnapshot=s.getSku();skuApresentacaoSnapshot=s.getDescricaoApresentacao();unidadeSiglaSnapshot=s.getUnidadeMedida().getSigla();}
 public void snapshotFrom(SkuSnapshot source) {skuCodigoSnapshot=source.skuCodigoSnapshot;skuApresentacaoSnapshot=source.skuApresentacaoSnapshot;unidadeSiglaSnapshot=source.unidadeSiglaSnapshot;}
}
