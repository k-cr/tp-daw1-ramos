package ar.edu.undec.eventos.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

// DTO para crear/modificar asistentes. @Email valida el formato del mail (si falla → 400 con error en "email").
public record AsistenteRequest(
        @NotBlank String nombre,
        @NotBlank @Email String email) { }
