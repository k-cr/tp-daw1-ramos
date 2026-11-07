package ar.edu.undec.eventos.model;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

// Entidad JPA: Hibernate crea la tabla EVENTO a partir de esta clase.
@Entity
public class Evento {

    // Clave primaria autoincremental (la genera la base).
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nombre;

    // Fecha y hora del evento. Se recibe/devuelve en ISO: 2026-11-20T21:00:00
    @Column(nullable = false)
    private LocalDateTime fecha;

    // Cantidad máxima de entradas no anuladas que se pueden vender.
    @Column(nullable = false)
    private Integer capacidad;

    // BigDecimal y no double para que no haya errores de redondeo con la plata.
    @Column(nullable = false)
    private BigDecimal precio;

    // No hay setId: el id lo pone la base, no el usuario.
    public Long getId() { return id; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public LocalDateTime getFecha() { return fecha; }
    public void setFecha(LocalDateTime fecha) { this.fecha = fecha; }
    public Integer getCapacidad() { return capacidad; }
    public void setCapacidad(Integer capacidad) { this.capacidad = capacidad; }
    public BigDecimal getPrecio() { return precio; }
    public void setPrecio(BigDecimal precio) { this.precio = precio; }
}
