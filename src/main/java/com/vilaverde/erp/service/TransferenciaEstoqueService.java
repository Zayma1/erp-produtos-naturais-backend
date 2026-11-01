package com.vilaverde.erp.service;

import com.vilaverde.erp.dto.transferencia.*;
import com.vilaverde.erp.entity.*;
import com.vilaverde.erp.entity.enums.StatusDocumento;
import com.vilaverde.erp.exception.*;
import com.vilaverde.erp.repository.*;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.time.OffsetDateTime;
import java.util.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class TransferenciaEstoqueService {

  private final TransferenciaEstoqueRepository repository;
  private final ItemTransferenciaRepository itemRepository;
  private final LojaRepository lojaRepository;
  private final UsuarioRepository usuarioRepository;
  private final ProdutoRepository produtoRepository;

  @PersistenceContext
  private EntityManager entityManager;

  @Transactional(readOnly = true)
  public List<TransferenciaResponseDTO> listar() {
    return repository
      .findAll()
      .stream()
      .map(TransferenciaResponseDTO::fromEntity)
      .toList();
  }

  @Transactional(readOnly = true)
  public TransferenciaResponseDTO buscar(UUID id) {
    return TransferenciaResponseDTO.fromEntity(
      repository
        .findById(id)
        .orElseThrow(() ->
          new ResourceNotFoundException("Transferência não encontrada")
        )
    );
  }

  @Transactional
  public TransferenciaResponseDTO criar(TransferenciaRequestDTO d) {
    if (d.idLojaOrigem().equals(d.idLojaDestino())) throw new BusinessException(
      "As lojas de origem e destino devem ser diferentes"
    );
    TransferenciaEstoque e = new TransferenciaEstoque();
    e.setLojaOrigem(
      lojaRepository
        .findById(d.idLojaOrigem())
        .orElseThrow(() ->
          new ResourceNotFoundException("Loja de origem não encontrada")
        )
    );
    e.setLojaDestino(
      lojaRepository
        .findById(d.idLojaDestino())
        .orElseThrow(() ->
          new ResourceNotFoundException("Loja de destino não encontrada")
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
    e.setDataTransferencia(
      d.dataTransferencia() == null
        ? OffsetDateTime.now()
        : d.dataTransferencia()
    );
    e.setObservacao(d.observacao());
    e.setCriadoEm(OffsetDateTime.now());
    e = repository.save(e);
    for (ItemTransferenciaRequestDTO x : d.itens()) {
      ItemTransferencia i = new ItemTransferencia();
      i.setTransferencia(e);
      i.setProduto(
        produtoRepository
          .findById(x.idProduto())
          .orElseThrow(() ->
            new ResourceNotFoundException("Produto não encontrado")
          )
      );
      i.setQuantidade(x.quantidade());
      itemRepository.save(i);
    }
    if (d.status() == StatusDocumento.CONCLUIDA) concluir(
      e.getId(),
      d.idUsuario()
    );
    return buscar(e.getId());
  }

  @Transactional
  public TransferenciaResponseDTO concluir(UUID id, UUID idUsuario) {
    repository
      .findById(id)
      .orElseThrow(() ->
        new ResourceNotFoundException("Transferência não encontrada")
      );
    var query =
      idUsuario == null
        ? entityManager
            .createNativeQuery(
              "select public.fn_concluir_transferencia(cast(:id as uuid), null)"
            )
            .setParameter("id", id)
        : entityManager
            .createNativeQuery(
              "select public.fn_concluir_transferencia(cast(:id as uuid), cast(:usuario as uuid))"
            )
            .setParameter("id", id)
            .setParameter("usuario", idUsuario);
    query.getSingleResult();
    return buscar(id);
  }
}
