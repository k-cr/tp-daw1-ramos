package ar.edu.undec.eventos.model;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

// Una entrada une a un asistente con un evento y tiene su propio estado.
@Entity
public class Entrada {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Muchas entradas pertenecen a un evento. En la tabla queda la columna evento_id (FK).
    // EAGER: cuando traigo la entrada, traigo también el evento (lo uso para armar la respuesta).
    @ManyToOne(optional = false, fetch = FetchType.EAGER)
    private Evento evento;

    // Muchas entradas pertenecen a un asistente (columna asistente_id).
    @ManyToOne(optional = false, fetch = FetchType.EAGER)
    private Asistente asistente;

    // STRING guarda el nombre del estado ("PAGADA") y no el número de orden,
    // así si agrego estados nuevos no se rompen los datos viejos.
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EstadoEntrada estado;

    // Extra "código de entrada": UUID único que se valida en la puerta.
    @Column(nullable = false, unique = true, length = 36)
    private String codigo;

    // Se usa para la anulación automática a las 48 h.
    @Column(nullable = false)
    private LocalDateTime fechaCreacion;

    // Se completa cuando pasa a PAGADA.
    private LocalDateTime fechaPago;

    // Lo que pagó el asistente (con descuento de preventa si corresponde). Null mientras no esté pagada.
    private BigDecimal importe;

    // Constructor vacío que exige JPA. Es protected para que nadie lo use desde afuera.
    protected Entrada() { }

    // El estado inicial lo pone el sistema (RESERVADA), no viene en el POST.
    public Entrada(Evento evento, Asistente asistente) {
        this.evento = evento;
        this.asistente = asistente;
        this.estado = EstadoEntrada.RESERVADA;
        this.codigo = UUID.randomUUID().toString();
        this.fechaCreacion = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public Evento getEvento() { return evento; }
    public Asistente getAsistente() { return asistente; }
    // Se usa en la transferencia de entradas.
    public void setAsistente(Asistente asistente) { this.asistente = asistente; }
    public EstadoEntrada getEstado() { return estado; }
    public void setEstado(EstadoEntrada estado) { this.estado = estado; }
    public String getCodigo() { return codigo; }
    public LocalDateTime getFechaCreacion() { return fechaCreacion; }
    public LocalDateTime getFechaPago() { return fechaPago; }
    public void setFechaPago(LocalDateTime fechaPago) { this.fechaPago = fechaPago; }
    public BigDecimal getImporte() { return importe; }
    public void setImporte(BigDecimal importe) { this.importe = importe; }
}
