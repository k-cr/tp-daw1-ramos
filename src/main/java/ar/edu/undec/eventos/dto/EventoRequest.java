package ar.edu.undec.eventos.dto;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

// DTO de entrada para POST/PUT de eventos. Es un record: clase inmutable con constructor y getters automáticos.
// Las anotaciones de Bean Validation se chequean cuando el controller pone @Valid;
// si alguna falla, Spring tira MethodArgumentNotValidException → 400 con "errores".
public record EventoRequest(
        @NotBlank String nombre,                       // no null, no vacío, no solo espacios
        @NotNull @Future LocalDateTime fecha,          // fecha y hora futura
        @NotNull @Positive Integer capacidad,          // mayor que 0
        @NotNull @PositiveOrZero BigDecimal precio) { } // 0 o más (puede haber eventos gratis)
