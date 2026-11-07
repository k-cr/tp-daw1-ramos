package ar.edu.undec.eventos.dto;

import jakarta.validation.constraints.NotBlank;

// Body de POST /api/entradas/validar: {"codigo":"..."} (extra código de entrada).
public record CodigoRequest(@NotBlank String codigo) { }
