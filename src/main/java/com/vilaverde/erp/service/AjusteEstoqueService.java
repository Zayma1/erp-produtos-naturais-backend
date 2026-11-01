package com.vilaverde.erp.service;

import com.vilaverde.erp.dto.ajuste.*;
import com.vilaverde.erp.entity.*;
import com.vilaverde.erp.entity.enums.*;
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
public class AjusteEstoqueService {

  private final AjusteEstoqueRepository repository;
  private final ItemAjusteEstoqueRepository itemRepository;
  private final LojaRepository lojaRepository;
  private final UsuarioRepository usuarioRepository;
  private final ProdutoRepository produtoRepository;

  @PersistenceContext
  private EntityManager entityManager;

  @Transactional(readOnly = true)
  public List<AjusteResponseDTO> listar() {
    return repository
      .findAll()
      .stream()
      .map(AjusteResponseDTO::fromEntity)
      .toList();
  }

  @Transactional(readOnly = true)
  public AjusteResponseDTO buscar(UUID id) {
    return AjusteResponseDTO.fromEntity(
      repository
        .findById(id)
        .orElseThrow(() ->
          new ResourceNotFoundException("Ajuste não encontrado")
        )
    );
  }

  @Transactional
  public AjusteResponseDTO criar(AjusteRequestDTO d) {
    AjusteEstoque e = new AjusteEstoque();
    e.setLoja(
      lojaRepository
        .findById(d.idLoja())
        .orElseThrow(() -> new ResourceNotFoundException("Loja não encontrada"))
    );
    e.setUsuario(
      usuarioRepository
        .findById(d.idUsuario())
        .orElseThrow(() ->
          new ResourceNotFoundException("Usuário não encontrado")
        )
    );
    e.setJustificativa(d.justificativa());
    e.setStatus(StatusDocumento.RASCUNHO);
    e.setCriadoEm(OffsetDateTime.now());
    e = repository.save(e);
    for (ItemAjusteRequestDTO x : d.itens()) {
      ItemAjusteEstoque i = new ItemAjusteEstoque();
      i.setAjusteEstoque(e);
      i.setProduto(
        produtoRepository
          .findById(x.idProduto())
          .orElseThrow(() ->
            new ResourceNotFoundException("Produto não encontrado")
          )
      );
      i.setTipo(x.tipo());
      i.setQuantidade(x.quantidade());
      i.setObservacao(x.observacao());
      itemRepository.save(i);
    }
    if (d.status() == StatusDocumento.CONCLUIDO) concluir(e.getId());
    return buscar(e.getId());
  }

  @Transactional
  public AjusteResponseDTO concluir(UUID id) {
    repository
      .findById(id)
      .orElseThrow(() ->
        new ResourceNotFoundException("Ajuste não encontrado")
      );
    entityManager
      .createNativeQuery("select public.fn_concluir_ajuste_estoque(:id)")
      .setParameter("id", id)
      .getSingleResult();
    return buscar(id);
  }
}
