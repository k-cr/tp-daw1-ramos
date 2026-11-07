package ar.edu.undec.eventos.service;

import ar.edu.undec.eventos.dto.EntradaRequest;
import ar.edu.undec.eventos.exception.ConflictoException;
import ar.edu.undec.eventos.exception.DatosInvalidosException;
import ar.edu.undec.eventos.exception.NoEncontradoException;
import ar.edu.undec.eventos.model.Asistente;
import ar.edu.undec.eventos.model.Entrada;
import ar.edu.undec.eventos.model.EstadoEntrada;
import ar.edu.undec.eventos.model.Evento;
import ar.edu.undec.eventos.repository.EntradaRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

// El corazón del sistema: reservas, reglas de capacidad y duplicados, cambios de estado y extras.
// Antes de cada consulta llamo a anularVencidas() para que las reservas vencidas liberen lugar al toque.
@Service
public class EntradaService {

    /** Una reserva sin pagar se anula sola pasado este plazo. */
    static final int HORAS_PARA_PAGAR = 48;
    /** Pagar con más de estos días de anticipación al evento da descuento de preventa. */
    static final int DIAS_PREVENTA = 7;
    static final BigDecimal FACTOR_PREVENTA = new BigDecimal("0.80");

    private final EntradaRepository entradas;
    private final EventoService eventoService;
    private final AsistenteService asistenteService;

    public EntradaService(EntradaRepository entradas, EventoService eventoService, AsistenteService asistenteService) {
        this.entradas = entradas;
        this.eventoService = eventoService;
        this.asistenteService = asistenteService;
    }

    @Transactional
    public List<Entrada> listar() {
        anularVencidas();
        return entradas.findAll();
    }

    @Transactional
    public Entrada buscar(Long id) {
        anularVencidas();
        return entradas.findById(id)
                .orElseThrow(() -> new NoEncontradoException("No existe la entrada " + id));
    }

    @Transactional
    public List<Entrada> porEvento(Long eventoId) {
        eventoService.buscar(eventoId);
        anularVencidas();
        return entradas.findByEventoId(eventoId);
    }

    @Transactional
    public List<Entrada> porAsistente(Long asistenteId) {
        asistenteService.buscar(asistenteId);
        anularVencidas();
        return entradas.findByAsistenteId(asistenteId);
    }

    @Transactional
    public Entrada crear(EntradaRequest req) {
        // 1) Si el evento o el asistente no existen → 404.
        Evento evento = eventoService.buscar(req.eventoId());
        Asistente asistente = asistenteService.buscar(req.asistenteId());
        anularVencidas();

        // 2) Regla 2: una sola entrada activa por asistente y evento → 409.
        verificarSinEntradaActiva(evento.getId(), asistente.getId());
        // 3) Regla 1: si las no anuladas ya llegan a la capacidad, no hay lugar → 409.
        if (eventoService.vendidas(evento.getId()) >= evento.getCapacidad()) {
            throw new ConflictoException("No quedan entradas para el evento " + evento.getId());
        }
        return entradas.save(new Entrada(evento, asistente));
    }

    // PATCH /estado. No llamo a save(): la entidad está "managed" dentro de la transacción,
    // así que JPA guarda los cambios solo al terminar el método (dirty checking).
    @Transactional
    public Entrada cambiarEstado(Long id, String estadoTexto) {
        EstadoEntrada nuevo = parsearEstado(estadoTexto); // 400 si el estado no existe
        Entrada e = buscar(id);                           // 404 si la entrada no existe
        aplicarTransicion(e, nuevo);                      // 409 si la transición no vale
        return e;
    }

