package com.vilaverde.erp.service;
import com.vilaverde.erp.dto.usuario.*;
import com.vilaverde.erp.entity.*;
import com.vilaverde.erp.entity.enums.*;
import com.vilaverde.erp.exception.*;
import com.vilaverde.erp.repository.*;
import java.util.*;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
@Service @RequiredArgsConstructor
public class UsuarioService {
  private final UsuarioRepository users;
  private final LojaRepository stores;
  private final PasswordEncoder encoder;
  private final JdbcTemplate jdbc;
  @Transactional(readOnly=true) public List<UsuarioResponseDTO> listar() { return users.findAll().stream().map(UsuarioResponseDTO::fromEntity).toList(); }
  @Transactional(readOnly=true) public UsuarioResponseDTO buscarPorId(UUID id) { return UsuarioResponseDTO.fromEntity(find(id)); }
  // Every account write takes the same database lock, including bootstrap and password changes.
  private void lock() { jdbc.query("select pg_advisory_xact_lock(8120048)",rs -> {}); }
  private Usuario find(UUID id) { return users.findById(id).orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado")); }
  private void password(Usuario u,String raw,boolean temporary) {
    if(raw==null || raw.length()<8 || raw.getBytes(java.nio.charset.StandardCharsets.UTF_8).length>72) throw new InvalidInputException("Senha deve ter ao menos 8 caracteres e no máximo 72 bytes");
    u.setSenhaHash(encoder.encode(raw));u.setAlterarSenha(temporary);
  }
  private void revoke(Usuario u) {
    u.setAuthVersion(u.getAuthVersion()+1);
    jdbc.update("delete from spring_session where principal_name=?",u.getEmail());
  }
  private boolean activeAdmin(Usuario u) { return u.getCargo()==CargoUsuario.ADMIN && u.getStatus()==StatusCadastro.ATIVO && u.isRevisado(); }
  private void finalAdmin(Usuario before,UsuarioRequestDTO d) {
    if(activeAdmin(before) && (d.cargo()!=CargoUsuario.ADMIN || d.status()!=StatusCadastro.ATIVO) && users.findAll().stream().filter(this::activeAdmin).count()<=1)
      throw new ConflictException("Não é possível remover o último administrador ativo");
  }
  private void apply(Usuario u,UsuarioRequestDTO d) {
    String email=AuthorizationService.normalize(d.email());
    if(users.existsByEmailAndIdNot(email,u.getId()==null ? new UUID(0,0) : u.getId())) throw new ConflictException("E-mail já cadastrado");
    if(stores.findAllById(d.lojas()).size()!=d.lojas().size()) throw new InvalidInputException("Loja inválida");
    u.setNome(d.nome());u.setEmail(email);u.setCargo(d.cargo());u.setStatus(d.status());u.setLojas(new HashSet<>(d.lojas()));
  }
  @Transactional public UsuarioResponseDTO criar(UsuarioRequestDTO d) {
    lock();var u=new Usuario();apply(u,d);password(u,d.senha(),true);u.setRevisado(true);return UsuarioResponseDTO.fromEntity(users.save(u));
  }
  @Transactional public UsuarioResponseDTO atualizar(UUID id,UsuarioRequestDTO d) {
    lock();var u=find(id);finalAdmin(u,d);revoke(u);apply(u,d);
    if(d.senha()!=null) password(u,d.senha(),true);
    // A legacy account requires an explicit password reset as part of its review.
    if(!u.isRevisado() && d.senha()!=null)u.setRevisado(true);
    return UsuarioResponseDTO.fromEntity(users.save(u));
  }
  @Transactional public void excluir(UUID id) {
    lock();var u=find(id);finalAdmin(u,new UsuarioRequestDTO(u.getNome(),u.getEmail(),null,u.getCargo(),StatusCadastro.INATIVO,u.getLojas()));
    revoke(u);u.setStatus(StatusCadastro.INATIVO);users.save(u);
  }
  @Transactional public void reset(UUID id,String senha) { lock();var u=find(id);revoke(u);password(u,senha,true);u.setRevisado(true);users.save(u); }
  @Transactional public void changePassword(UUID id,String old,String next) {
    lock();var u=find(id);if(!encoder.matches(old,u.getSenhaHash()))throw new InvalidInputException("Senha atual inválida");
    if(encoder.matches(next,u.getSenhaHash()))throw new InvalidInputException("Escolha uma senha diferente da atual");
    revoke(u);password(u,next,false);users.save(u);
  }
  @Transactional public void bootstrap(String nome,String email,String senha) {
    lock();if(users.findAll().stream().anyMatch(this::activeAdmin))throw new ConflictException("Já existe administrador ativo");
    createBootstrap(nome,email,senha,false);
  }
  @Transactional public boolean bootstrapDefaultAdmin(String senha) {
    lock();
    if(users.findAll().stream().anyMatch(this::activeAdmin))return false;
    createBootstrap("Administrador",AuthorizationService.DEFAULT_ADMIN_EMAIL,senha,true);
    return true;
  }
  private void createBootstrap(String nome,String email,String senha,boolean automatic) {
    var u=users.findByEmail(AuthorizationService.normalize(email)).orElseGet(Usuario::new);
    revoke(u);u.setNome(nome);u.setEmail(AuthorizationService.normalize(email));u.setCargo(CargoUsuario.ADMIN);u.setStatus(StatusCadastro.ATIVO);u.setRevisado(true);
    // The requested initial admin/admin credential is the only short-password exception.
    // Business access remains blocked until it is replaced through the normal password policy.
    if(automatic && "admin".equals(senha)) {u.setSenhaHash(encoder.encode(senha));u.setAlterarSenha(true);}
    else password(u,senha,true);
    users.save(u);
  }
}
