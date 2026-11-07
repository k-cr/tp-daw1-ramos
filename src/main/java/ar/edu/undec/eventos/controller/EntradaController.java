package ar.edu.undec.eventos.controller;

import ar.edu.undec.eventos.dto.CodigoRequest;
import ar.edu.undec.eventos.dto.EntradaRequest;
import ar.edu.undec.eventos.dto.EntradaResponse;
import ar.edu.undec.eventos.dto.EstadoRequest;
import ar.edu.undec.eventos.dto.TransferenciaRequest;
import ar.edu.undec.eventos.service.EntradaService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

// Endpoints de entradas. Siempre devuelvo EntradaResponse (DTO), nunca la entidad.
// No hay PUT ni DELETE: una entrada no se edita ni se borra, se anula con PATCH /estado.
@RestController
@RequestMapping("/api/entradas")
public class EntradaController {

    private final EntradaService entradaService;

    public EntradaController(EntradaService entradaService) {
        this.entradaService = entradaService;
    }

    @GetMapping
    public List<EntradaResponse> listar() {
        return entradaService.listar().stream().map(EntradaResponse::de).toList();
    }

    @GetMapping("/{id}")
    public EntradaResponse buscar(@PathVariable Long id) {
        return EntradaResponse.de(entradaService.buscar(id));
    }

    @PostMapping
    public ResponseEntity<EntradaResponse> crear(@Valid @RequestBody EntradaRequest req) {
        EntradaResponse creada = EntradaResponse.de(entradaService.crear(req));
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}").buildAndExpand(creada.id()).toUri();
        return ResponseEntity.created(location).body(creada);
    }

    // PATCH porque modifica una sola parte del recurso (el estado).
    // {} sin estado → @NotBlank falla → 400.
    @PatchMapping("/{id}/estado")
    public EntradaResponse cambiarEstado(@PathVariable Long id, @Valid @RequestBody EstadoRequest req) {
        return EntradaResponse.de(entradaService.cambiarEstado(id, req.estado()));
    }

    // Extra: /validar no choca con /{id} porque es POST, y POST /{id} no existe.
    @PostMapping("/validar")
    public EntradaResponse validar(@Valid @RequestBody CodigoRequest req) {
        return EntradaResponse.de(entradaService.validar(req.codigo()));
    }

    @PatchMapping("/{id}/transferir")
    public EntradaResponse transferir(@PathVariable Long id, @Valid @RequestBody TransferenciaRequest req) {
        return EntradaResponse.de(entradaService.transferir(id, req.asistenteId()));
    }
}
