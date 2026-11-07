package ar.edu.undec.eventos;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

// Punto de arranque de la aplicación.
// @SpringBootApplication activa la autoconfiguración y escanea los paquetes de abajo
// (controller, service, repository, etc.) para registrar los beans.
// @EnableScheduling habilita las tareas @Scheduled (la uso para anular reservas vencidas).
@SpringBootApplication
@EnableScheduling
public class EventosApplication {

	public static void main(String[] args) {
		SpringApplication.run(EventosApplication.class, args);
	}

}
