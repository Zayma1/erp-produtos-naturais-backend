package com.vilaverde.erp.service;

import com.vilaverde.erp.dto.loja.LojaRequestDTO;
import com.vilaverde.erp.dto.loja.LojaResponseDTO;
import com.vilaverde.erp.entity.Loja;
import com.vilaverde.erp.exception.ResourceNotFoundException;
import com.vilaverde.erp.repository.LojaRepository;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class LojaService {
  private final com.vilaverde.erp.security.AccessScope scope;

  private final LocalidadesService localidades;
  private final LojaRepository lojaRepository;

  @Transactional(readOnly = true)
  public List<LojaResponseDTO> listar() {
    return scope.rows(lojaRepository,"id").stream().map(LojaResponseDTO::fromEntity).toList();
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
    if(loja.getId()!=null && loja.getMunicipioIbge()==null && dto.municipioIbge()==null && (!java.util.Objects.equals(dto.bairro(),loja.getBairro()) || !java.util.Objects.equals(dto.complemento(),loja.getComplemento()) || !java.util.Objects.equals(dto.numero(),loja.getNumero())))throw new com.vilaverde.erp.exception.InvalidInputException("Selecione o município ao alterar o endereço anterior");
    loja.setNomeFantasia(dto.nomeFantasia());
    loja.setRazaoSocial(dto.razaoSocial());
    var address=localidades.validate(dto.estado(),dto.cidade(),dto.municipioIbge(),loja.getEstado(),loja.getCidade(),loja.getMunicipioIbge());
    loja.setEstado(address.uf());loja.setCidade(address.nome());loja.setMunicipioIbge(address.id());
    loja.setBairro(dto.bairro());
    loja.setNumero(dto.numero());
    loja.setComplemento(dto.complemento());
    String cnpj=Documents.parse(dto.cnpj(),true,false);
    if(cnpj!=null && lojaRepository.findAll().stream().anyMatch(r -> !r.getId().equals(loja.getId()) && cnpj.equals(r.getCnpj())))throw new com.vilaverde.erp.exception.ConflictException("CNPJ já cadastrado");
    loja.setCnpj(cnpj);
  }

  private Loja buscar(UUID id) {
    scope.store(id);
    return lojaRepository
      .findById(id)
      .orElseThrow(() -> new ResourceNotFoundException("Loja não encontrada"));
  }
}
