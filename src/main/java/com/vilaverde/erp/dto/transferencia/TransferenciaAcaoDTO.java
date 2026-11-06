package com.vilaverde.erp.dto.transferencia;
import jakarta.validation.constraints.*;
public record TransferenciaAcaoDTO(@NotNull @Min(0) Integer versao,String motivo) {}