    /** Extra: valida el código en la puerta y pasa la entrada a USADA. */
    @Transactional
    public Entrada validar(String codigo) {
        anularVencidas();
        Entrada e = entradas.findByCodigo(codigo.trim())
                .orElseThrow(() -> new NoEncontradoException("No existe una entrada con ese código"));
        if (e.getEstado() == EstadoEntrada.USADA) {
            throw new ConflictoException("La entrada ya fue usada");
        }
        if (e.getEstado() != EstadoEntrada.PAGADA) {
            throw new ConflictoException("La entrada no está pagada (estado " + e.getEstado() + ")");
        }
        e.setEstado(EstadoEntrada.USADA);
        return e;
    }

    /** Extra: pasa una entrada pagada a otro asistente (respetando la regla 2). */
    @Transactional
    public Entrada transferir(Long id, Long asistenteId) {
        Entrada e = buscar(id);
        Asistente destino = asistenteService.buscar(asistenteId);
        if (e.getEstado() != EstadoEntrada.PAGADA) {
            throw new ConflictoException("Solo se puede transferir una entrada PAGADA");
        }
        // equals y no ==: son Long (objetos), con == compararía referencias.
        if (e.getAsistente().getId().equals(destino.getId())) {
            throw new ConflictoException("La entrada ya pertenece a ese asistente");
        }
        verificarSinEntradaActiva(e.getEvento().getId(), destino.getId());
        e.setAsistente(destino);
        return e;
    }

    /** Extra: las reservas sin pagar en 48 h se anulan y liberan el lugar (cada minuto y al consultar). */
    // fixedRate = 60_000 ms → corre cada 1 minuto (necesita @EnableScheduling en la clase principal).
    @Scheduled(fixedRate = 60_000)
    @Transactional
    public void anularVencidas() {
        LocalDateTime limite = LocalDateTime.now().minusHours(HORAS_PARA_PAGAR);
        entradas.findByEstadoAndFechaCreacionBefore(EstadoEntrada.RESERVADA, limite)
                .forEach(e -> e.setEstado(EstadoEntrada.ANULADA));
    }

    private void aplicarTransicion(Entrada e, EstadoEntrada nuevo) {
        // La tabla de transiciones está en el enum EstadoEntrada.
        if (!e.getEstado().puedePasarA(nuevo)) {
            throw new ConflictoException("No se puede pasar de " + e.getEstado() + " a " + nuevo);
        }
        if (nuevo == EstadoEntrada.PAGADA) {
            LocalDateTime ahora = LocalDateTime.now();
            e.setFechaPago(ahora);
            e.setImporte(calcularImporte(e.getEvento(), ahora));
        }
        e.setEstado(nuevo);
    }

    /** Extra preventa: pagando más de 7 días antes del evento, 20 % menos. */
    static BigDecimal calcularImporte(Evento evento, LocalDateTime fechaPago) {
        BigDecimal precio = evento.getPrecio();
        // Si pago + 7 días todavía es antes del evento → pagó con más de 7 días de anticipación.
        if (fechaPago.plusDays(DIAS_PREVENTA).isBefore(evento.getFecha())) {
            precio = precio.multiply(FACTOR_PREVENTA);
        }
        // 2 decimales, redondeo comercial (0,5 para arriba).
        return precio.setScale(2, RoundingMode.HALF_UP);
    }

    private void verificarSinEntradaActiva(Long eventoId, Long asistenteId) {
        if (entradas.existsByEventoIdAndAsistenteIdAndEstadoNot(eventoId, asistenteId, EstadoEntrada.ANULADA)) {
            throw new ConflictoException("El asistente ya tiene una entrada activa para este evento");
        }
    }

    // valueOf tira IllegalArgumentException si el texto no es un estado del enum → lo paso a 400.
    // toUpperCase para aceptar también "pagada".
    private static EstadoEntrada parsearEstado(String texto) {
        try {
            return EstadoEntrada.valueOf(texto.trim().toUpperCase());
        } catch (IllegalArgumentException ex) {
            throw new DatosInvalidosException("Estado inválido: " + texto
                    + ". Valores posibles: " + Arrays.toString(EstadoEntrada.values()));
        }
    }
}
