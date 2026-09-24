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

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ProdutoService {

    private final ProdutoRepository produtoRepository;
    private final CategoriaRepository categoriaRepository;
    private final UnidadeMedidaRepository unidadeMedidaRepository;

    public List<ProdutoResponseDTO> listarTodos() {
        return produtoRepository.findAll().stream()
                .map(ProdutoResponseDTO::fromEntity)
                .toList();
    }

    public ProdutoResponseDTO buscarPorId(UUID id) {
        return ProdutoResponseDTO.fromEntity(buscarEntidade(id));
    }

    public Produto buscarEntidade(UUID id) {
        return produtoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Produto não encontrado: " + id));
    }

    public ProdutoResponseDTO criar(ProdutoRequestDTO dto) {
        Produto produto = new Produto();
        aplicarDados(produto, dto);
        return ProdutoResponseDTO.fromEntity(produtoRepository.save(produto));
    }

    public ProdutoResponseDTO atualizar(UUID id, ProdutoRequestDTO dto) {
        Produto existente = buscarEntidade(id);
        aplicarDados(existente, dto);
        return ProdutoResponseDTO.fromEntity(produtoRepository.save(existente));
    }

    public void excluir(UUID id) {
        buscarEntidade(id);
        produtoRepository.deleteById(id);
    }

    private void aplicarDados(Produto produto, ProdutoRequestDTO dto) {
        Categoria categoria = categoriaRepository.findById(dto.idCategoria())
                .orElseThrow(() -> new ResourceNotFoundException("Categoria não encontrada: " + dto.idCategoria()));
        UnidadeMedida unidadeMedida = unidadeMedidaRepository.findById(dto.idUnidadeMedida())
                .orElseThrow(() -> new ResourceNotFoundException("Unidade de medida não encontrada: " + dto.idUnidadeMedida()));

        produto.setNome(dto.nome());
        produto.setCategoria(categoria);
        produto.setUnidadeMedida(unidadeMedida);
        produto.setStatus(dto.status() != null ? dto.status() : StatusProduto.ATIVO);
    }
}
