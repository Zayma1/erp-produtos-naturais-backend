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
public class DevolucaoFornecedorService {

  private final DevolucaoFornecedorRepository repository;
  private final ItemDevolucaoFornecedorRepository itemRepository;
  private final CompraRepository compraRepository;
  private final UsuarioRepository usuarioRepository;
  private final ItemCompraRepository itemCompraRepository;
  private final ProdutoRepository produtoRepository;

  @Transactional(readOnly = true)
  public List<DevolucaoFornecedorResponseDTO> listar() {
    return repository
      .findAll()
      .stream()
      .map(DevolucaoFornecedorResponseDTO::fromEntity)
      .toList();
  }

  @Transactional(readOnly = true)
  public DevolucaoFornecedorResponseDTO buscar(UUID id) {
    return DevolucaoFornecedorResponseDTO.fromEntity(
      repository
        .findById(id)
        .orElseThrow(() ->
          new ResourceNotFoundException(
            "Devolução de fornecedor não encontrada"
          )
        )
    );
  }

  @Transactional
  public DevolucaoFornecedorResponseDTO criar(DevolucaoFornecedorRequestDTO d) {
    DevolucaoFornecedor e = new DevolucaoFornecedor();
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
    e.setStatus(StatusDocumento.RASCUNHO);
    e.setDataDevolucao(
      d.dataDevolucao() == null ? OffsetDateTime.now() : d.dataDevolucao()
    );
    e.setMotivo(d.motivo());
    e.setObservacao(d.observacao());
    e.setCriadoEm(OffsetDateTime.now());
    e = repository.save(e);
    for (ItemDevolucaoFornecedorRequestDTO x : d.itens()) {
      ItemDevolucaoFornecedor i = new ItemDevolucaoFornecedor();
      i.setDevolucaoFornecedor(e);
      i.setItemCompra(
        itemCompraRepository
          .findById(x.idItemCompra())
          .orElseThrow(() ->
            new ResourceNotFoundException("Item de compra não encontrado")
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
    DevolucaoFornecedor e = repository
      .findById(id)
      .orElseThrow(() ->
        new ResourceNotFoundException("Devolução de fornecedor não encontrada")
      );
    if (e.getStatus() != StatusDocumento.RASCUNHO) throw new BusinessException(
      "Somente devoluções em rascunho podem ser excluídas"
    );
    repository.delete(e);
  }
}
