package com.vilaverde.erp.service;

import com.vilaverde.erp.dto.produto.ProdutoRequestDTO;
import com.vilaverde.erp.dto.produto.ProdutoResponseDTO;
import com.vilaverde.erp.entity.Categoria;
import com.vilaverde.erp.entity.Produto;
import com.vilaverde.erp.entity.UnidadeMedida;
import com.vilaverde.erp.entity.enums.StatusProduto;
import com.vilaverde.erp.exception.ResourceNotFoundException;
import com.vilaverde.erp.repository.CategoriaRepository;
import com.vilaverde.erp.repository.ProdutoRepository;
import com.vilaverde.erp.repository.UnidadeMedidaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ProdutoService {

    private final ProdutoRepository produtoRepository;
    private final CategoriaRepository categoriaRepository;
    private final UnidadeMedidaRepository unidadeMedidaRepository;

    @Transactional(readOnly = true)
    public List<ProdutoResponseDTO> listar() {
        return produtoRepository.findAll().stream().map(ProdutoResponseDTO::fromEntity).toList();
    }

    @Transactional(readOnly = true)
    public ProdutoResponseDTO buscarPorId(UUID id) {
        return ProdutoResponseDTO.fromEntity(buscar(id));
    }

    @Transactional
    public ProdutoResponseDTO criar(ProdutoRequestDTO dto) {
        Produto produto = new Produto();
        aplicar(produto, dto);
        return ProdutoResponseDTO.fromEntity(produtoRepository.save(produto));
    }

    @Transactional
    public ProdutoResponseDTO atualizar(UUID id, ProdutoRequestDTO dto) {
        Produto produto = buscar(id);
        aplicar(produto, dto);
        return ProdutoResponseDTO.fromEntity(produtoRepository.save(produto));
    }

    @Transactional
    public void excluir(UUID id) {
        Produto produto = buscar(id);
        produtoRepository.delete(produto);
    }

    private void aplicar(Produto produto, ProdutoRequestDTO dto) {
        produto.setNome(dto.nome());
        produto.setTipo(buscarTipo(dto.idTipo()));
        produto.setUnidadeMedida(buscarUnidade(dto.idUnidadeMedida()));
        produto.setStatus(dto.status() != null ? dto.status() : StatusProduto.ATIVO);
    }

    private Produto buscar(UUID id) {
        return produtoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Produto não encontrado"));
    }

    private Categoria buscarTipo(UUID id) {
        return categoriaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Tipo/categoria não encontrado"));
    }

    private UnidadeMedida buscarUnidade(UUID id) {
        return unidadeMedidaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Unidade de medida não encontrada"));
    }
}
