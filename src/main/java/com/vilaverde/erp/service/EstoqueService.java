package com.vilaverde.erp.service;

import com.vilaverde.erp.dto.estoque.EstoqueResponseDTO;
import com.vilaverde.erp.dto.estoque.SaldoEstoqueResponseDTO;
import com.vilaverde.erp.entity.EstoqueProduto;
import com.vilaverde.erp.entity.Loja;
import com.vilaverde.erp.entity.Produto;
import com.vilaverde.erp.exception.ResourceNotFoundException;
import com.vilaverde.erp.repository.EstoqueProdutoRepository;
import com.vilaverde.erp.repository.LojaRepository;
import com.vilaverde.erp.repository.ProdutoRepository;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class EstoqueService {

  private final EstoqueProdutoRepository estoqueProdutoRepository;
  private final ProdutoRepository produtoRepository;
  private final LojaRepository lojaRepository;

  @Transactional(readOnly = true)
  public List<EstoqueResponseDTO> listar(UUID idProduto, UUID idLoja) {
    List<EstoqueProduto> movimentacoes;
    if (idProduto != null && idLoja != null) {
      movimentacoes = estoqueProdutoRepository.findByProdutoIdAndLojaId(
        idProduto,
        idLoja
      );
    } else if (idProduto != null) {
      movimentacoes = estoqueProdutoRepository.findByProdutoId(idProduto);
    } else if (idLoja != null) {
      movimentacoes = estoqueProdutoRepository.findByLojaId(idLoja);
    } else {
      movimentacoes = estoqueProdutoRepository.findAll();
    }
    return movimentacoes.stream().map(EstoqueResponseDTO::fromEntity).toList();
  }

  @Transactional(readOnly = true)
  public EstoqueResponseDTO buscarPorId(UUID id) {
    return EstoqueResponseDTO.fromEntity(
      estoqueProdutoRepository
        .findById(id)
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
      .findFirstByProdutoIdAndLojaId(idProduto, idLoja)
      .map(EstoqueProduto::getQuantidadeAtual)
      .orElse(BigDecimal.ZERO);
    return new SaldoEstoqueResponseDTO(idProduto, idLoja, quantidade);
  }

  private Produto buscarProduto(UUID id) {
    return produtoRepository
      .findById(id)
      .orElseThrow(() ->
        new ResourceNotFoundException("Produto não encontrado")
      );
  }

  private Loja buscarLoja(UUID id) {
    return lojaRepository
      .findById(id)
      .orElseThrow(() -> new ResourceNotFoundException("Loja não encontrada"));
  }
}
