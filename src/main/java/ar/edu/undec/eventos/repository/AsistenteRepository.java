package ar.edu.undec.eventos.repository;

import ar.edu.undec.eventos.model.Asistente;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AsistenteRepository extends JpaRepository<Asistente, Long> {

    // Confirma que dos asistentes no tengan el mismo email
    boolean existsByEmail(String email);
}
