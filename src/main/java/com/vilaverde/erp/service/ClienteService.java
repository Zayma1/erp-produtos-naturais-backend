package com.vilaverde.erp.service;

import com.vilaverde.erp.dto.cliente.ClienteRequestDTO;
import com.vilaverde.erp.dto.cliente.ClienteResponseDTO;
import com.vilaverde.erp.entity.Cliente;
import com.vilaverde.erp.exception.ResourceNotFoundException;
import com.vilaverde.erp.repository.ClienteRepository;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ClienteService {

  private final ClienteRepository clienteRepository;

  @Transactional(readOnly = true)
  public List<ClienteResponseDTO> listar() {
    return clienteRepository
      .findAll()
      .stream()
      .map(ClienteResponseDTO::fromEntity)
      .toList();
  }

  @Transactional(readOnly = true)
  public ClienteResponseDTO buscarPorId(UUID id) {
    return ClienteResponseDTO.fromEntity(buscar(id));
  }

  @Transactional
  public ClienteResponseDTO criar(ClienteRequestDTO dto) {
    Cliente cliente = new Cliente();
    cliente.setNome(dto.nome());
    String cpf=Documents.parse(dto.cpf(),false,true);
    if(cpf!=null && clienteRepository.findAll().stream().anyMatch(r -> !r.getId().equals(cliente.getId()) && cpf.equals(r.getCpf())))throw new com.vilaverde.erp.exception.ConflictException("CPF já cadastrado");
    cliente.setCpf(cpf);
    cliente.setDataNascimento(dto.dataNascimento());
    return ClienteResponseDTO.fromEntity(clienteRepository.save(cliente));
  }

  @Transactional
  public ClienteResponseDTO atualizar(UUID id, ClienteRequestDTO dto) {
    Cliente cliente = buscar(id);
    cliente.setNome(dto.nome());
    String cpf=Documents.parse(dto.cpf(),false,true);
    if(cpf!=null && clienteRepository.findAll().stream().anyMatch(r -> !r.getId().equals(cliente.getId()) && cpf.equals(r.getCpf())))throw new com.vilaverde.erp.exception.ConflictException("CPF já cadastrado");
    cliente.setCpf(cpf);
    cliente.setDataNascimento(dto.dataNascimento());
    return ClienteResponseDTO.fromEntity(clienteRepository.save(cliente));
  }

  @Transactional
  public void excluir(UUID id) {
    Cliente cliente = buscar(id);
    clienteRepository.delete(cliente);
  }

  private Cliente buscar(UUID id) {
    return clienteRepository
      .findById(id)
      .orElseThrow(() ->
        new ResourceNotFoundException("Cliente não encontrado")
      );
  }
}
