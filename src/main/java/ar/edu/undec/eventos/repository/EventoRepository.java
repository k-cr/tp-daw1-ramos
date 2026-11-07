package ar.edu.undec.eventos.repository;

import ar.edu.undec.eventos.model.Evento;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

// Repositorio de eventos. findByNombreContainingIgnoreCase arma un LIKE %texto% sin distinguir mayúsculas (?nombre=rock).
public interface EventoRepository extends JpaRepository<Evento, Long> {
    List<Evento> findByNombreContainingIgnoreCase(String nombre);
}
