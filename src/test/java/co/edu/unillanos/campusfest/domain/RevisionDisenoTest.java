package co.edu.unillanos.campusfest.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;

/** Pruebas propias del equipo para los cambios hechos tras la revisión de diseño. */
class RevisionDisenoTest {

    private static final LocalDateTime AHORA = LocalDateTime.of(2026, 11, 20, 18, 0);
    private static final LocalDateTime FECHA_EVENTO = LocalDateTime.of(2026, 11, 28, 19, 0);

    private Evento nuevoEvento() {
        return new Evento("ev-1", "Concierto", FECHA_EVENTO, "Auditorio", 10, 0);
    }

    @Test
    void confirmarVencidaSeRechazaSinCambiarElEstado() {
        Reserva reserva = Reserva.pendiente("est-1", AHORA);

        assertThrows(IllegalStateException.class,
                () -> reserva.confirmar(AHORA.plusMinutes(10)));
        assertEquals(EstadoReserva.PENDIENTE, reserva.getEstado());
    }

    @Test
    void elEventoConfirmaUnaReservaPorSuCodigo() {
        Evento evento = nuevoEvento();
        Reserva reserva = evento.reservar("est-1", AHORA);

        Reserva confirmada = evento.confirmarReserva(reserva.getCodigo(), AHORA.plusMinutes(1));

        assertEquals(EstadoReserva.CONFIRMADA, confirmada.getEstado());
    }

    @Test
    void elEventoRegistraElIngresoPorElCodigo() {
        Evento evento = nuevoEvento();
        Reserva reserva = evento.reservar("est-1", AHORA);
        evento.confirmarReserva(reserva.getCodigo(), AHORA.plusMinutes(1));
        LocalDateTime hora = AHORA.plusDays(8);

        Asistencia asistencia = evento.registrarIngreso(reserva.getCodigo(), hora);

        assertEquals(hora, asistencia.getHora());
    }

    @Test
    void confirmarOIngresarConUnCodigoAjenoSeRechaza() {
        Evento evento = nuevoEvento();

        assertThrows(IllegalArgumentException.class,
                () -> evento.confirmarReserva("codigo-inventado", AHORA));
        assertThrows(IllegalArgumentException.class,
                () -> evento.registrarIngreso("codigo-inventado", AHORA));
    }

    @Test
    void unaFechaNulaEsUnDatoInvalido() {
        assertThrows(IllegalArgumentException.class,
                () -> new Evento("ev-1", "Concierto", null, "Auditorio", 10, 0));
    }
}
