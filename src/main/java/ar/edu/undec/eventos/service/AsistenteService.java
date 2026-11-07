package ar.edu.undec.eventos.service;

import ar.edu.undec.eventos.dto.AsistenteRequest;
import ar.edu.undec.eventos.exception.ConflictoException;
import ar.edu.undec.eventos.exception.NoEncontradoException;
import ar.edu.undec.eventos.model.Asistente;
import ar.edu.undec.eventos.repository.AsistenteRepository;
import ar.edu.undec.eventos.repository.EntradaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

// Lógica de asistentes: CRUD simple + no dejar borrar a alguien con entradas.
@Service
public class AsistenteService {

    private final AsistenteRepository asistentes;
    private final EntradaRepository entradas;

    public AsistenteService(AsistenteRepository asistentes, EntradaRepository entradas) {
        this.asistentes = asistentes;
        this.entradas = entradas;
    }

    public List<Asistente> listar() {
        return asistentes.findAll();
    }

    public Asistente buscar(Long id) {
        return asistentes.findById(id)
                .orElseThrow(() -> new NoEncontradoException("No existe el asistente " + id));
    }

    @Transactional
    public Asistente crear(AsistenteRequest req) {
        Asistente a = new Asistente();
        copiar(req, a);
        return asistentes.save(a);
    }

    @Transactional
    public Asistente modificar(Long id, AsistenteRequest req) {
        Asistente a = buscar(id);
        copiar(req, a);
        return asistentes.save(a);
    }

    @Transactional
    public void borrar(Long id) {
        Asistente a = buscar(id);
        // Si lo borro, sus entradas quedarían apuntando a nada (y la FK no lo deja) → 409.
        if (entradas.existsByAsistenteId(id)) {
            throw new ConflictoException("El asistente tiene entradas y no se puede borrar");
        }
        asistentes.delete(a);
    }

    // trim() saca espacios de más al principio y al final.
    private static void copiar(AsistenteRequest req, Asistente a) {
        a.setNombre(req.nombre().trim());
        a.setEmail(req.email().trim());
    }
}
