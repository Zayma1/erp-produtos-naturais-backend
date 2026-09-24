package com.vilaverde.erp.service;

import com.vilaverde.erp.dto.estoque.EstoqueResponseDTO;
import com.vilaverde.erp.dto.estoque.MovimentacaoRequestDTO;
import com.vilaverde.erp.entity.*;
import com.vilaverde.erp.entity.enums.TipoTransacao;
import com.vilaverde.erp.exception.BusinessException;
import com.vilaverde.erp.exception.ResourceNotFoundException;
import com.vilaverde.erp.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class EstoqueService {

    private final EstoqueProdutoRepository estoqueRepository;
    private final ProdutoRepository produtoRepository;
    private final LojaRepository lojaRepository;
    private final FornecedorRepository fornecedorRepository;
    private final TransacaoRepository transacaoRepository;

    public List<EstoqueResponseDTO> listarTodos() {
        return estoqueRepository.findAll().stream().map(EstoqueResponseDTO::fromEntity).toList();
    }

    public List<EstoqueResponseDTO> historicoPorProduto(UUID idProduto) {
        return estoqueRepository.findByProdutoIdOrderByDataMovimentacaoDesc(idProduto)
                .stream().map(EstoqueResponseDTO::fromEntity).toList();
    }

    public BigDecimal saldoAtual(UUID idProduto, UUID idLoja) {
        return estoqueRepository.findFirstByProdutoIdAndLojaIdOrderByDataMovimentacaoDesc(idProduto, idLoja)
                .map(EstoqueProduto::getQuantidadeAtual)
                .orElse(BigDecimal.ZERO);
    }

    @Transactional
    public EstoqueResponseDTO registrarMovimentacao(MovimentacaoRequestDTO dto) {
        Produto produto = produtoRepository.findById(dto.idProduto())
                .orElseThrow(() -> new ResourceNotFoundException("Produto não encontrado: " + dto.idProduto()));
        Loja loja = lojaRepository.findById(dto.idLoja())
                .orElseThrow(() -> new ResourceNotFoundException("Loja não encontrada: " + dto.idLoja()));
        Transacao transacao = transacaoRepository.findById(dto.idTransacao())
                .orElseThrow(() -> new ResourceNotFoundException("Transação não encontrada: " + dto.idTransacao()));

        Fornecedor fornecedor = null;
        if (dto.idFornecedor() != null) {
            fornecedor = fornecedorRepository.findById(dto.idFornecedor())
                    .orElseThrow(() -> new ResourceNotFoundException("Fornecedor não encontrado: " + dto.idFornecedor()));
        }

        BigDecimal quantidadeAnterior = saldoAtual(dto.idProduto(), dto.idLoja());
        BigDecimal quantidadeAtual;

        switch (transacao.getTipo()) {
            case ENTRADA -> quantidadeAtual = quantidadeAnterior.add(dto.quantidade());
            case SAIDA -> {
                quantidadeAtual = quantidadeAnterior.subtract(dto.quantidade());
                if (quantidadeAtual.compareTo(BigDecimal.ZERO) < 0) {
                    throw new BusinessException("Estoque insuficiente. Saldo atual: " + quantidadeAnterior);
                }
            }
            case AJUSTE -> quantidadeAtual = dto.quantidade();
            default -> throw new BusinessException("Tipo de transação inválido");
        }

        if (transacao.getTipo() == TipoTransacao.ENTRADA && fornecedor == null) {
            throw new BusinessException("Fornecedor é obrigatório para movimentações de entrada");
        }

        EstoqueProduto movimento = new EstoqueProduto();
        movimento.setProduto(produto);
        movimento.setLoja(loja);
        movimento.setFornecedor(fornecedor);
        movimento.setTransacao(transacao);
        movimento.setQuantidadeAnterior(quantidadeAnterior);
        movimento.setQuantidadeRecebida(dto.quantidade());
        movimento.setQuantidadeAtual(quantidadeAtual);
        movimento.setMotivo(dto.motivo());
        movimento.setDataMovimentacao(LocalDateTime.now());

        return EstoqueResponseDTO.fromEntity(estoqueRepository.save(movimento));
    }
}
