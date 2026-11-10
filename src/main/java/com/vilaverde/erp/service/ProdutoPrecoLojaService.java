package com.vilaverde.erp.service;

import com.vilaverde.erp.dto.produto.*;
import com.vilaverde.erp.entity.*;
import com.vilaverde.erp.exception.ResourceNotFoundException;
import com.vilaverde.erp.repository.*;
import java.time.OffsetDateTime;
import java.util.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ProdutoPrecoLojaService {
  private final com.vilaverde.erp.security.AccessScope scope;

  private final ProdutoPrecoLojaRepository repository;
  private final ProdutoSkuRepository produtoRepository;
  private final LojaRepository lojaRepository;

  @Transactional(readOnly = true)
  public List<ProdutoPrecoLojaResponseDTO> listar(UUID idProduto, UUID idProdutoSku, UUID idLoja) {
    return scope.rows(repository,"loja.id").stream().filter(e -> (idProduto==null || e.getProdutoSku().getProduto().getId().equals(idProduto)) && (idProdutoSku==null || e.getProdutoSku().getId().equals(idProdutoSku)) && (idLoja==null || e.getLoja().getId().equals(idLoja))).map(ProdutoPrecoLojaResponseDTO::fromEntity).toList();
  }

  @Transactional(readOnly = true)
  public ProdutoPrecoLojaResponseDTO buscar(UUID id) {
    return ProdutoPrecoLojaResponseDTO.fromEntity(
      repository
        .findById(id)
      .filter(candidate -> scope.permits(candidate.getLoja().getId()))
        .orElseThrow(() ->
          new ResourceNotFoundException(
            "Preço do produto na loja não encontrado"
          )
        )
    );
  }

  @Transactional
  public ProdutoPrecoLojaResponseDTO criar(ProdutoPrecoLojaRequestDTO d) {
    ProdutoPrecoLoja e = new ProdutoPrecoLoja();
    aplicar(e, d);
    return ProdutoPrecoLojaResponseDTO.fromEntity(repository.save(e));
  }

  @Transactional
  public ProdutoPrecoLojaResponseDTO atualizar(
    UUID id,
    ProdutoPrecoLojaRequestDTO d
  ) {
    ProdutoPrecoLoja e = repository
      .findById(id)
      .filter(candidate -> scope.permits(candidate.getLoja().getId()))
      .orElseThrow(() ->
        new ResourceNotFoundException("Preço do produto na loja não encontrado")
      );
    aplicar(e, d);
    return ProdutoPrecoLojaResponseDTO.fromEntity(repository.save(e));
  }

  @Transactional
  public void excluir(UUID id) {
    repository.delete(
      repository
        .findById(id)
      .filter(candidate -> scope.permits(candidate.getLoja().getId()))
        .orElseThrow(() ->
          new ResourceNotFoundException(
            "Preço do produto na loja não encontrado"
          )
        )
    );
  }

  private void aplicar(ProdutoPrecoLoja e, ProdutoPrecoLojaRequestDTO d) {
    scope.store(d.idLoja());
    e.setProdutoSku(
      produtoRepository
        .findById(d.idProdutoSku())
        .orElseThrow(() ->
          new ResourceNotFoundException("SKU não encontrado")
        )
    );
    e.setLoja(
      lojaRepository
        .findById(d.idLoja())
        .orElseThrow(() -> new ResourceNotFoundException("Loja não encontrada"))
    );
    e.setPreco(d.preco());
    e.setDataInicio(
      d.dataInicio() == null ? OffsetDateTime.now() : d.dataInicio()
    );
    e.setDataFim(d.dataFim());
    if (e.getCriadoEm() == null) e.setCriadoEm(OffsetDateTime.now());
  }
}
