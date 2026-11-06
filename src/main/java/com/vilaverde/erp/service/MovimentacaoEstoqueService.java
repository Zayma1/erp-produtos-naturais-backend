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
  private final com.vilaverde.erp.security.AccessScope scope;

  private final MovimentacaoEstoqueRepository repository;

  @Transactional(readOnly = true)
  public List<MovimentacaoResponseDTO> listar(UUID idProduto, UUID idLoja) {
    return scope.rows(repository,"loja.id").stream().filter(e -> (idProduto==null || e.getProduto().getId().equals(idProduto)) && (idLoja==null || e.getLoja().getId().equals(idLoja))).map(MovimentacaoResponseDTO::fromEntity).toList();
  }

  @Transactional(readOnly = true)
  public MovimentacaoResponseDTO buscar(UUID id) {
    return MovimentacaoResponseDTO.fromEntity(
      repository
        .findById(id)
      .filter(e -> scope.permits(e.getLoja().getId()))
        .orElseThrow(() ->
          new ResourceNotFoundException("Movimentação não encontrada")
        )
    );
  }
}
