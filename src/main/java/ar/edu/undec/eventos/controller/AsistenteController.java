package ar.edu.undec.eventos.controller;

import ar.edu.undec.eventos.dto.AsistenteRequest;
import ar.edu.undec.eventos.dto.EntradaResponse;
import ar.edu.undec.eventos.model.Asistente;
import ar.edu.undec.eventos.service.AsistenteService;
import ar.edu.undec.eventos.service.EntradaService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

// Endpoints de asistentes. Mismo esquema que EventoController.
@RestController
@RequestMapping("/api/asistentes")
public class AsistenteController {

    private final AsistenteService asistenteService;
    private final EntradaService entradaService;

    public AsistenteController(AsistenteService asistenteService, EntradaService entradaService) {
        this.asistenteService = asistenteService;
        this.entradaService = entradaService;
    }

    @GetMapping
    public List<Asistente> listar() {
        return asistenteService.listar();
    }

    @GetMapping("/{id}")
    public Asistente buscar(@PathVariable Long id) {
        return asistenteService.buscar(id);
    }

    // @Valid dispara las validaciones del DTO (nombre obligatorio, email válido).
    @PostMapping
    public ResponseEntity<Asistente> crear(@Valid @RequestBody AsistenteRequest req) {
        Asistente creado = asistenteService.crear(req);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}").buildAndExpand(creado.getId()).toUri();
        return ResponseEntity.created(location).body(creado);
    }

    @PutMapping("/{id}")
    public Asistente modificar(@PathVariable Long id, @Valid @RequestBody AsistenteRequest req) {
        return asistenteService.modificar(id, req);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> borrar(@PathVariable Long id) {
        asistenteService.borrar(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/entradas")
    public List<EntradaResponse> entradas(@PathVariable Long id) {
        return entradaService.porAsistente(id).stream().map(EntradaResponse::de).toList();
    }
}
