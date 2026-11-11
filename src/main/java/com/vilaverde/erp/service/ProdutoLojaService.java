package com.vilaverde.erp.service;

import com.vilaverde.erp.dto.produtoloja.ProdutoLojaRequestDTO;
import com.vilaverde.erp.dto.produtoloja.ProdutoLojaResponseDTO;
import com.vilaverde.erp.entity.Loja;
import com.vilaverde.erp.entity.ProdutoSku;
import com.vilaverde.erp.entity.ProdutoLoja;
import com.vilaverde.erp.exception.BusinessException;
import com.vilaverde.erp.exception.ResourceNotFoundException;
import com.vilaverde.erp.repository.LojaRepository;
import com.vilaverde.erp.repository.ProdutoLojaRepository;
import com.vilaverde.erp.repository.ProdutoSkuRepository;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ProdutoLojaService {
  private final com.vilaverde.erp.security.AccessScope scope;

  private final ProdutoLojaRepository produtoLojaRepository;
  private final ProdutoSkuRepository produtoRepository;
  private final LojaRepository lojaRepository;

  @Transactional(readOnly = true)
  public List<ProdutoLojaResponseDTO> listar(UUID idProduto, UUID idProdutoSku, UUID idLoja) {
    return scope.rows(produtoLojaRepository,"loja.id").stream().filter(e -> (idProduto==null || e.getProdutoSku().getProduto().getId().equals(idProduto)) && (idProdutoSku==null || e.getProdutoSku().getId().equals(idProdutoSku)) && (idLoja==null || e.getLoja().getId().equals(idLoja))).map(ProdutoLojaResponseDTO::fromEntity).toList();
  }

  @Transactional(readOnly = true)
  public ProdutoLojaResponseDTO buscarPorId(UUID id) {
    return ProdutoLojaResponseDTO.fromEntity(buscar(id));
  }

  @Transactional
  public ProdutoLojaResponseDTO criar(ProdutoLojaRequestDTO dto) {
    if (
      produtoLojaRepository.existsByProdutoSkuIdAndLojaId(
        dto.idProdutoSku(),
        dto.idLoja()
      )
    ) {
      throw new BusinessException("SKU já vinculado a esta loja");
    }
    ProdutoLoja vinculo = new ProdutoLoja();
    vinculo.setProdutoSku(buscarProduto(dto.idProdutoSku()));
    vinculo.setLoja(buscarLoja(dto.idLoja()));
    return ProdutoLojaResponseDTO.fromEntity(
      produtoLojaRepository.save(vinculo)
    );
  }

  @Transactional
  public void excluir(UUID id) {
    ProdutoLoja vinculo = buscar(id);
    produtoLojaRepository.delete(vinculo);
  }

  private ProdutoLoja buscar(UUID id) {
    return produtoLojaRepository
      .findById(id)
      .filter(e -> scope.permits(e.getLoja().getId()))
      .orElseThrow(() ->
        new ResourceNotFoundException("Vínculo produto-loja não encontrado")
      );
  }

  private ProdutoSku buscarProduto(UUID id) {
    return produtoRepository
      .findById(id)
      .orElseThrow(() ->
        new ResourceNotFoundException("SKU não encontrado")
      );
  }

  private Loja buscarLoja(UUID id) {
    scope.store(id);
    return lojaRepository
      .findById(id)
      .orElseThrow(() -> new ResourceNotFoundException("Loja não encontrada"));
  }
}
