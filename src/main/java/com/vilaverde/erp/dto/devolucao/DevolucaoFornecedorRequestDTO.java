package com.vilaverde.erp.dto.devolucao;
import com.vilaverde.erp.entity.enums.StatusDocumento;
import java.util.*;
import java.time.OffsetDateTime;
public record DevolucaoFornecedorRequestDTO(UUID idCompra,UUID idUsuario,OffsetDateTime dataDevolucao,StatusDocumento status,String motivo,String observacao,Integer versao,List<ItemDevolucaoFornecedorRequestDTO> itens) {}
