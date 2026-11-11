package com.vilaverde.erp.dto.devolucao;
import com.vilaverde.erp.entity.*;
import com.vilaverde.erp.entity.enums.StatusDocumento;
import java.util.*;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
public record DevolucaoClienteResponseDTO(UUID id,UUID idVenda,UUID idLoja,String lojaNome,UUID idUsuario,String usuarioNome,StatusDocumento status,Integer versao,boolean legado,String motivo,String observacao,OffsetDateTime dataDevolucao,OffsetDateTime criadoEm,OffsetDateTime atualizadoEm,OffsetDateTime concluidoEm,OffsetDateTime canceladoEm,String motivoCancelamento,UUID idModificador,String nomeModificador,UUID idConfirmador,String nomeConfirmador,UUID idCancelador,String nomeCancelador,List<ItemDevolucaoClienteResponseDTO> itens) {
 public record ItemDevolucaoClienteResponseDTO(UUID id,UUID idItemVenda,UUID idProduto,
  UUID idProdutoSku, String skuCodigoSnapshot, String skuApresentacaoSnapshot,String nomeProduto,BigDecimal quantidade,Boolean retornaAoEstoque,String observacao,String unidadeSigla,String unidadeOrigem) {}
 public static DevolucaoClienteResponseDTO fromEntity(DevolucaoCliente e) {
  return new DevolucaoClienteResponseDTO(e.getId(),e.getVenda().getId(),e.getVenda().getLoja().getId(),e.getVenda().getLoja().getNomeFantasia(),id(e.getUsuario()),name(e.getUsuario()),e.getStatus(),e.getVersao(),e.isLegado(),e.getMotivo(),e.getObservacao(),e.getDataDevolucao(),e.getCriadoEm(),e.getAtualizadoEm(),e.getConcluidoEm(),e.getCanceladoEm(),e.getMotivoCancelamento(),id(e.getModificador()),name(e.getModificador()),id(e.getConfirmador()),name(e.getConfirmador()),id(e.getCancelador()),name(e.getCancelador()),e.getItens().stream().map(i->new ItemDevolucaoClienteResponseDTO(i.getId(),i.getItemVenda().getId(),i.getProdutoSku().getProduto().getId(),
      i.getProdutoSku().getId(),i.getSkuCodigoSnapshot(),i.getSkuApresentacaoSnapshot(),i.getNomeProdutoSnapshot(),i.getQuantidade(),i.getRetornaAoEstoque(),i.getObservacao(),i.getUnidadeSiglaSnapshot(),i.getUnidadeOrigem())).toList());
 }
 private static UUID id(Usuario u) {return u==null?null:u.getId();}
 private static String name(Usuario u) {return u==null?null:u.getNome();}
}
