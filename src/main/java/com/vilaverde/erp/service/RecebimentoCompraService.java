package com.vilaverde.erp.service;

import com.vilaverde.erp.dto.recebimento.*;
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
public class RecebimentoCompraService {

  private final RecebimentoCompraRepository repository;
  private final ItemRecebimentoCompraRepository itemRepository;
  private final CompraRepository compraRepository;
  private final ItemCompraRepository itemCompraRepository;
  private final ProdutoRepository produtoRepository;
  private final UsuarioRepository usuarioRepository;

  @Transactional(readOnly = true)
  public List<RecebimentoCompraResponseDTO> listar() {
    return repository
      .findAll()
      .stream()
      .map(RecebimentoCompraResponseDTO::fromEntity)
      .toList();
  }

  @Transactional(readOnly = true)
  public RecebimentoCompraResponseDTO buscar(UUID id) {
    return RecebimentoCompraResponseDTO.fromEntity(
      repository
        .findById(id)
        .orElseThrow(() ->
          new ResourceNotFoundException("Recebimento não encontrado")
        )
    );
  }

  @Transactional
  public RecebimentoCompraResponseDTO criar(RecebimentoCompraRequestDTO d) {
    RecebimentoCompra e = new RecebimentoCompra();
    aplicar(e, d);
    e.setStatus(StatusDocumento.RASCUNHO);
    e = repository.save(e);
    salvarItens(e, d.itens());
    if (d.status() == StatusDocumento.CONCLUIDO) {
      e.setStatus(StatusDocumento.CONCLUIDO);
      repository.save(e);
    }
    return RecebimentoCompraResponseDTO.fromEntity(e);
  }

  @Transactional
  public RecebimentoCompraResponseDTO atualizar(
    UUID id,
    RecebimentoCompraRequestDTO d
  ) {
    RecebimentoCompra e = repository
      .findById(id)
      .orElseThrow(() ->
        new ResourceNotFoundException("Recebimento não encontrado")
      );
    if (e.getStatus() != StatusDocumento.RASCUNHO) throw new BusinessException(
      "Somente recebimentos em rascunho podem ser alterados"
    );
    aplicar(e, d);
    repository.save(e);
    salvarItens(e, d.itens());
    if (d.status() == StatusDocumento.CONCLUIDO) {
      e.setStatus(StatusDocumento.CONCLUIDO);
      repository.save(e);
    }
    return RecebimentoCompraResponseDTO.fromEntity(e);
  }

  @Transactional
  public void excluir(UUID id) {
    RecebimentoCompra e = repository
      .findById(id)
      .orElseThrow(() ->
        new ResourceNotFoundException("Recebimento não encontrado")
      );
    if (e.getStatus() != StatusDocumento.RASCUNHO) throw new BusinessException(
      "Somente recebimentos em rascunho podem ser excluídos"
    );
    repository.delete(e);
  }

  private void aplicar(RecebimentoCompra e, RecebimentoCompraRequestDTO d) {
    e.setCompra(
      compraRepository
        .findById(d.idCompra())
        .orElseThrow(() ->
          new ResourceNotFoundException("Compra não encontrada")
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
    e.setDataRecebimento(
      d.dataRecebimento() == null ? OffsetDateTime.now() : d.dataRecebimento()
    );
    e.setObservacao(d.observacao());
    if (e.getCriadoEm() == null) e.setCriadoEm(OffsetDateTime.now());
  }

  private void salvarItens(
    RecebimentoCompra e,
    List<ItemRecebimentoRequestDTO> itens
  ) {
    for (ItemRecebimentoRequestDTO d : itens) {
      ItemRecebimentoCompra i = new ItemRecebimentoCompra();
      i.setRecebimentoCompra(e);
      i.setItemCompra(
        itemCompraRepository
          .findById(d.idItemCompra())
          .orElseThrow(() ->
            new ResourceNotFoundException("Item de compra não encontrado")
          )
      );
      i.setProduto(
        produtoRepository
          .findById(d.idProduto())
          .orElseThrow(() ->
            new ResourceNotFoundException("Produto não encontrado")
          )
      );
      i.setQuantidade(d.quantidade());
      i.setObservacao(d.observacao());
      itemRepository.save(i);
    }
  }
}
