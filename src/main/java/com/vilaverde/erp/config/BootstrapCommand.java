package com.vilaverde.erp.config;
import com.vilaverde.erp.service.UsuarioService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.boot.*;
import org.springframework.context.ConfigurableApplicationContext;
@Component @Profile("bootstrap-admin") @RequiredArgsConstructor
public class BootstrapCommand implements ApplicationRunner {
  private final UsuarioService users;
  private final ConfigurableApplicationContext context;
  @Override public void run(ApplicationArguments args) {
    if(context.getEnvironment().getProperty("spring.main.web-application-type","").equals("none")==false) throw new IllegalStateException("Bootstrap exige --spring.main.web-application-type=none");
    var console=System.console();if(console==null)throw new IllegalStateException("Execute em terminal interativo");
    String nome=console.readLine("Nome: "),email=console.readLine("E-mail: ");char[] senha=console.readPassword("Senha temporária (mínimo de 8 caracteres): ");
    try { users.bootstrap(nome,email,new String(senha)); } finally { java.util.Arrays.fill(senha,'\0'); }
    console.printf("Administrador criado. Troca de senha obrigatória.\n");context.close();
  }
}
