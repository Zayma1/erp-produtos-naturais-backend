package com.vilaverde.erp.service;
import java.util.*;
import java.security.*;
import java.nio.charset.StandardCharsets;
import java.net.URI;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.transaction.annotation.Transactional;
import com.vilaverde.erp.exception.InvalidInputException;
@Service
public class PasswordRecoveryService {
  private final JdbcTemplate sql;private final AuthorizationService auth;private final UsuarioService users;
  private final JavaMailSender mail;private final String origin,from;
  public PasswordRecoveryService(JdbcTemplate sql,AuthorizationService auth,UsuarioService users,JavaMailSender mail,
    @Value("${FRONTEND_PUBLIC_ORIGIN:http://localhost:3000}") String origin,@Value("${SMTP_FROM:no-reply@vilaverde.local}") String from) {
    this.sql=sql;this.auth=auth;this.users=users;this.mail=mail;this.from=from;
    URI uri=URI.create(origin);
    if(!Set.of("http","https").contains(uri.getScheme()) || uri.getHost()==null || uri.getUserInfo()!=null || uri.getQuery()!=null || uri.getFragment()!=null || !(uri.getPath().isEmpty() || uri.getPath().equals("/")))throw new IllegalArgumentException("FRONTEND_PUBLIC_ORIGIN deve ser uma origem fixa");
    this.origin=origin.replaceAll("/$", "");
  }
  public static String hash(String token) {
    try{return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(token.getBytes(StandardCharsets.UTF_8)));}
    catch(NoSuchAlgorithmException e){throw new IllegalStateException(e);}
  }
  @Transactional public void request(String identifier) {
    sql.query("select pg_advisory_xact_lock(8120048)",rs -> {});
    var account=auth.find(identifier).orElse(null);
    String key=hash(account==null?AuthorizationService.normalize(identifier):account.getEmail());
    sql.update("insert into password_reset_attempt(identifier,started_at,requests) values (?,now(),0) on conflict do nothing",key);
    var row=sql.queryForMap("select requests,started_at < now()-interval '15 minutes' expired from password_reset_attempt where identifier=? for update",key);
    int count=(Boolean)row.get("expired")?0:((Number)row.get("requests")).intValue();
    if(count>=3)return;
    sql.update("update password_reset_attempt set requests=?,started_at=case when ? then now() else started_at end where identifier=?",count+1,row.get("expired"),key);
    if(account==null)return;
    byte[] bytes=new byte[32];new SecureRandom().nextBytes(bytes);
    String token=Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    sql.update("insert into password_reset(token_hash,usuario_id,expires_at) values (?,?,now()+interval '30 minutes')",hash(token),account.getId());
    var message=new SimpleMailMessage();message.setFrom(from);message.setTo(account.getEmail());message.setSubject("Redefinir senha — Vila Verde");
    message.setText("Para redefinir sua senha, abra o link em até 30 minutos. Se não solicitou, ignore esta mensagem.\n"+origin+"/redefinir-senha#"+token);
    try{mail.send(message);}catch(org.springframework.mail.MailException e){
      // Do not expose addresses, tokens or SMTP errors to clients or application logs.
      org.slf4j.LoggerFactory.getLogger(getClass()).warn("Não foi possível entregar uma mensagem de recuperação; confira o SMTP.");
    }
  }
  @Transactional public void reset(String token,String password,String confirmation) {
    if(!Objects.equals(password,confirmation))throw new InvalidInputException("As senhas não coincidem");
    sql.query("select pg_advisory_xact_lock(8120048)",rs -> {});
    var ids=sql.query("select usuario_id from password_reset where token_hash=? and expires_at>clock_timestamp() for update",(rs,n)->rs.getObject(1,UUID.class),hash(token));
    if(ids.isEmpty())throw new InvalidInputException("Link inválido ou expirado. Solicite outro.");
    users.recoverPassword(ids.getFirst(),password);
  }
}
