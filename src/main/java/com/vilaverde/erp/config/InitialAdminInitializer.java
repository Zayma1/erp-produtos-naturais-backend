package com.vilaverde.erp.config;

import com.vilaverde.erp.service.UsuarioService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnWebApplication(type=ConditionalOnWebApplication.Type.SERVLET)
public class InitialAdminInitializer implements ApplicationRunner {
  private final UsuarioService users;
  private final boolean enabled;
  private final String password;

  public InitialAdminInitializer(UsuarioService users,
      @Value("${BOOTSTRAP_ADMIN_ENABLED:true}") boolean enabled,
      @Value("${BOOTSTRAP_ADMIN_PASSWORD:admin}") String password) {
    this.users=users;this.enabled=enabled;this.password=password;
  }

  @Override public void run(ApplicationArguments args) {
    if(enabled)users.bootstrapDefaultAdmin(password);
  }
}
