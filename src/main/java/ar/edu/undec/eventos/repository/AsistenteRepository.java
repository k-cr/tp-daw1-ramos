package ar.edu.undec.eventos.repository;

import ar.edu.undec.eventos.model.Asistente;
import org.springframework.data.jpa.repository.JpaRepository;

// Repositorio de asistentes: con lo que trae JpaRepository (save, findById, findAll, delete) alcanza.
public interface AsistenteRepository extends JpaRepository<Asistente, Long> { }
