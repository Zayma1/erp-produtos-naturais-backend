package com.vilaverde.erp.service;

import com.vilaverde.erp.dto.fornecedor.FornecedorRequestDTO;
import com.vilaverde.erp.dto.fornecedor.FornecedorResponseDTO;
import com.vilaverde.erp.entity.Fornecedor;
import com.vilaverde.erp.exception.ResourceNotFoundException;
import com.vilaverde.erp.repository.FornecedorRepository;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class FornecedorService {

  private final LocalidadesService localidades;
  private final FornecedorRepository fornecedorRepository;

  @Transactional(readOnly = true)
  public List<FornecedorResponseDTO> listar() {
    return fornecedorRepository
      .findAll()
      .stream()
      .map(FornecedorResponseDTO::fromEntity)
      .toList();
  }

  @Transactional(readOnly = true)
  public FornecedorResponseDTO buscarPorId(UUID id) {
    return FornecedorResponseDTO.fromEntity(buscar(id));
  }

  @Transactional
  public FornecedorResponseDTO criar(FornecedorRequestDTO dto) {
    Fornecedor fornecedor = new Fornecedor();
    aplicar(fornecedor, dto);
    return FornecedorResponseDTO.fromEntity(
      fornecedorRepository.save(fornecedor)
    );
  }

  @Transactional
  public FornecedorResponseDTO atualizar(UUID id, FornecedorRequestDTO dto) {
    Fornecedor fornecedor = buscar(id);
    aplicar(fornecedor, dto);
    return FornecedorResponseDTO.fromEntity(
      fornecedorRepository.save(fornecedor)
    );
  }

  @Transactional
  public void excluir(UUID id) {
    Fornecedor fornecedor = buscar(id);
    fornecedorRepository.delete(fornecedor);
  }

  private void aplicar(Fornecedor fornecedor, FornecedorRequestDTO dto) {
    if(Documents.normalize(dto.cpf()).isEmpty()==Documents.normalize(dto.cnpj()).isEmpty())throw new com.vilaverde.erp.exception.InvalidInputException("Informe exatamente um documento: CPF ou CNPJ");
    if(fornecedor.getId()!=null && fornecedor.getMunicipioIbge()==null && dto.municipioIbge()==null && (!java.util.Objects.equals(dto.bairro(),fornecedor.getBairro()) || !java.util.Objects.equals(java.util.Objects.toString(dto.complemento(),""),java.util.Objects.toString(fornecedor.getComplemento(),""))))throw new com.vilaverde.erp.exception.InvalidInputException("Selecione o município ao alterar o endereço anterior");
    fornecedor.setNomeFantasia(dto.nomeFantasia());
    fornecedor.setRazaoSocial(dto.razaoSocial());
    var address=localidades.validate(dto.estado(),dto.cidade(),dto.municipioIbge(),fornecedor.getEstado(),fornecedor.getCidade(),fornecedor.getMunicipioIbge());
    fornecedor.setEstado(address.uf());fornecedor.setCidade(address.nome());fornecedor.setMunicipioIbge(address.id());
    fornecedor.setBairro(dto.bairro());
    fornecedor.setComplemento(dto.complemento());
    String cnpj=Documents.parse(dto.cnpj(),true,true);
    if(cnpj!=null && fornecedorRepository.findAll().stream().anyMatch(r -> !r.getId().equals(fornecedor.getId()) && cnpj.equals(r.getCnpj())))throw new com.vilaverde.erp.exception.ConflictException("CNPJ já cadastrado");
    fornecedor.setCnpj(cnpj);
    String cpf=Documents.parse(dto.cpf(),false,true);
    if(cpf!=null && fornecedorRepository.findAll().stream().anyMatch(r -> !r.getId().equals(fornecedor.getId()) && cpf.equals(r.getCpf())))throw new com.vilaverde.erp.exception.ConflictException("CPF já cadastrado");
    fornecedor.setCpf(cpf);
  }

  private Fornecedor buscar(UUID id) {
    return fornecedorRepository
      .findById(id)
      .orElseThrow(() ->
        new ResourceNotFoundException("Fornecedor não encontrado")
      );
  }
}
