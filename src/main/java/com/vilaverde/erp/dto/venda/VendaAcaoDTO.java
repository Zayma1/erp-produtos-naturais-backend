package com.vilaverde.erp.dto.venda;
import jakarta.validation.constraints.*;
public record VendaAcaoDTO(@NotNull @Min(0) Integer versao, String motivo) {}
