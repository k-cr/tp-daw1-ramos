package ar.edu.undec.eventos.controller;

import ar.edu.undec.eventos.dto.EntradaResponse;
import ar.edu.undec.eventos.dto.EventoRequest;
import ar.edu.undec.eventos.dto.OcupacionResponse;
import ar.edu.undec.eventos.model.Evento;
import ar.edu.undec.eventos.service.EntradaService;
import ar.edu.undec.eventos.service.EventoService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

// @RestController = @Controller + @ResponseBody: lo que devuelvo se convierte en JSON.
// @RequestMapping pone el prefijo /api/eventos a todos los métodos.
// El controller no tiene lógica: valida con @Valid, llama al service y arma la respuesta HTTP.
@RestController
@RequestMapping("/api/eventos")
public class EventoController {

    private final EventoService eventoService;
    private final EntradaService entradaService;

    public EventoController(EventoService eventoService, EntradaService entradaService) {
        this.eventoService = eventoService;
        this.entradaService = entradaService;
    }

    // GET /api/eventos
    // GET /api/eventos?nombre=rock (required = false: el filtro es opcional).
    // GET /api/eventos?futuros=true (required = false: el filtro es opcional).
    @GetMapping
    public List<Evento> listar(
            @RequestParam(required = false) String nombre,
            @RequestParam(required = false) Boolean futuros
    ) {
        return eventoService.listar(nombre, futuros);
    }

    // @PathVariable toma el {id} de la URL. Si no es número → 400 (lo maneja ManejadorErrores).
    @GetMapping("/{id}")
    public Evento buscar(@PathVariable Long id) {
        return eventoService.buscar(id);
    }

    @PostMapping
    public ResponseEntity<Evento> crear(@Valid @RequestBody EventoRequest req) {
        Evento creado = eventoService.crear(req);
        // Armo el header Location: URL actual + /{id} → http://localhost:8080/api/eventos/5
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}").buildAndExpand(creado.getId()).toUri();
        return ResponseEntity.created(location).body(creado); // 201 + Location + objeto creado
    }

    @PutMapping("/{id}")
    public Evento modificar(@PathVariable Long id, @Valid @RequestBody EventoRequest req) {
        return eventoService.modificar(id, req);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> borrar(@PathVariable Long id) {
        eventoService.borrar(id);
        return ResponseEntity.noContent().build(); // 204 sin cuerpo
    }

    // Convierto cada entidad Entrada en EntradaResponse con stream + map.
    @GetMapping("/{id}/entradas")
    public List<EntradaResponse> entradas(@PathVariable Long id) {
        return entradaService.porEvento(id).stream().map(EntradaResponse::de).toList();
    }

    @GetMapping("/{id}/ocupacion")
    public OcupacionResponse ocupacion(@PathVariable Long id) {
        return eventoService.ocupacion(id);
    }
}
