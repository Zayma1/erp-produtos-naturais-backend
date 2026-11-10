package com.vilaverde.erp.dto.recebimento;
import jakarta.validation.constraints.*;
public record ConcluirRecebimentoRequestDTO(@NotNull @PositiveOrZero Integer versao) {}
