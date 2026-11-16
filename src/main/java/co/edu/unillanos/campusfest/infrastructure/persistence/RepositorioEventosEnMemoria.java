package co.edu.unillanos.campusfest.infrastructure.persistence;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import co.edu.unillanos.campusfest.application.port.RepositorioEventos;
import co.edu.unillanos.campusfest.domain.Evento;
import co.edu.unillanos.campusfest.domain.Reserva;

/**
 * Adaptador simple para pruebas o trabajo local, sin acceso a H2.
 * Guarda y entrega copias, como H2: un cambio solo persiste si se llama a guardar.
 */
public class RepositorioEventosEnMemoria implements RepositorioEventos {
    private final Map<String, Evento> eventos = new HashMap<>();

    public RepositorioEventosEnMemoria() { }

    @Override
    public Evento buscar(String eventoId) {
        Evento guardado = eventos.get(eventoId);
        return guardado == null ? null : copiar(guardado);
    }

    @Override
    public void guardar(Evento evento) {
        eventos.put(evento.getId(), copiar(evento));
    }

    private static Evento copiar(Evento origen) {
        List<Reserva> reservas = new ArrayList<>();
        for (Reserva reserva : origen.getReservas()) {
            reservas.add(Reserva.reconstruir(reserva.getCodigo(), reserva.getEstudianteId(),
                    reserva.getVenceEn(), reserva.getEstado(), reserva.getAsistencia()));
        }
        return new Evento(origen.getId(), origen.getNombre(), origen.getFecha(),
                origen.getLugar(), origen.getAforo(), origen.getBloqueados(), reservas);
    }
}
