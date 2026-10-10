package com.vilaverde.erp.service;

import com.vilaverde.erp.dto.loja.LojaRequestDTO;
import com.vilaverde.erp.dto.loja.LojaResponseDTO;
import com.vilaverde.erp.entity.Loja;
import com.vilaverde.erp.exception.ResourceNotFoundException;
import com.vilaverde.erp.repository.LojaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class LojaService {

    private final LojaRepository lojaRepository;

    @Transactional(readOnly = true)
    public List<LojaResponseDTO> listar() {
        return lojaRepository.findAll().stream().map(LojaResponseDTO::fromEntity).toList();
    }

    @Transactional(readOnly = true)
    public LojaResponseDTO buscarPorId(UUID id) {
        return LojaResponseDTO.fromEntity(buscar(id));
    }

    @Transactional
    public LojaResponseDTO criar(LojaRequestDTO dto) {
        Loja loja = new Loja();
        aplicar(loja, dto);
        return LojaResponseDTO.fromEntity(lojaRepository.save(loja));
    }

    @Transactional
    public LojaResponseDTO atualizar(UUID id, LojaRequestDTO dto) {
        Loja loja = buscar(id);
        aplicar(loja, dto);
        return LojaResponseDTO.fromEntity(lojaRepository.save(loja));
    }

    @Transactional
    public void excluir(UUID id) {
        Loja loja = buscar(id);
        lojaRepository.delete(loja);
    }

    private void aplicar(Loja loja, LojaRequestDTO dto) {
        loja.setNomeFantasia(dto.nomeFantasia());
        loja.setRazaoSocial(dto.razaoSocial());
        loja.setEstado(dto.estado());
        loja.setCidade(dto.cidade());
        loja.setBairro(dto.bairro());
        loja.setNumero(dto.numero());
        loja.setComplemento(dto.complemento());
        loja.setCnpj(dto.cnpj());
    }

    private Loja buscar(UUID id) {
        return lojaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Loja não encontrada"));
    }
}
