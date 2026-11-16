package co.edu.unillanos.campusfest.application.port;

import java.time.LocalDateTime;

/** Puerto de tiempo intercambiable por un reloj fijo durante las pruebas. */
public interface Reloj {
    LocalDateTime ahora();
}
