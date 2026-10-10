package com.vilaverde.erp.service;

import com.vilaverde.erp.dto.produtoloja.ProdutoLojaRequestDTO;
import com.vilaverde.erp.dto.produtoloja.ProdutoLojaResponseDTO;
import com.vilaverde.erp.entity.Loja;
import com.vilaverde.erp.entity.Produto;
import com.vilaverde.erp.entity.ProdutoLoja;
import com.vilaverde.erp.exception.BusinessException;
import com.vilaverde.erp.exception.ResourceNotFoundException;
import com.vilaverde.erp.repository.LojaRepository;
import com.vilaverde.erp.repository.ProdutoLojaRepository;
import com.vilaverde.erp.repository.ProdutoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ProdutoLojaService {

    private final ProdutoLojaRepository produtoLojaRepository;
    private final ProdutoRepository produtoRepository;
    private final LojaRepository lojaRepository;

    @Transactional(readOnly = true)
    public List<ProdutoLojaResponseDTO> listar(UUID idProduto, UUID idLoja) {
        List<ProdutoLoja> vinculos;
        if (idProduto != null) {
            vinculos = produtoLojaRepository.findByProdutoId(idProduto);
        } else if (idLoja != null) {
            vinculos = produtoLojaRepository.findByLojaId(idLoja);
        } else {
            vinculos = produtoLojaRepository.findAll();
        }
        return vinculos.stream().map(ProdutoLojaResponseDTO::fromEntity).toList();
    }

    @Transactional(readOnly = true)
    public ProdutoLojaResponseDTO buscarPorId(UUID id) {
        return ProdutoLojaResponseDTO.fromEntity(buscar(id));
    }

    @Transactional
    public ProdutoLojaResponseDTO criar(ProdutoLojaRequestDTO dto) {
        if (produtoLojaRepository.existsByProdutoIdAndLojaId(dto.idProduto(), dto.idLoja())) {
            throw new BusinessException("Produto já vinculado a esta loja");
        }
        ProdutoLoja vinculo = new ProdutoLoja();
        vinculo.setProduto(buscarProduto(dto.idProduto()));
        vinculo.setLoja(buscarLoja(dto.idLoja()));
        return ProdutoLojaResponseDTO.fromEntity(produtoLojaRepository.save(vinculo));
    }

    @Transactional
    public void excluir(UUID id) {
        ProdutoLoja vinculo = buscar(id);
        produtoLojaRepository.delete(vinculo);
    }

    private ProdutoLoja buscar(UUID id) {
        return produtoLojaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Vínculo produto-loja não encontrado"));
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
