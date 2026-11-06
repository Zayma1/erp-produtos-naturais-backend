package com.vilaverde.erp.dto.ajuste;
import jakarta.validation.constraints.*;
public record AjusteAcaoDTO(@NotNull @Min(0) Integer versao,String motivo) {}
