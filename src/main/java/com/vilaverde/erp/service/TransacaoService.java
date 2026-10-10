package com.vilaverde.erp.service;

import com.vilaverde.erp.dto.transacao.TransacaoRequestDTO;
import com.vilaverde.erp.dto.transacao.TransacaoResponseDTO;
import com.vilaverde.erp.entity.Transacao;
import com.vilaverde.erp.exception.ResourceNotFoundException;
import com.vilaverde.erp.repository.TransacaoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TransacaoService {

    private final TransacaoRepository transacaoRepository;

    @Transactional(readOnly = true)
    public List<TransacaoResponseDTO> listar() {
        return transacaoRepository.findAll().stream().map(TransacaoResponseDTO::fromEntity).toList();
    }

    @Transactional(readOnly = true)
    public TransacaoResponseDTO buscarPorId(UUID id) {
        return TransacaoResponseDTO.fromEntity(buscar(id));
    }

    @Transactional
    public TransacaoResponseDTO criar(TransacaoRequestDTO dto) {
        Transacao transacao = new Transacao();
        transacao.setSigla(dto.sigla());
        transacao.setNome(dto.nome());
        transacao.setTipo(dto.tipo());
        return TransacaoResponseDTO.fromEntity(transacaoRepository.save(transacao));
    }

    @Transactional
    public TransacaoResponseDTO atualizar(UUID id, TransacaoRequestDTO dto) {
        Transacao transacao = buscar(id);
        transacao.setSigla(dto.sigla());
        transacao.setNome(dto.nome());
        transacao.setTipo(dto.tipo());
        return TransacaoResponseDTO.fromEntity(transacaoRepository.save(transacao));
    }

    @Transactional
    public void excluir(UUID id) {
        Transacao transacao = buscar(id);
        transacaoRepository.delete(transacao);
    }

    private Transacao buscar(UUID id) {
        return transacaoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Transação não encontrada"));
    }
}
