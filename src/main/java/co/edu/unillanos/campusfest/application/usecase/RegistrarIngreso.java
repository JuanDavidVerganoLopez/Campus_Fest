package co.edu.unillanos.campusfest.application.usecase;

import co.edu.unillanos.campusfest.application.port.RepositorioEventos;
import co.edu.unillanos.campusfest.application.port.Reloj;
import co.edu.unillanos.campusfest.domain.Asistencia;
import co.edu.unillanos.campusfest.domain.Evento;

/** CU4: el lector QR es externo y solo entrega el texto del código. */
public class RegistrarIngreso {
    private final RepositorioEventos repositorio;
    private final Reloj reloj;

    public RegistrarIngreso(RepositorioEventos repositorio, Reloj reloj) {
        this.repositorio = repositorio;
        this.reloj = reloj;
    }

    public Asistencia registrar(String eventoId, String codigo) {
        Evento evento = repositorio.obtener(eventoId);
        Asistencia asistencia = evento.registrarIngreso(codigo, reloj.ahora());
        repositorio.guardar(evento);
        return asistencia;
    }
}
