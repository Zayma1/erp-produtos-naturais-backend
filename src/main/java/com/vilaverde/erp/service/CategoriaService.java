package com.vilaverde.erp.service;

import com.vilaverde.erp.entity.Categoria;
import com.vilaverde.erp.exception.ResourceNotFoundException;
import com.vilaverde.erp.repository.CategoriaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CategoriaService {

    private final CategoriaRepository repository;

    public List<Categoria> listarTodas() {
        return repository.findAll();
    }

    public Categoria buscarPorId(UUID id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Categoria não encontrada: " + id));
    }

    public Categoria criar(Categoria categoria) {
        categoria.setId(null);
        return repository.save(categoria);
    }

    public Categoria atualizar(UUID id, Categoria dados) {
        Categoria existente = buscarPorId(id);
        existente.setNome(dados.getNome());
        return repository.save(existente);
    }

    public void excluir(UUID id) {
        buscarPorId(id);
        repository.deleteById(id);
    }
}
