package ar.edu.undec.eventos.service;

import ar.edu.undec.eventos.dto.EventoRequest;
import ar.edu.undec.eventos.dto.OcupacionResponse;
import ar.edu.undec.eventos.exception.ConflictoException;
import ar.edu.undec.eventos.exception.NoEncontradoException;
import ar.edu.undec.eventos.model.EstadoEntrada;
import ar.edu.undec.eventos.model.Evento;
import ar.edu.undec.eventos.repository.EntradaRepository;
import ar.edu.undec.eventos.repository.EventoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

// Capa de servicio: acá va la lógica de negocio. El controller solo recibe y responde HTTP.
@Service
public class EventoService {

    private final EventoRepository eventos;
    private final EntradaRepository entradas;

    // Inyección por constructor: Spring pasa los repositorios solo (no hace falta @Autowired).
    public EventoService(EventoRepository eventos, EntradaRepository entradas) {
        this.eventos = eventos;
        this.entradas = entradas;
    }

    // Hace GET de los eventos
    public List<Evento> listar(String nombre, Boolean futuro) {
        // Si futuro es true, solo trae los eventos futuros
        if (Boolean.TRUE.equals(futuro)) return eventos.findByFechaAfter(LocalDateTime.now());
        // Si nombre no tiene nada, trae todos
        if (nombre == null || nombre.isBlank()) return eventos.findAll();
        return eventos.findByNombreContainingIgnoreCase(nombre.trim());
    }

    // findById devuelve un Optional: si está vacío tiro NoEncontradoException → 404.
    public Evento buscar(Long id) {
        return eventos.findById(id)
                .orElseThrow(() -> new NoEncontradoException("No existe el evento " + id));
    }

    // @Transactional: todo el método corre en una transacción; si tira excepción se hace rollback.
    @Transactional
    public Evento crear(EventoRequest req) {
        Evento e = new Evento();
        copiar(req, e);
        return eventos.save(e);
    }

    @Transactional
    public Evento modificar(Long id, EventoRequest req) {
        Evento e = buscar(id);
        long vendidas = vendidas(id);
        // Regla propia: si ya vendí 50, no puedo bajar la capacidad a 40.
        if (req.capacidad() < vendidas) {
            throw new ConflictoException("La capacidad no puede ser menor a las " + vendidas + " entradas ya vendidas");
        }
        copiar(req, e);
        return eventos.save(e);
    }

    @Transactional
    public void borrar(Long id) {
        Evento e = buscar(id); // primero 404 si no existe
        // Regla 3: un evento con entradas (de cualquier estado) no se puede borrar → 409.
        if (entradas.existsByEventoId(id)) {
            throw new ConflictoException("El evento tiene entradas y no se puede borrar");
        }
        eventos.delete(e);
    }

    /** Extra: vendidas, disponibles y porcentaje ocupado. */
    public OcupacionResponse ocupacion(Long id) {
        Evento e = buscar(id);
        long vendidas = vendidas(id);
        long disponibles = Math.max(0, e.getCapacidad() - vendidas);
        // Porcentaje redondeado a 2 decimales: multiplico por 10000, redondeo y divido por 100.
        double porcentaje = Math.round(vendidas * 10000.0 / e.getCapacidad()) / 100.0;
        return new OcupacionResponse(id, e.getCapacidad(), vendidas, disponibles, porcentaje);
    }

    /** Entradas no anuladas del evento. Sin "public": solo se usa dentro del paquete service. */
    long vendidas(Long eventoId) {
        return entradas.countByEventoIdAndEstadoNot(eventoId, EstadoEntrada.ANULADA);
    }

    // Pasa los datos del DTO a la entidad (lo comparten crear y modificar).
    private static void copiar(EventoRequest req, Evento e) {
        e.setNombre(req.nombre().trim());
        e.setFecha(req.fecha());
        e.setCapacidad(req.capacidad());
        e.setPrecio(req.precio());
    }
}
