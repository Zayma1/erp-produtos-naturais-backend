package com.vilaverde.erp.service;

import com.vilaverde.erp.dto.categoria.CategoriaRequestDTO;
import com.vilaverde.erp.dto.categoria.CategoriaResponseDTO;
import com.vilaverde.erp.entity.Categoria;
import com.vilaverde.erp.exception.BusinessException;
import com.vilaverde.erp.exception.ResourceNotFoundException;
import com.vilaverde.erp.repository.CategoriaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CategoriaService {

    private final CategoriaRepository categoriaRepository;

    @Transactional(readOnly = true)
    public List<CategoriaResponseDTO> listar() {
        return categoriaRepository.findAll().stream().map(CategoriaResponseDTO::fromEntity).toList();
    }

    @Transactional(readOnly = true)
    public CategoriaResponseDTO buscarPorId(UUID id) {
        return CategoriaResponseDTO.fromEntity(buscar(id));
    }

    @Transactional
    public CategoriaResponseDTO criar(CategoriaRequestDTO dto) {
        if (categoriaRepository.existsByNome(dto.nome())) {
            throw new BusinessException("Já existe uma categoria com este nome");
        }
        Categoria categoria = new Categoria();
        categoria.setNome(dto.nome());
        return CategoriaResponseDTO.fromEntity(categoriaRepository.save(categoria));
    }

    @Transactional
    public CategoriaResponseDTO atualizar(UUID id, CategoriaRequestDTO dto) {
        if (categoriaRepository.existsByNomeAndIdNot(dto.nome(), id)) {
            throw new BusinessException("Já existe uma categoria com este nome");
        }
        Categoria categoria = buscar(id);
        categoria.setNome(dto.nome());
        return CategoriaResponseDTO.fromEntity(categoriaRepository.save(categoria));
    }

    @Transactional
    public void excluir(UUID id) {
        Categoria categoria = buscar(id);
        categoriaRepository.delete(categoria);
    }

    private Categoria buscar(UUID id) {
        return categoriaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Categoria não encontrada"));
    }
}
