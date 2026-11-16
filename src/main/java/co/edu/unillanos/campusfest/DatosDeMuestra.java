package co.edu.unillanos.campusfest;

import co.edu.unillanos.campusfest.application.port.RepositorioEventos;
import co.edu.unillanos.campusfest.application.port.Reloj;
import co.edu.unillanos.campusfest.domain.Evento;

/** Eventos iniciales de demostración; no son requisito del contrato. */
final class DatosDeMuestra {
    private DatosDeMuestra() { }

    static void cargarSiNoExisten(RepositorioEventos repositorio, Reloj reloj) {
        if (repositorio.buscar("ev-1") == null) {
            repositorio.guardar(new Evento("ev-1", "Festival de bienvenida",
                    reloj.ahora().plusDays(7), "Auditorio principal", 120, 5));
        }
        if (repositorio.buscar("ev-2") == null) {
            repositorio.guardar(new Evento("ev-2", "Feria de emprendimiento",
                    reloj.ahora().plusDays(10), "Plazoleta central", 60, 3));
        }
    }
}
