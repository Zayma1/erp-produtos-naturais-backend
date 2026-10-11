package com.vilaverde.erp.service;

import com.vilaverde.erp.dto.estoque.EstoqueResponseDTO;
import com.vilaverde.erp.dto.estoque.MovimentacaoRequestDTO;
import com.vilaverde.erp.dto.estoque.SaldoEstoqueResponseDTO;
import com.vilaverde.erp.entity.EstoqueProduto;
import com.vilaverde.erp.entity.Fornecedor;
import com.vilaverde.erp.entity.Loja;
import com.vilaverde.erp.entity.Produto;
import com.vilaverde.erp.entity.Transacao;
import com.vilaverde.erp.exception.BusinessException;
import com.vilaverde.erp.exception.ResourceNotFoundException;
import com.vilaverde.erp.repository.EstoqueProdutoRepository;
import com.vilaverde.erp.repository.FornecedorRepository;
import com.vilaverde.erp.repository.LojaRepository;
import com.vilaverde.erp.repository.ProdutoRepository;
import com.vilaverde.erp.repository.TransacaoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class EstoqueService {

    private final EstoqueProdutoRepository estoqueProdutoRepository;
    private final ProdutoRepository produtoRepository;
    private final LojaRepository lojaRepository;
    private final FornecedorRepository fornecedorRepository;
    private final TransacaoRepository transacaoRepository;

    @Transactional(readOnly = true)
    public List<EstoqueResponseDTO> listar(UUID idProduto, UUID idLoja) {
        List<EstoqueProduto> movimentacoes;
        if (idProduto != null && idLoja != null) {
            movimentacoes = estoqueProdutoRepository.findByProdutoIdAndLojaId(idProduto, idLoja);
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
        return EstoqueResponseDTO.fromEntity(estoqueProdutoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Registro de estoque não encontrado")));
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

    @Transactional
    public EstoqueResponseDTO movimentar(MovimentacaoRequestDTO dto) {
        Produto produto = buscarProduto(dto.idProduto());
        Loja loja = buscarLoja(dto.idLoja());
        Fornecedor fornecedor = fornecedorRepository.findById(dto.idFornecedor())
                .orElseThrow(() -> new ResourceNotFoundException("Fornecedor não encontrado"));
        Transacao transacao = transacaoRepository.findById(dto.idTransacao())
                .orElseThrow(() -> new ResourceNotFoundException("Transação não encontrada"));

        EstoqueProduto estoque = estoqueProdutoRepository
                .findFirstByProdutoIdAndLojaId(dto.idProduto(), dto.idLoja())
                .orElseGet(EstoqueProduto::new);

        BigDecimal anterior = estoque.getQuantidadeAtual() != null ? estoque.getQuantidadeAtual() : BigDecimal.ZERO;
        BigDecimal atual = switch (transacao.getTipo()) {
            case ENTRADA -> anterior.add(dto.quantidade());
            case SAIDA -> {
                if (anterior.compareTo(dto.quantidade()) < 0) {
                    throw new BusinessException("Saldo insuficiente para a saída");
                }
                yield anterior.subtract(dto.quantidade());
            }
            case AJUSTE -> dto.quantidade();
        };

        estoque.setProduto(produto);
        estoque.setLoja(loja);
        estoque.setFornecedor(fornecedor);
        estoque.setTransacao(transacao);
        estoque.setQuantidadeAnterior(anterior);
        estoque.setQuantidadeRecebida(dto.quantidade().setScale(0, RoundingMode.DOWN).longValue());
        estoque.setQuantidadeAtual(atual);

        return EstoqueResponseDTO.fromEntity(estoqueProdutoRepository.save(estoque));
    }

    private Produto buscarProduto(UUID id) {
        return produtoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Produto não encontrado"));
    }

    private Loja buscarLoja(UUID id) {
        return lojaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Loja não encontrada"));
    }
}
