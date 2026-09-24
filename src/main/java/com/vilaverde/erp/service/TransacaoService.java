package com.vilaverde.erp.service;

import com.vilaverde.erp.entity.Transacao;
import com.vilaverde.erp.exception.ResourceNotFoundException;
import com.vilaverde.erp.repository.TransacaoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TransacaoService {

    private final TransacaoRepository repository;

    public List<Transacao> listarTodas() {
        return repository.findAll();
    }

    public Transacao buscarPorId(UUID id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Transação não encontrada: " + id));
    }

    public Transacao criar(Transacao transacao) {
        transacao.setId(null);
        return repository.save(transacao);
    }

    public Transacao atualizar(UUID id, Transacao dados) {
        Transacao existente = buscarPorId(id);
        existente.setSigla(dados.getSigla());
        existente.setNome(dados.getNome());
        existente.setTipo(dados.getTipo());
        return repository.save(existente);
    }

    public void excluir(UUID id) {
        buscarPorId(id);
        repository.deleteById(id);
    }
}
