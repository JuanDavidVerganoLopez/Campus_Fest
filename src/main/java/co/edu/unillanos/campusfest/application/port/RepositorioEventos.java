package co.edu.unillanos.campusfest.application.port;

import co.edu.unillanos.campusfest.domain.Evento;

/** Puerto de salida: el dominio no sabe dónde se guardan los eventos. */
public interface RepositorioEventos {
    Evento buscar(String eventoId);
    void guardar(Evento evento);
}
