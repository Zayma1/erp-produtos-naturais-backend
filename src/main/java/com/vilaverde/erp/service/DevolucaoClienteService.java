package com.vilaverde.erp.service;

import com.vilaverde.erp.dto.devolucao.*;
import com.vilaverde.erp.entity.*;
import com.vilaverde.erp.entity.enums.StatusDocumento;
import com.vilaverde.erp.exception.*;
import com.vilaverde.erp.repository.*;
import java.time.OffsetDateTime;
import java.util.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class DevolucaoClienteService {

  private final DevolucaoClienteRepository repository;
  private final ItemDevolucaoClienteRepository itemRepository;
  private final VendaRepository vendaRepository;
  private final UsuarioRepository usuarioRepository;
  private final ProdutoVendaRepository produtoVendaRepository;
  private final ProdutoRepository produtoRepository;

  @Transactional(readOnly = true)
  public List<DevolucaoClienteResponseDTO> listar() {
    return repository
      .findAll()
      .stream()
      .map(DevolucaoClienteResponseDTO::fromEntity)
      .toList();
  }

  @Transactional(readOnly = true)
  public DevolucaoClienteResponseDTO buscar(UUID id) {
    return DevolucaoClienteResponseDTO.fromEntity(
      repository
        .findById(id)
        .orElseThrow(() ->
          new ResourceNotFoundException("Devolução de cliente não encontrada")
        )
    );
  }

  @Transactional
  public DevolucaoClienteResponseDTO criar(DevolucaoClienteRequestDTO d) {
    DevolucaoCliente e = new DevolucaoCliente();
    e.setVenda(
      vendaRepository
        .findById(d.idVenda())
        .orElseThrow(() ->
          new ResourceNotFoundException("Venda não encontrada")
        )
    );
    e.setUsuario(
      d.idUsuario() == null
        ? null
        : usuarioRepository
            .findById(d.idUsuario())
            .orElseThrow(() ->
              new ResourceNotFoundException("Usuário não encontrado")
            )
    );
    e.setStatus(StatusDocumento.RASCUNHO);
    e.setDataDevolucao(
      d.dataDevolucao() == null ? OffsetDateTime.now() : d.dataDevolucao()
    );
    e.setMotivo(d.motivo());
    e.setObservacao(d.observacao());
    e.setCriadoEm(OffsetDateTime.now());
    e = repository.save(e);
    for (ItemDevolucaoClienteRequestDTO x : d.itens()) {
      ItemDevolucaoCliente i = new ItemDevolucaoCliente();
      i.setDevolucaoCliente(e);
      i.setItemVenda(
        produtoVendaRepository
          .findById(x.idItemVenda())
          .orElseThrow(() ->
            new ResourceNotFoundException("Item de venda não encontrado")
          )
      );
      i.setProduto(
        produtoRepository
          .findById(x.idProduto())
          .orElseThrow(() ->
            new ResourceNotFoundException("Produto não encontrado")
          )
      );
      i.setQuantidade(x.quantidade());
      i.setRetornaAoEstoque(true);
      i.setObservacao(x.observacao());
      itemRepository.save(i);
    }
    if (d.status() == StatusDocumento.CONCLUIDA) {
      e.setStatus(StatusDocumento.CONCLUIDA);
      repository.save(e);
    }
    return buscar(e.getId());
  }

  @Transactional
  public void excluir(UUID id) {
    DevolucaoCliente e = repository
      .findById(id)
      .orElseThrow(() ->
        new ResourceNotFoundException("Devolução de cliente não encontrada")
      );
    if (e.getStatus() != StatusDocumento.RASCUNHO) throw new BusinessException(
      "Somente devoluções em rascunho podem ser excluídas"
    );
    repository.delete(e);
  }
}
