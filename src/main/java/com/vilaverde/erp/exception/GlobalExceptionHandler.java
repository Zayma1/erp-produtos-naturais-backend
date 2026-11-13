package com.vilaverde.erp.exception;

import java.util.List;
import java.util.Objects;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

  @ExceptionHandler(ResourceNotFoundException.class)
  public ResponseEntity<ApiError> handleNotFound(ResourceNotFoundException ex) {
    ApiError error = new ApiError(
      HttpStatus.NOT_FOUND.value(),
      "Recurso não encontrado",
      ex.getMessage()
    );
    return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
  }

  @ExceptionHandler(BusinessException.class)
  public ResponseEntity<ApiError> handleBusiness(BusinessException ex) {
    ApiError error = new ApiError(
      HttpStatus.UNPROCESSABLE_ENTITY.value(),
      "Regra de negócio violada",
      ex.getMessage()
    );
    return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(error);
  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<ApiError> handleValidation(
    MethodArgumentNotValidException ex
  ) {
    List<String> detalhes = ex
      .getBindingResult()
      .getFieldErrors()
      .stream()
      .map(f -> f.getField() + ": " + f.getDefaultMessage())
      .toList();
    ApiError error = new ApiError(
      HttpStatus.BAD_REQUEST.value(),
      "Dados inválidos",
      "Verifique os campos enviados",
      detalhes
    );
    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
  }

  @ExceptionHandler(DataIntegrityViolationException.class)
  public ResponseEntity<ApiError> handleIntegrity(
    DataIntegrityViolationException ex
  ) {
    Throwable cause=ex;while(cause.getCause()!=null)cause=cause.getCause();
    String state=cause instanceof java.sql.SQLException sql ? sql.getSQLState() : "";
    String detail=Objects.toString(cause.getMessage(),"");
    String message="Conflito de integridade";int status=409;
    if("23505".equals(state)) {
      if(detail.contains("cliente_cpf_unique"))message="CPF de cliente já cadastrado";
      else if(detail.contains("fornecedor_cpf_unique"))message="CPF de fornecedor já cadastrado";
      else if(detail.contains("fornecedor_cnpj_unique"))message="CNPJ de fornecedor já cadastrado";
      else if(detail.contains("loja_cnpj_unique"))message="CNPJ de loja já cadastrado";
      else if(detail.contains("unidade_nome_unique"))message="Nome de unidade já cadastrado";
      else if(detail.contains("usuario_login_unique"))message="Usuário já cadastrado";
      else message="Valor único já cadastrado";
    } else if("23503".equals(state))message="Registro em uso ou referência inválida";
    else if("22001".equals(state) || "23514".equals(state) || "23502".equals(state)) {status=400;message="Formato, tamanho ou combinação de campos inválida";}
    else {status=500;message="Não foi possível persistir o registro";}
    return ResponseEntity.status(status).body(new ApiError(status,"Integridade",message));
  }
  @ExceptionHandler(org.springframework.web.server.ResponseStatusException.class)
  public ResponseEntity<ApiError> unavailable(org.springframework.web.server.ResponseStatusException ex) {
    var response=ResponseEntity.status(ex.getStatusCode());
    java.util.List<String> details=null;
    var auth=org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
    if(ex instanceof LocalidadesUnavailableException locality && auth!=null && auth.isAuthenticated() &&
      auth.getAuthorities().stream().anyMatch(a->a.getAuthority().equals("ROLE_ADMIN"))) {
      response.header("X-IBGE-Failure",locality.diagnostic());
      details=locality.diagnosticDetails();
    }
    return response.header("Cache-Control","no-store").body(new ApiError(ex.getStatusCode().value(),"Serviço indisponível",ex.getReason(),details));
  }

  @ExceptionHandler(InvalidInputException.class)
  public ResponseEntity<ApiError> handleInvalidInputException(InvalidInputException ex) {
    return ResponseEntity.status(400).body(new ApiError(400, "Dados inválidos", ex.getMessage()));
  }

  @ExceptionHandler(ConflictException.class)
  public ResponseEntity<ApiError> handleConflictException(ConflictException ex) {
    return ResponseEntity.status(409).body(new ApiError(409, "Conflito", ex.getMessage()));
  }

  @ExceptionHandler({org.springframework.web.method.annotation.MethodArgumentTypeMismatchException.class,
    org.springframework.web.bind.MissingRequestHeaderException.class,
    org.springframework.web.bind.MissingServletRequestParameterException.class,
    org.springframework.http.converter.HttpMessageNotReadableException.class})
  public ResponseEntity<ApiError> malformed(Exception ex) {
    return ResponseEntity.badRequest().body(new ApiError(400, "Dados inválidos", "Parâmetro, cabeçalho ou JSON inválido"));
  }

  @ExceptionHandler(org.springframework.security.access.AccessDeniedException.class)
  public ResponseEntity<ApiError> forbidden(Exception ex) { return ResponseEntity.status(403).body(new ApiError(403,"Acesso negado","Operação não permitida")); }

  @ExceptionHandler(Exception.class)
  public ResponseEntity<ApiError> handleGeneric(Exception ex) {
    ApiError error = new ApiError(
      HttpStatus.INTERNAL_SERVER_ERROR.value(),
      "Erro interno",
      ex.getMessage()
    );
    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
  }
}
