package com.vilaverde.erp.service;

import com.vilaverde.erp.dto.estoque.MovimentacaoResponseDTO;
import com.vilaverde.erp.entity.MovimentacaoEstoque;
import com.vilaverde.erp.exception.ResourceNotFoundException;
import com.vilaverde.erp.repository.MovimentacaoEstoqueRepository;
import java.util.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MovimentacaoEstoqueService {

  private final MovimentacaoEstoqueRepository repository;

  @Transactional(readOnly = true)
  public List<MovimentacaoResponseDTO> listar(UUID idProduto, UUID idLoja) {
    List<MovimentacaoEstoque> l =
      idProduto != null && idLoja != null
        ? repository.findByProdutoIdAndLojaId(idProduto, idLoja)
        : repository.findAll();
    return l.stream().map(MovimentacaoResponseDTO::fromEntity).toList();
  }

  @Transactional(readOnly = true)
  public MovimentacaoResponseDTO buscar(UUID id) {
    return MovimentacaoResponseDTO.fromEntity(
      repository
        .findById(id)
        .orElseThrow(() ->
          new ResourceNotFoundException("Movimentação não encontrada")
        )
    );
  }
}
