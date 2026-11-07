package ar.edu.undec.eventos.dto;

import jakarta.validation.constraints.NotNull;

// DTO para reservar una entrada. Solo los ids: el estado inicial (RESERVADA) lo pone el sistema.
public record EntradaRequest(@NotNull Long eventoId, @NotNull Long asistenteId) { }
