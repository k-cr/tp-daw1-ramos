package ar.edu.undec.eventos.exception;

// Se tira cuando algo buscado por id no existe. ManejadorErrores la convierte en 404.
public class NoEncontradoException extends RuntimeException {
    // RuntimeException (unchecked): no hace falta declararla con throws en cada método.
    public NoEncontradoException(String mensaje) { super(mensaje); }
}
