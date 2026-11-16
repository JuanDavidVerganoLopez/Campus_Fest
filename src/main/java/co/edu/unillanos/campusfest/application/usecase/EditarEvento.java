package co.edu.unillanos.campusfest.application.usecase;

import java.time.LocalDateTime;
import co.edu.unillanos.campusfest.application.port.RepositorioEventos;
import co.edu.unillanos.campusfest.application.port.Reloj;
import co.edu.unillanos.campusfest.domain.Evento;

/** CU3: coordina el cambio de un evento, sin calcular el aforo. */
public class EditarEvento {
    private final RepositorioEventos repositorio;
    private final Reloj reloj;

    public EditarEvento(RepositorioEventos repositorio, Reloj reloj) {
        this.repositorio = repositorio;
        this.reloj = reloj;
    }

    public Evento obtener(String eventoId) {
        Evento evento = repositorio.buscar(eventoId);
        if (evento == null) {
            throw new IllegalArgumentException("No existe el evento: " + eventoId);
        }
        return evento;
    }

    public void editar(String eventoId, String nombre, LocalDateTime fecha,
                       String lugar, int nuevoAforo) {
        Evento evento = obtener(eventoId);
        evento.actualizar(nombre, fecha, lugar, nuevoAforo, reloj.ahora());
        repositorio.guardar(evento);
    }
}
