package ar.edu.undec.eventos.exception;

// Se tira cuando se rompe una regla de negocio. ManejadorErrores la convierte en 409.
public class ConflictoException extends RuntimeException {
    public ConflictoException(String mensaje) { super(mensaje); }
}
