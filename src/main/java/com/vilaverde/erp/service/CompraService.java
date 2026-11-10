package com.vilaverde.erp.service;

import com.vilaverde.erp.dto.compra.*;
import com.vilaverde.erp.entity.*;
import com.vilaverde.erp.entity.enums.StatusCompra;
import com.vilaverde.erp.exception.*;
import com.vilaverde.erp.repository.*;
import java.math.*;
import java.time.OffsetDateTime;
import java.util.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CompraService {
  private final com.vilaverde.erp.security.AccessScope scope;

  private final CompraRepository repository;
  private final RecebimentoCompraRepository recebimentos;
  private final ItemCompraRepository itemRepository;
  private final FornecedorRepository fornecedorRepository;
  private final LojaRepository lojaRepository;
  private final UsuarioRepository usuarioRepository;
  private final ProdutoRepository produtoRepository;

  @Transactional(readOnly = true)
  public List<CompraResponseDTO> listar() {
    return scope.rows(repository,"loja.id").stream().map(this::response).toList();
  }

  @Transactional(readOnly = true)
  public CompraResponseDTO buscar(UUID id) {
    return response(
      repository
        .findById(id)
        .filter(candidate -> scope.permits(candidate.getLoja().getId()))
        .orElseThrow(() ->
          new ResourceNotFoundException("Compra não encontrada")
        )
    );
  }

  @Transactional
  public CompraResponseDTO criar(CompraRequestDTO d) {
    Compra e = new Compra();
    aplicar(e, d);
    e.setStatus(StatusCompra.RASCUNHO);
    e = repository.save(e);
    salvarItens(e, d.itens());
    return response(e);
  }

  @Transactional
  public CompraResponseDTO atualizar(UUID id, CompraRequestDTO d) {
    Compra e = repository
      .lockById(id)
      .filter(candidate -> scope.permits(candidate.getLoja().getId()))
      .orElseThrow(() ->
        new ResourceNotFoundException("Compra não encontrada")
      );
    if (recebimentos.existsByCompraId(id)) throw new ConflictException("Compra possui recebimentos; exclua os rascunhos antes de editar");
    if (e.getStatus() != StatusCompra.RASCUNHO) throw new BusinessException(
      "Somente compras em rascunho podem ser alteradas"
    );
    itemRepository.deleteAll(itemRepository.findByCompraId(id));
    aplicar(e, d);
    repository.save(e);
    salvarItens(e, d.itens());
    return response(e);
  }

  @Transactional
  public void excluir(UUID id) {
    Compra e = repository
      .lockById(id)
      .filter(candidate -> scope.permits(candidate.getLoja().getId()))
      .orElseThrow(() ->
        new ResourceNotFoundException("Compra não encontrada")
      );
    if (recebimentos.existsByCompraId(id)) throw new ConflictException("Compra possui recebimentos; exclua os rascunhos antes de editar");
    if (e.getStatus() != StatusCompra.RASCUNHO) throw new BusinessException(
      "Somente compras em rascunho podem ser excluídas"
    );
    repository.delete(e);
  }

  private void aplicar(Compra e, CompraRequestDTO d) {
    scope.store(d.idLoja());
    var actor=scope.actor(d.idUsuario());
    if(e.getId()==null)e.setUsuario(actor);
    e.setModificador(actor);
    e.setFornecedor(
      fornecedorRepository
        .findById(d.idFornecedor())
        .orElseThrow(() ->
          new ResourceNotFoundException("Fornecedor não encontrado")
        )
    );
    e.setLoja(
      lojaRepository
        .findById(d.idLoja())
        .orElseThrow(() -> new ResourceNotFoundException("Loja não encontrada"))
    );
    e.setDataCompra(
      d.dataCompra() == null ? OffsetDateTime.now() : d.dataCompra()
    );
    e.setObservacao(d.observacao());
    if (e.getCriadoEm() == null) e.setCriadoEm(OffsetDateTime.now());
  }

  private void salvarItens(Compra e, List<ItemCompraRequestDTO> itens) {
    BigDecimal total = BigDecimal.ZERO;
    for (ItemCompraRequestDTO d : itens) {
      ItemCompra i = new ItemCompra();
      i.setCompra(e);
      i.setProduto(
        produtoRepository
          .findById(d.idProduto())
          .orElseThrow(() ->
            new ResourceNotFoundException("Produto não encontrado")
          )
      );
      i.setQuantidade(d.quantidade());
      i.setCustoUnitario(d.custoUnitario());
      i.setDesconto(d.desconto() == null ? BigDecimal.ZERO : d.desconto());
      i.setTotalItem(
        d
          .quantidade()
          .multiply(d.custoUnitario())
          .subtract(i.getDesconto())
          .setScale(2, RoundingMode.HALF_UP)
      );
      total = total.add(i.getTotalItem());
      itemRepository.save(i);
    }
    e.setValorTotal(total.setScale(2, RoundingMode.HALF_UP));
    repository.save(e);
  }

  private CompraResponseDTO response(Compra e) {
    return CompraResponseDTO.fromEntity(
      e,
      itemRepository
        .findByCompraId(e.getId())
        .stream()
        .map(ItemCompraResponseDTO::fromEntity)
        .toList()
    );
  }
}
