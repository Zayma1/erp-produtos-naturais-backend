package com.vilaverde.erp.service;

import com.vilaverde.erp.dto.produtosku.ProdutoSkuRequestDTO;
import com.vilaverde.erp.dto.produtosku.ProdutoSkuResponseDTO;
import com.vilaverde.erp.entity.Loja;
import com.vilaverde.erp.entity.Produto;
import com.vilaverde.erp.entity.ProdutoSku;
import com.vilaverde.erp.entity.enums.StatusProduto;
import com.vilaverde.erp.exception.BusinessException;
import com.vilaverde.erp.exception.ResourceNotFoundException;
import com.vilaverde.erp.repository.LojaRepository;
import com.vilaverde.erp.repository.ProdutoRepository;
import com.vilaverde.erp.repository.ProdutoSkuRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ProdutoSkuService {

    private final ProdutoSkuRepository produtoSkuRepository;
    private final ProdutoRepository produtoRepository;
    private final LojaRepository lojaRepository;

    @Transactional(readOnly = true)
    public List<ProdutoSkuResponseDTO> listar(UUID idProduto, UUID idLoja) {
        List<ProdutoSku> skus;
        if (idProduto != null) {
            skus = produtoSkuRepository.findByProdutoId(idProduto);
        } else if (idLoja != null) {
            skus = produtoSkuRepository.findByLojaId(idLoja);
        } else {
            skus = produtoSkuRepository.findAll();
        }
        return skus.stream().map(ProdutoSkuResponseDTO::fromEntity).toList();
    }

    @Transactional(readOnly = true)
    public ProdutoSkuResponseDTO buscarPorId(UUID id) {
        return ProdutoSkuResponseDTO.fromEntity(buscar(id));
    }

    @Transactional
    public ProdutoSkuResponseDTO criar(ProdutoSkuRequestDTO dto) {
        if (produtoSkuRepository.existsBySku(dto.sku())) {
            throw new BusinessException("SKU já cadastrado");
        }
        ProdutoSku sku = new ProdutoSku();
        aplicar(sku, dto);
        return ProdutoSkuResponseDTO.fromEntity(produtoSkuRepository.save(sku));
    }

    @Transactional
    public ProdutoSkuResponseDTO atualizar(UUID id, ProdutoSkuRequestDTO dto) {
        if (produtoSkuRepository.existsBySkuAndIdNot(dto.sku(), id)) {
            throw new BusinessException("SKU já cadastrado");
        }
        ProdutoSku sku = buscar(id);
        aplicar(sku, dto);
        return ProdutoSkuResponseDTO.fromEntity(produtoSkuRepository.save(sku));
    }

    @Transactional
    public void excluir(UUID id) {
        ProdutoSku sku = buscar(id);
        produtoSkuRepository.delete(sku);
    }

    private void aplicar(ProdutoSku sku, ProdutoSkuRequestDTO dto) {
        sku.setProduto(buscarProduto(dto.idProduto()));
        sku.setLoja(dto.idLoja() != null ? buscarLoja(dto.idLoja()) : null);
        sku.setPreco(dto.preco());
        sku.setStatus(dto.status() != null ? dto.status() : StatusProduto.ATIVO);
        sku.setSku(dto.sku());
        sku.setEan13(dto.ean13());
        sku.setDun14(dto.dun14());
    }

    private ProdutoSku buscar(UUID id) {
        return produtoSkuRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("SKU não encontrado"));
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
