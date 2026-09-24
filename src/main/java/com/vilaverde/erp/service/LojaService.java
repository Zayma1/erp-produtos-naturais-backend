package com.vilaverde.erp.service;

import com.vilaverde.erp.entity.Loja;
import com.vilaverde.erp.exception.ResourceNotFoundException;
import com.vilaverde.erp.repository.LojaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class LojaService {

    private final LojaRepository repository;

    public List<Loja> listarTodas() {
        return repository.findAll();
    }

    public Loja buscarPorId(UUID id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Loja não encontrada: " + id));
    }

    public Loja criar(Loja loja) {
        loja.setId(null);
        return repository.save(loja);
    }

    public Loja atualizar(UUID id, Loja dados) {
        Loja existente = buscarPorId(id);
        existente.setNomeFantasia(dados.getNomeFantasia());
        existente.setRazaoSocial(dados.getRazaoSocial());
        existente.setEstado(dados.getEstado());
        existente.setCidade(dados.getCidade());
        existente.setBairro(dados.getBairro());
        existente.setNumero(dados.getNumero());
        existente.setComplemento(dados.getComplemento());
        existente.setCnpj(dados.getCnpj());
        return repository.save(existente);
    }

    public void excluir(UUID id) {
        buscarPorId(id);
        repository.deleteById(id);
    }
}
