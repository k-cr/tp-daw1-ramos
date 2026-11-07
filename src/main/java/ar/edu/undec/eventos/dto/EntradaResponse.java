package ar.edu.undec.eventos.dto;

import ar.edu.undec.eventos.model.Entrada;
import ar.edu.undec.eventos.model.EstadoEntrada;

import java.math.BigDecimal;
import java.time.LocalDateTime;

// DTO de salida de una entrada. No devuelvo la entidad directo para mandar
// eventoId y asistenteId planos (como pide el enunciado) en vez de los objetos completos.
public record EntradaResponse(
        Long id,
        Long eventoId,
        String eventoNombre,
        Long asistenteId,
        String asistenteNombre,
        EstadoEntrada estado,
        String codigo,
        BigDecimal importe,
        LocalDateTime fechaCreacion,
        LocalDateTime fechaPago) {

    // Convierte la entidad en el DTO. Se usa en los controllers con .map(EntradaResponse::de).
    public static EntradaResponse de(Entrada e) {
        return new EntradaResponse(e.getId(),
                e.getEvento().getId(), e.getEvento().getNombre(),
                e.getAsistente().getId(), e.getAsistente().getNombre(),
                e.getEstado(), e.getCodigo(), e.getImporte(),
                e.getFechaCreacion(), e.getFechaPago());
    }
}
