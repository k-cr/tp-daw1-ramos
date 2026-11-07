package ar.edu.undec.eventos.model;

import java.util.Set;

// Estados posibles de una entrada.
public enum EstadoEntrada {
    RESERVADA, PAGADA, USADA, ANULADA;

    // Máquina de estados: dice si desde el estado actual (this) se puede pasar a "nuevo".
    // RESERVADA → PAGADA | ANULADA · PAGADA → USADA | ANULADA · USADA y ANULADA son finales.
    // Si devuelve false, el service tira 409 (transición no permitida).
    public boolean puedePasarA(EstadoEntrada nuevo) {
        return switch (this) {
            case RESERVADA -> Set.of(PAGADA, ANULADA).contains(nuevo);
            case PAGADA -> Set.of(USADA, ANULADA).contains(nuevo);
            case USADA, ANULADA -> false;
        };
    }
}
