# TP Integrador · DAW1 — Sistema de venta de entradas

**Alumno/a:** Ramos, Cristian · **Legajo:** 00000
**Enunciado elegido:** eventos


## Descripción

API REST para una productora que vende entradas a sus eventos. Controla que no se vendan más
entradas que la capacidad de la sala, que cada asistente tenga una sola entrada activa por evento
y el ciclo de vida de cada entrada (reserva, pago, uso o anulación).

## Cómo ejecutarlo

Requisitos: Java 21 (Maven viene incluido con `mvnw`).

```bash
./mvnw spring-boot:run
```

La API queda en `http://localhost:8080/api`. La base H2 se guarda en `data/` (no se sube al repositorio).
Consola H2: `http://localhost:8080/h2-console`.

## Modelo

| Entidad | Atributos principales | Relaciones |
|---|---|---|
| Evento | id, nombre, fecha, capacidad, precio | — |
| Asistente | id, nombre, email | — |
| Entrada | id, estado, codigo, importe, fechaCreacion, fechaPago | N a 1 con Evento y Asistente |

Estados de Entrada: `RESERVADA → PAGADA | ANULADA`, `PAGADA → USADA | ANULADA`. `USADA` y `ANULADA` son finales.

Validaciones: `nombre` obligatorio, `fecha` futura, `capacidad` positiva, `precio` 0 o más, `email` válido.

## Endpoints

| Método | Ruta | Descripción | Respuestas |
|---|---|---|---|
| GET | /api/eventos | Lista los eventos (`?nombre=rock` filtra por nombre, sin distinguir mayúsculas) | 200 |
| GET | /api/eventos/{id} | Busca un evento por id | 200, 400, 404 |
| POST | /api/eventos | Crea un evento | 201, 400 |
| PUT | /api/eventos/{id} | Modifica un evento | 200, 400, 404, 409 |
| DELETE | /api/eventos/{id} | Borra un evento sin entradas | 204, 404, 409 |
| GET | /api/eventos/{id}/entradas | Entradas del evento | 200, 404 |
| GET | /api/eventos/{id}/ocupacion | Vendidas, disponibles y porcentaje ocupado | 200, 404 |
| GET | /api/asistentes | Lista los asistentes | 200 |
| GET | /api/asistentes/{id} | Busca un asistente por id | 200, 400, 404 |
| POST | /api/asistentes | Crea un asistente | 201, 400 |
| PUT | /api/asistentes/{id} | Modifica un asistente | 200, 400, 404 |
| DELETE | /api/asistentes/{id} | Borra un asistente sin entradas | 204, 404, 409 |
| GET | /api/asistentes/{id}/entradas | Entradas del asistente | 200, 404 |
| GET | /api/entradas | Lista las entradas | 200 |
| GET | /api/entradas/{id} | Busca una entrada por id | 200, 400, 404 |
| POST | /api/entradas | Reserva una entrada (`eventoId`, `asistenteId`); queda `RESERVADA` | 201, 400, 404, 409 |
| PATCH | /api/entradas/{id}/estado | Cambia el estado (`{"estado":"PAGADA"}`) | 200, 400, 404, 409 |
| POST | /api/entradas/validar | Valida el `codigo` en la puerta y la pasa a `USADA` | 200, 400, 404, 409 |
| PATCH | /api/entradas/{id}/transferir | Pasa una entrada pagada a otro asistente (`{"asistenteId":…}`) | 200, 400, 404, 409 |

Los errores se devuelven en formato `ProblemDetail` (`status`, `title`, `detail`); los 400 de validación
traen además el mapa `errores` (campo → mensaje). Un JSON mal formado o un id no numérico devuelven 400.

## Reglas de negocio

1. No se venden más entradas no anuladas que la capacidad del evento → 409.
2. Un asistente no puede tener dos entradas activas (no anuladas) para el mismo evento → 409.
3. Un evento que tiene entradas no se puede borrar → 409.
4. Solo se permiten las transiciones de estado indicadas arriba → 409. Un estado inexistente → 400.
5. No se puede bajar la capacidad de un evento por debajo de las entradas ya vendidas → 409.
6. Un asistente con entradas no se puede borrar → 409.

## Extras implementados

| Extra | Cómo funciona |
|---|---|
| Código de entrada | Cada entrada recibe un `codigo` UUID único. `POST /api/entradas/validar` la pasa a `USADA`; 409 si no está pagada o ya se usó, 404 si el código no existe. |
| Preventa | Al pasar a `PAGADA` se guarda `fechaPago` e `importe`: si se paga más de 7 días antes del evento, el importe es 20 % menor que el precio. |
| Ocupación | `GET /api/eventos/{id}/ocupacion` devuelve `vendidas`, `disponibles` y `porcentajeOcupado`. |
| Transferencia | `PATCH /api/entradas/{id}/transferir` cambia el asistente de una entrada `PAGADA`, respetando la regla 2. |
| Anulación automática | Una tarea `@Scheduled` (cada minuto) y cada consulta de entradas anulan las `RESERVADA` con más de 48 h sin pagar, lo que libera el lugar. |

## Pruebas

En `pruebas.http` están todas las peticiones de ejemplo (se ejecutan desde IntelliJ).
