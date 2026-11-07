package ar.edu.undec.eventos.exception;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.util.LinkedHashMap;
import java.util.Map;

// Manejo global de errores: cualquier excepción que salga de un controller cae acá
// y se convierte en una respuesta ProblemDetail (status, title, detail).
// Extiende ResponseEntityExceptionHandler, que ya maneja los errores estándar de Spring
// (método no permitido, ruta inexistente, etc.) en formato ProblemDetail.
@RestControllerAdvice
public class ManejadorErrores extends ResponseEntityExceptionHandler {

    // Algo que se buscó por id no existe → 404.
    @ExceptionHandler(NoEncontradoException.class)
    public ProblemDetail noEncontrado(NoEncontradoException ex) {
        return problema(HttpStatus.NOT_FOUND, "No encontrado", ex.getMessage());
    }

    // Se rompió una regla de negocio → 409.
    @ExceptionHandler(ConflictoException.class)
    public ProblemDetail conflicto(ConflictoException ex) {
        return problema(HttpStatus.CONFLICT, "Conflicto", ex.getMessage());
    }

    // Datos inválidos que no detecta Bean Validation (ej: un estado que no existe) → 400.
    @ExceptionHandler(DatosInvalidosException.class)
    public ProblemDetail datosInvalidos(DatosInvalidosException ex) {
        return problema(HttpStatus.BAD_REQUEST, "Datos inválidos", ex.getMessage());
    }

    // Un parámetro de la URL no se puede convertir (ej: /api/eventos/abc cuando se espera un número) → 400.
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ProblemDetail tipoInvalido(MethodArgumentTypeMismatchException ex) {
        return problema(HttpStatus.BAD_REQUEST, "Parámetro inválido",
                "El valor '" + ex.getValue() + "' no es válido para '" + ex.getName() + "'");
    }

    // Restricción de la base (ej: código de entrada repetido) → 409 en vez de 500.
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ProblemDetail integridad(DataIntegrityViolationException ex) {
        return problema(HttpStatus.CONFLICT, "Conflicto", "La operación viola una restricción de datos");
    }

    // Falló @Valid en un @RequestBody → 400 con el mapa "errores" (campo → mensaje).
    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex,
            HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        // LinkedHashMap mantiene el orden en que aparecen los errores.
        Map<String, String> errores = new LinkedHashMap<>();
        for (FieldError fe : ex.getBindingResult().getFieldErrors()) {
            // putIfAbsent: si un campo tiene dos errores, me quedo con el primero.
            errores.putIfAbsent(fe.getField(), fe.getDefaultMessage());
        }
        ProblemDetail pd = problema(HttpStatus.BAD_REQUEST, "Datos inválidos", "La petición tiene errores");
        // setProperty agrega un campo extra al JSON del ProblemDetail.
        pd.setProperty("errores", errores);
        return ResponseEntity.badRequest().body(pd);
    }

    // JSON mal formado (o un tipo que no se puede leer) → 400, nunca 500.
    @Override
    protected ResponseEntity<Object> handleHttpMessageNotReadable(HttpMessageNotReadableException ex,
            HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        return ResponseEntity.badRequest().body(
                problema(HttpStatus.BAD_REQUEST, "JSON inválido", "El cuerpo de la petición no se pudo leer"));
    }

    // Método de ayuda para no repetir el armado del ProblemDetail.
    private static ProblemDetail problema(HttpStatus status, String titulo, String detalle) {
        ProblemDetail pd = ProblemDetail.forStatusAndDetail(status, detalle);
        pd.setTitle(titulo);
        return pd;
    }
}
