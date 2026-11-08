package ar.edu.undec.eventos.repository;

import ar.edu.undec.eventos.model.Evento;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

// Repositorio de eventos.
public interface EventoRepository extends JpaRepository<Evento, Long> {
    // findByNombreContainingIgnoreCase arma un LIKE %texto% sin distinguir mayúsculas (?nombre=rock).
    List<Evento> findByNombreContainingIgnoreCase(String nombre);

    // Busca por eventos futuros
    List<Evento> findByFechaAfter(LocalDateTime fecha);
}
