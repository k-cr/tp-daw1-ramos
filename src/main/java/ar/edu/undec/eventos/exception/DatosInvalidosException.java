package ar.edu.undec.eventos.exception;

// Se tira con datos inválidos que no cubre Bean Validation (ej: estado inexistente). Se convierte en 400.
public class DatosInvalidosException extends RuntimeException {
    public DatosInvalidosException(String mensaje) { super(mensaje); }
}
