package com.vilaverde.erp.service;

import com.vilaverde.erp.dto.produtosku.ProdutoSkuRequestDTO;
import com.vilaverde.erp.dto.produtosku.ProdutoSkuResponseDTO;
import com.vilaverde.erp.entity.Loja;
import com.vilaverde.erp.entity.Produto;
import com.vilaverde.erp.entity.ProdutoSku;
import com.vilaverde.erp.entity.enums.StatusProduto;
import com.vilaverde.erp.exception.ResourceNotFoundException;
import com.vilaverde.erp.repository.LojaRepository;
import com.vilaverde.erp.repository.ProdutoRepository;
import com.vilaverde.erp.repository.ProdutoSkuRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ProdutoSkuService {

    private final ProdutoSkuRepository skuRepository;
    private final ProdutoRepository produtoRepository;
    private final LojaRepository lojaRepository;

    public List<ProdutoSkuResponseDTO> listarTodos() {
        return skuRepository.findAll().stream().map(ProdutoSkuResponseDTO::fromEntity).toList();
    }

    public List<ProdutoSkuResponseDTO> listarPorProduto(UUID idProduto) {
        return skuRepository.findByProdutoId(idProduto).stream().map(ProdutoSkuResponseDTO::fromEntity).toList();
    }

    public ProdutoSkuResponseDTO buscarPorId(UUID id) {
        return ProdutoSkuResponseDTO.fromEntity(buscarEntidade(id));
    }

    public ProdutoSku buscarEntidade(UUID id) {
        return skuRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("SKU não encontrado: " + id));
    }

    public ProdutoSkuResponseDTO criar(ProdutoSkuRequestDTO dto) {
        ProdutoSku sku = new ProdutoSku();
        aplicarDados(sku, dto);
        return ProdutoSkuResponseDTO.fromEntity(skuRepository.save(sku));
    }

    public ProdutoSkuResponseDTO atualizar(UUID id, ProdutoSkuRequestDTO dto) {
        ProdutoSku existente = buscarEntidade(id);
        aplicarDados(existente, dto);
        return ProdutoSkuResponseDTO.fromEntity(skuRepository.save(existente));
    }

    public void excluir(UUID id) {
        buscarEntidade(id);
        skuRepository.deleteById(id);
    }

    private void aplicarDados(ProdutoSku sku, ProdutoSkuRequestDTO dto) {
        Produto produto = produtoRepository.findById(dto.idProduto())
                .orElseThrow(() -> new ResourceNotFoundException("Produto não encontrado: " + dto.idProduto()));
        Loja loja = lojaRepository.findById(dto.idLoja())
                .orElseThrow(() -> new ResourceNotFoundException("Loja não encontrada: " + dto.idLoja()));

        sku.setProduto(produto);
        sku.setLoja(loja);
        sku.setPreco(dto.preco());
        sku.setStatus(dto.status() != null ? dto.status() : StatusProduto.ATIVO);
        sku.setSku(dto.sku());
        sku.setEan13(dto.ean13());
        sku.setDun14(dto.dun14());
    }
}
