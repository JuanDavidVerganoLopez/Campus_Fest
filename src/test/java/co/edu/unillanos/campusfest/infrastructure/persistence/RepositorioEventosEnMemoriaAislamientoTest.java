package co.edu.unillanos.campusfest.infrastructure.persistence;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;

import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;

import co.edu.unillanos.campusfest.application.port.RepositorioEventos;
import co.edu.unillanos.campusfest.domain.Evento;

/** El adaptador en memoria entrega copias, igual que H2: sin guardar no hay cambio. */
class RepositorioEventosEnMemoriaAislamientoTest {

    private static final LocalDateTime AHORA = LocalDateTime.of(2026, 11, 20, 18, 0);
    private static final LocalDateTime FECHA = LocalDateTime.of(2026, 11, 28, 19, 0);

    @Test
    void buscarEntregaUnaCopiaNueva() {
        RepositorioEventos repositorio = new RepositorioEventosEnMemoria();
        repositorio.guardar(new Evento("ev-1", "Concierto", FECHA, "Auditorio", 10, 0));

        assertNotSame(repositorio.buscar("ev-1"), repositorio.buscar("ev-1"));
    }

    @Test
    void unCambioSinGuardarNoPersiste() {
        RepositorioEventos repositorio = new RepositorioEventosEnMemoria();
        repositorio.guardar(new Evento("ev-1", "Concierto", FECHA, "Auditorio", 10, 0));

        repositorio.buscar("ev-1").reservar("est-1", AHORA);

        assertEquals(10, repositorio.buscar("ev-1").cuposDisponibles(AHORA));
    }

    @Test
    void unCambioGuardadoPersisteConSusReservas() {
        RepositorioEventos repositorio = new RepositorioEventosEnMemoria();
        repositorio.guardar(new Evento("ev-1", "Concierto", FECHA, "Auditorio", 10, 0));

        Evento evento = repositorio.buscar("ev-1");
        evento.reservar("est-1", AHORA);
        repositorio.guardar(evento);

        assertEquals(9, repositorio.buscar("ev-1").cuposDisponibles(AHORA));
        assertEquals(1, repositorio.buscar("ev-1").getReservas().size());
    }
}
