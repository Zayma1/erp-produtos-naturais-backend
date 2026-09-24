package com.vilaverde.erp.service;

import com.vilaverde.erp.entity.Loja;
import com.vilaverde.erp.entity.Produto;
import com.vilaverde.erp.entity.ProdutoLoja;
import com.vilaverde.erp.exception.ResourceNotFoundException;
import com.vilaverde.erp.repository.LojaRepository;
import com.vilaverde.erp.repository.ProdutoLojaRepository;
import com.vilaverde.erp.repository.ProdutoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ProdutoLojaService {

    private final ProdutoLojaRepository repository;
    private final ProdutoRepository produtoRepository;
    private final LojaRepository lojaRepository;

    public List<ProdutoLoja> listarTodos() {
        return repository.findAll();
    }

    public List<ProdutoLoja> listarPorProduto(UUID idProduto) {
        return repository.findByProdutoId(idProduto);
    }

    public ProdutoLoja vincular(UUID idProduto, UUID idLoja) {
        Produto produto = produtoRepository.findById(idProduto)
                .orElseThrow(() -> new ResourceNotFoundException("Produto não encontrado: " + idProduto));
        Loja loja = lojaRepository.findById(idLoja)
                .orElseThrow(() -> new ResourceNotFoundException("Loja não encontrada: " + idLoja));

        ProdutoLoja vinculo = new ProdutoLoja();
        vinculo.setProduto(produto);
        vinculo.setLoja(loja);
        return repository.save(vinculo);
    }

    public void desvincular(UUID id) {
        if (!repository.existsById(id)) {
            throw new ResourceNotFoundException("Vínculo produto/loja não encontrado: " + id);
        }
        repository.deleteById(id);
    }
}
