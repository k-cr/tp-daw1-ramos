package ar.edu.undec.eventos.dto;

import jakarta.validation.constraints.NotBlank;

// Body del PATCH /estado: {"estado":"PAGADA"}. Lo recibo como String para poder devolver 400 si el estado no existe.
public record EstadoRequest(@NotBlank String estado) { }
