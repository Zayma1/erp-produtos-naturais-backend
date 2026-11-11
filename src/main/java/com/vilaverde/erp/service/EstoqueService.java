package com.vilaverde.erp.service;

import com.vilaverde.erp.dto.estoque.EstoqueResponseDTO;
import com.vilaverde.erp.dto.estoque.SaldoEstoqueResponseDTO;
import com.vilaverde.erp.entity.EstoqueProduto;
import com.vilaverde.erp.entity.Loja;
import com.vilaverde.erp.entity.ProdutoSku;
import com.vilaverde.erp.exception.ResourceNotFoundException;
import com.vilaverde.erp.repository.EstoqueProdutoRepository;
import com.vilaverde.erp.repository.LojaRepository;
import com.vilaverde.erp.repository.ProdutoSkuRepository;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class EstoqueService {
  private final com.vilaverde.erp.security.AccessScope scope;

  private final EstoqueProdutoRepository estoqueProdutoRepository;
  private final ProdutoSkuRepository produtoRepository;
  private final LojaRepository lojaRepository;

  @Transactional(readOnly = true)
  public List<EstoqueResponseDTO> listar(UUID idProduto, UUID idProdutoSku, UUID idLoja) {
    return scope.rows(estoqueProdutoRepository,"loja.id").stream().filter(e -> (idProduto==null || e.getProdutoSku().getProduto().getId().equals(idProduto)) && (idProdutoSku==null || e.getProdutoSku().getId().equals(idProdutoSku)) && (idLoja==null || e.getLoja().getId().equals(idLoja))).map(EstoqueResponseDTO::fromEntity).toList();
  }

  @Transactional(readOnly = true)
  public EstoqueResponseDTO buscarPorId(UUID id) {
    return EstoqueResponseDTO.fromEntity(
      estoqueProdutoRepository
        .findById(id)
      .filter(e -> scope.permits(e.getLoja().getId()))
        .orElseThrow(() ->
          new ResourceNotFoundException("Registro de estoque não encontrado")
        )
    );
  }

  @Transactional(readOnly = true)
  public SaldoEstoqueResponseDTO saldo(UUID idProduto, UUID idLoja) {
    buscarProduto(idProduto);
    buscarLoja(idLoja);
    BigDecimal quantidade = estoqueProdutoRepository
      .findFirstByProdutoSkuIdAndLojaId(idProduto, idLoja)
      .map(EstoqueProduto::getQuantidadeAtual)
      .orElse(BigDecimal.ZERO);
    return new SaldoEstoqueResponseDTO(buscarProduto(idProduto).getProduto().getId(), idProduto, idLoja, quantidade);
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
