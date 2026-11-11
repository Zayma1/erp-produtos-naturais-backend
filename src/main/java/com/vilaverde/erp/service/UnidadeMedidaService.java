package com.vilaverde.erp.service;

import com.vilaverde.erp.dto.unidademedida.UnidadeMedidaRequestDTO;
import com.vilaverde.erp.dto.unidademedida.UnidadeMedidaResponseDTO;
import com.vilaverde.erp.entity.UnidadeMedida;
import com.vilaverde.erp.exception.ResourceNotFoundException;
import com.vilaverde.erp.repository.UnidadeMedidaRepository;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UnidadeMedidaService {

  private final UnidadeMedidaRepository unidadeMedidaRepository;

  @Transactional(readOnly = true)
  public List<UnidadeMedidaResponseDTO> listar() {
    return unidadeMedidaRepository
      .findAll()
      .stream()
      .map(UnidadeMedidaResponseDTO::fromEntity)
      .toList();
  }

  @Transactional(readOnly = true)
  public UnidadeMedidaResponseDTO buscarPorId(UUID id) {
    return UnidadeMedidaResponseDTO.fromEntity(buscar(id));
  }

  @Transactional
  public UnidadeMedidaResponseDTO criar(UnidadeMedidaRequestDTO dto) {
    UnidadeMedida unidade = new UnidadeMedida();
    unidade.setSigla(dto.sigla());
    String name=dto.nome().strip().replaceAll("\\s+"," ");
    if(unidadeMedidaRepository.findAll().stream().anyMatch(r -> !r.getId().equals(unidade.getId()) && name.equalsIgnoreCase(r.getNome().strip().replaceAll("\\s+"," "))))throw new com.vilaverde.erp.exception.ConflictException("Nome de unidade já cadastrado");
    unidade.setNome(name);
    return UnidadeMedidaResponseDTO.fromEntity(
      unidadeMedidaRepository.save(unidade)
    );
  }

  @Transactional
  public UnidadeMedidaResponseDTO atualizar(
    UUID id,
    UnidadeMedidaRequestDTO dto
  ) {
    UnidadeMedida unidade = buscar(id);
    unidade.setSigla(dto.sigla());
    String name=dto.nome().strip().replaceAll("\\s+"," ");
    if(unidadeMedidaRepository.findAll().stream().anyMatch(r -> !r.getId().equals(unidade.getId()) && name.equalsIgnoreCase(r.getNome().strip().replaceAll("\\s+"," "))))throw new com.vilaverde.erp.exception.ConflictException("Nome de unidade já cadastrado");
    unidade.setNome(name);
    return UnidadeMedidaResponseDTO.fromEntity(
      unidadeMedidaRepository.save(unidade)
    );
  }

  @Transactional
  public void excluir(UUID id) {
    UnidadeMedida unidade = buscar(id);
    unidadeMedidaRepository.delete(unidade);
  }

  private UnidadeMedida buscar(UUID id) {
    return unidadeMedidaRepository
      .findById(id)
      .orElseThrow(() ->
        new ResourceNotFoundException("Unidade de medida não encontrada")
      );
  }
}
