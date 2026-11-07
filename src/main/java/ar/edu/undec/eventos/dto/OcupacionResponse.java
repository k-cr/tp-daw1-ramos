package ar.edu.undec.eventos.dto;

// Respuesta de GET /api/eventos/{id}/ocupacion (extra). vendidas = entradas no anuladas.
public record OcupacionResponse(Long eventoId, int capacidad, long vendidas, long disponibles, double porcentajeOcupado) { }
