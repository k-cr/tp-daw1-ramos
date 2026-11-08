package ar.edu.undec.eventos.repository;

import ar.edu.undec.eventos.model.Entrada;
import ar.edu.undec.eventos.model.EstadoEntrada;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

// Spring Data arma la consulta SQL a partir del nombre de cada método (query methods).
// JpaRepository ya trae save, findById, findAll, delete, etc.
public interface EntradaRepository extends JpaRepository<Entrada, Long> {

    // Cuenta las entradas del evento cuyo estado NO es el indicado (lo uso con ANULADA → "vendidas").
    long countByEventoIdAndEstadoNot(Long eventoId, EstadoEntrada estado);

    // Confirma si el asistente ya tiene una entrada no anulada para ese evento (regla 2)
    boolean existsByEventoIdAndAsistenteIdAndEstadoNot(Long eventoId, Long asistenteId, EstadoEntrada estado);

    // Confirma si el evento tiene alguna entrada (regla 3: no se puede borrar)
    boolean existsByEventoId(Long eventoId);

    // Confirma si el asistente tiene alguna entrada (no se puede borrar)
    boolean existsByAsistenteId(Long asistenteId);

    // GET /api/eventos/{id}/entradas
    List<Entrada> findByEventoId(Long eventoId);

    // GET /api/asistentes/{id}/entradas
    List<Entrada> findByAsistenteId(Long asistenteId);

    // Extra validar: busca por el código UUID.
    Optional<Entrada> findByCodigo(String codigo);

    // Extra anulación automática: reservas creadas antes del límite (hace más de 48 h).
    List<Entrada> findByEstadoAndFechaCreacionBefore(EstadoEntrada estado, LocalDateTime limite);
}
