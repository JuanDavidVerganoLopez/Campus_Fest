package co.edu.unillanos.campusfest.application.port;

import co.edu.unillanos.campusfest.domain.Evento;

/** Puerto de salida: el dominio no sabe dónde se guardan los eventos. */
public interface RepositorioEventos {
    Evento buscar(String eventoId);
    void guardar(Evento evento);

    /** Como {@link #buscar}, pero rechaza el evento inexistente (evita repetir la verificación). */
    default Evento obtener(String eventoId) {
        Evento evento = buscar(eventoId);
        if (evento == null) {
            throw new IllegalArgumentException("No existe el evento: " + eventoId);
        }
        return evento;
    }
}
