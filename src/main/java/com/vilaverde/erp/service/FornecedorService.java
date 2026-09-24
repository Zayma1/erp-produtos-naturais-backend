package com.vilaverde.erp.service;

import com.vilaverde.erp.entity.Fornecedor;
import com.vilaverde.erp.exception.ResourceNotFoundException;
import com.vilaverde.erp.repository.FornecedorRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class FornecedorService {

    private final FornecedorRepository repository;

    public List<Fornecedor> listarTodos() {
        return repository.findAll();
    }

    public Fornecedor buscarPorId(UUID id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Fornecedor não encontrado: " + id));
    }

    public Fornecedor criar(Fornecedor fornecedor) {
        fornecedor.setId(null);
        return repository.save(fornecedor);
    }

    public Fornecedor atualizar(UUID id, Fornecedor dados) {
        Fornecedor existente = buscarPorId(id);
        existente.setNomeFantasia(dados.getNomeFantasia());
        existente.setRazaoSocial(dados.getRazaoSocial());
        existente.setEstado(dados.getEstado());
        existente.setCidade(dados.getCidade());
        existente.setBairro(dados.getBairro());
        existente.setComplemento(dados.getComplemento());
        existente.setCnpj(dados.getCnpj());
        existente.setCpf(dados.getCpf());
        return repository.save(existente);
    }

    public void excluir(UUID id) {
        buscarPorId(id);
        repository.deleteById(id);
    }
}
