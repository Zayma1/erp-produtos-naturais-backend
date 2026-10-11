package com.vilaverde.erp.service;

import com.vilaverde.erp.dto.venda.ProdutoVendaRequestDTO;
import com.vilaverde.erp.dto.venda.VendaRequestDTO;
import com.vilaverde.erp.dto.venda.VendaResponseDTO;
import com.vilaverde.erp.entity.Cliente;
import com.vilaverde.erp.entity.Loja;
import com.vilaverde.erp.entity.Produto;
import com.vilaverde.erp.entity.ProdutoVenda;
import com.vilaverde.erp.entity.Usuario;
import com.vilaverde.erp.entity.Venda;
import com.vilaverde.erp.exception.ResourceNotFoundException;
import com.vilaverde.erp.repository.ClienteRepository;
import com.vilaverde.erp.repository.LojaRepository;
import com.vilaverde.erp.repository.ProdutoRepository;
import com.vilaverde.erp.repository.UsuarioRepository;
import com.vilaverde.erp.repository.VendaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class VendaService {

    private final VendaRepository vendaRepository;
    private final ClienteRepository clienteRepository;
    private final UsuarioRepository usuarioRepository;
    private final LojaRepository lojaRepository;
    private final ProdutoRepository produtoRepository;

    @Transactional(readOnly = true)
    public List<VendaResponseDTO> listar(UUID idCliente, UUID idUsuario, UUID idLoja) {
        List<Venda> vendas;
        if (idCliente != null) {
            vendas = vendaRepository.findByClienteId(idCliente);
        } else if (idUsuario != null) {
            vendas = vendaRepository.findByUsuarioId(idUsuario);
        } else if (idLoja != null) {
            vendas = vendaRepository.findByLojaId(idLoja);
        } else {
            vendas = vendaRepository.findAll();
        }
        return vendas.stream().map(VendaResponseDTO::fromEntity).toList();
    }

    @Transactional(readOnly = true)
    public VendaResponseDTO buscarPorId(UUID id) {
        return VendaResponseDTO.fromEntity(buscar(id));
    }

    @Transactional
    public VendaResponseDTO criar(VendaRequestDTO dto) {
        Venda venda = new Venda();
        aplicarCabecalho(venda, dto);
        aplicarItens(venda, dto.itens());
        venda.setPrecoVenda(calcularTotal(venda));
        return VendaResponseDTO.fromEntity(vendaRepository.save(venda));
    }

    @Transactional
    public VendaResponseDTO atualizar(UUID id, VendaRequestDTO dto) {
        Venda venda = buscar(id);
        aplicarCabecalho(venda, dto);
        venda.getItens().clear();
        aplicarItens(venda, dto.itens());
        venda.setPrecoVenda(calcularTotal(venda));
        return VendaResponseDTO.fromEntity(vendaRepository.save(venda));
    }

    @Transactional
    public void excluir(UUID id) {
        Venda venda = buscar(id);
        vendaRepository.delete(venda);
    }

    private void aplicarCabecalho(Venda venda, VendaRequestDTO dto) {
        venda.setDataVenda(dto.dataVenda());
        venda.setCliente(buscarCliente(dto.idCliente()));
        venda.setUsuario(buscarUsuario(dto.idUsuario()));
        venda.setLoja(buscarLoja(dto.idLoja()));
    }

    private void aplicarItens(Venda venda, List<ProdutoVendaRequestDTO> itens) {
        for (ProdutoVendaRequestDTO itemDto : itens) {
            ProdutoVenda item = new ProdutoVenda();
            item.setVenda(venda);
            item.setProduto(buscarProduto(itemDto.idProduto()));
            item.setQuantidade(itemDto.quantidade());
            item.setPrecoUnitario(itemDto.precoUnitario());
            item.setDesconto(itemDto.desconto() != null ? itemDto.desconto() : BigDecimal.ZERO);
            venda.getItens().add(item);
        }
    }

    private BigDecimal calcularTotal(Venda venda) {
        return venda.getItens().stream()
                .map(item -> item.getQuantidade()
                        .multiply(item.getPrecoUnitario())
                        .subtract(item.getDesconto()))
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(2, RoundingMode.HALF_UP);
    }

    private Venda buscar(UUID id) {
        return vendaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Venda não encontrada"));
    }

    private Cliente buscarCliente(UUID id) {
        return clienteRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Cliente não encontrado"));
    }

    private Usuario buscarUsuario(UUID id) {
        return usuarioRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado"));
    }

    private Loja buscarLoja(UUID id) {
        return lojaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Loja não encontrada"));
    }

    private Produto buscarProduto(UUID id) {
        return produtoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Produto não encontrado"));
    }
}
