package ar.edu.undec.eventos.model;

import jakarta.persistence.*;

// Persona que compra entradas. No tiene la lista de entradas adentro:
// la relación se guarda del lado de Entrada (ManyToOne), así se evitan ciclos al pasar a JSON.
@Entity
public class Asistente {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nombre;

    // El formato del email se valida en AsistenteRequest con @Email.
    @Column(nullable = false)
    private String email;

    public Long getId() { return id; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
}
