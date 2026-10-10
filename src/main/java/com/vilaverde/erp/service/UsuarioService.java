package com.vilaverde.erp.service;

import com.vilaverde.erp.dto.usuario.UsuarioRequestDTO;
import com.vilaverde.erp.dto.usuario.UsuarioResponseDTO;
import com.vilaverde.erp.entity.Usuario;
import com.vilaverde.erp.exception.BusinessException;
import com.vilaverde.erp.exception.ResourceNotFoundException;
import com.vilaverde.erp.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;

    @Transactional(readOnly = true)
    public List<UsuarioResponseDTO> listar() {
        return usuarioRepository.findAll().stream().map(UsuarioResponseDTO::fromEntity).toList();
    }

    @Transactional(readOnly = true)
    public UsuarioResponseDTO buscarPorId(UUID id) {
        return UsuarioResponseDTO.fromEntity(buscar(id));
    }

    @Transactional
    public UsuarioResponseDTO criar(UsuarioRequestDTO dto) {
        if (usuarioRepository.existsByEmail(dto.email())) {
            throw new BusinessException("E-mail já cadastrado");
        }
        Usuario usuario = new Usuario();
        usuario.setNome(dto.nome());
        usuario.setEmail(dto.email());
        usuario.setSenha(dto.senha());
        usuario.setCargo(dto.cargo() != null && !dto.cargo().isBlank() ? dto.cargo() : "func");
        usuario.setCriacaoTimestamp(LocalDateTime.now());
        usuario.setDataCriacao(LocalDate.now());
        return UsuarioResponseDTO.fromEntity(usuarioRepository.save(usuario));
    }

    @Transactional
    public UsuarioResponseDTO atualizar(UUID id, UsuarioRequestDTO dto) {
        if (usuarioRepository.existsByEmailAndIdNot(dto.email(), id)) {
            throw new BusinessException("E-mail já cadastrado");
        }
        Usuario usuario = buscar(id);
        usuario.setNome(dto.nome());
        usuario.setEmail(dto.email());
        usuario.setSenha(dto.senha());
        usuario.setCargo(dto.cargo() != null && !dto.cargo().isBlank() ? dto.cargo() : usuario.getCargo());
        return UsuarioResponseDTO.fromEntity(usuarioRepository.save(usuario));
    }

    @Transactional
    public void excluir(UUID id) {
        Usuario usuario = buscar(id);
        usuarioRepository.delete(usuario);
    }

    private Usuario buscar(UUID id) {
        return usuarioRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado"));
    }
}
