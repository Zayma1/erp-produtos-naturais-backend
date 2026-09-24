package com.vilaverde.erp.service;

import com.vilaverde.erp.entity.UnidadeMedida;
import com.vilaverde.erp.exception.ResourceNotFoundException;
import com.vilaverde.erp.repository.UnidadeMedidaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UnidadeMedidaService {

    private final UnidadeMedidaRepository repository;

    public List<UnidadeMedida> listarTodas() {
        return repository.findAll();
    }

    public UnidadeMedida buscarPorId(UUID id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Unidade de medida não encontrada: " + id));
    }

    public UnidadeMedida criar(UnidadeMedida unidade) {
        unidade.setId(null);
        return repository.save(unidade);
    }

    public UnidadeMedida atualizar(UUID id, UnidadeMedida dados) {
        UnidadeMedida existente = buscarPorId(id);
        existente.setSigla(dados.getSigla());
        existente.setNome(dados.getNome());
        return repository.save(existente);
    }

    public void excluir(UUID id) {
        buscarPorId(id);
        repository.deleteById(id);
    }
}
