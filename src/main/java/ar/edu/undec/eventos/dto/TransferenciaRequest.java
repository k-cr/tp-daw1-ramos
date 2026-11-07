package ar.edu.undec.eventos.dto;

import jakarta.validation.constraints.NotNull;

// Body de PATCH /api/entradas/{id}/transferir: a qué asistente se pasa la entrada (extra).
public record TransferenciaRequest(@NotNull Long asistenteId) { }
