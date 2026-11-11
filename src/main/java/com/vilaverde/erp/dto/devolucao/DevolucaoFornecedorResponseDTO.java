package com.vilaverde.erp.dto.devolucao;
import com.vilaverde.erp.entity.*;
import com.vilaverde.erp.entity.enums.StatusDocumento;
import java.util.*;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
public record DevolucaoFornecedorResponseDTO(UUID id,UUID idCompra,UUID idLoja,String lojaNome,UUID idUsuario,String usuarioNome,StatusDocumento status,Integer versao,boolean legado,String motivo,String observacao,OffsetDateTime dataDevolucao,OffsetDateTime criadoEm,OffsetDateTime atualizadoEm,OffsetDateTime concluidoEm,OffsetDateTime canceladoEm,String motivoCancelamento,UUID idModificador,String nomeModificador,UUID idConfirmador,String nomeConfirmador,UUID idCancelador,String nomeCancelador,List<ItemDevolucaoFornecedorResponseDTO> itens) {
 public record ItemDevolucaoFornecedorResponseDTO(UUID id,UUID idItemCompra,UUID idProduto,
  UUID idProdutoSku, String skuCodigoSnapshot, String skuApresentacaoSnapshot,String nomeProduto,BigDecimal quantidade,Boolean retornaAoEstoque,String observacao,String unidadeSigla,String unidadeOrigem) {}
 public static DevolucaoFornecedorResponseDTO fromEntity(DevolucaoFornecedor e) {
  return new DevolucaoFornecedorResponseDTO(e.getId(),e.getCompra().getId(),e.getCompra().getLoja().getId(),e.getCompra().getLoja().getNomeFantasia(),id(e.getUsuario()),name(e.getUsuario()),e.getStatus(),e.getVersao(),e.isLegado(),e.getMotivo(),e.getObservacao(),e.getDataDevolucao(),e.getCriadoEm(),e.getAtualizadoEm(),e.getConcluidoEm(),e.getCanceladoEm(),e.getMotivoCancelamento(),id(e.getModificador()),name(e.getModificador()),id(e.getConfirmador()),name(e.getConfirmador()),id(e.getCancelador()),name(e.getCancelador()),e.getItens().stream().map(i->new ItemDevolucaoFornecedorResponseDTO(i.getId(),i.getItemCompra().getId(),i.getProdutoSku().getProduto().getId(),
      i.getProdutoSku().getId(),i.getSkuCodigoSnapshot(),i.getSkuApresentacaoSnapshot(),i.getNomeProdutoSnapshot(),i.getQuantidade(),false,i.getObservacao(),i.getUnidadeSiglaSnapshot(),i.getUnidadeOrigem())).toList());
 }
 private static UUID id(Usuario u) {return u==null?null:u.getId();}
 private static String name(Usuario u) {return u==null?null:u.getNome();}
}
