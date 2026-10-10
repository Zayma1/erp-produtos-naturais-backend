package com.vilaverde.erp.service;

import com.vilaverde.erp.dto.fornecedor.FornecedorRequestDTO;
import com.vilaverde.erp.dto.fornecedor.FornecedorResponseDTO;
import com.vilaverde.erp.entity.Fornecedor;
import com.vilaverde.erp.exception.ResourceNotFoundException;
import com.vilaverde.erp.repository.FornecedorRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class FornecedorService {

    private final FornecedorRepository fornecedorRepository;

    @Transactional(readOnly = true)
    public List<FornecedorResponseDTO> listar() {
        return fornecedorRepository.findAll().stream().map(FornecedorResponseDTO::fromEntity).toList();
    }

    @Transactional(readOnly = true)
    public FornecedorResponseDTO buscarPorId(UUID id) {
        return FornecedorResponseDTO.fromEntity(buscar(id));
    }

    @Transactional
    public FornecedorResponseDTO criar(FornecedorRequestDTO dto) {
        Fornecedor fornecedor = new Fornecedor();
        aplicar(fornecedor, dto);
        return FornecedorResponseDTO.fromEntity(fornecedorRepository.save(fornecedor));
    }

    @Transactional
    public FornecedorResponseDTO atualizar(UUID id, FornecedorRequestDTO dto) {
        Fornecedor fornecedor = buscar(id);
        aplicar(fornecedor, dto);
        return FornecedorResponseDTO.fromEntity(fornecedorRepository.save(fornecedor));
    }

    @Transactional
    public void excluir(UUID id) {
        Fornecedor fornecedor = buscar(id);
        fornecedorRepository.delete(fornecedor);
    }

    private void aplicar(Fornecedor fornecedor, FornecedorRequestDTO dto) {
        fornecedor.setNomeFantasia(dto.nomeFantasia());
        fornecedor.setRazaoSocial(dto.razaoSocial());
        fornecedor.setEstado(dto.estado());
        fornecedor.setCidade(dto.cidade());
        fornecedor.setBairro(dto.bairro());
        fornecedor.setComplemento(dto.complemento());
        fornecedor.setCnpj(dto.cnpj());
        fornecedor.setCpf(dto.cpf());
    }

    private Fornecedor buscar(UUID id) {
        return fornecedorRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Fornecedor não encontrado"));
    }
}
