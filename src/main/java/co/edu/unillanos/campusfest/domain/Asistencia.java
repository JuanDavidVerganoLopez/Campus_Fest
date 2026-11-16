package co.edu.unillanos.campusfest.domain;

import java.time.LocalDateTime;
import java.util.Objects;

/** Marca de tiempo de un único ingreso. No depende de sensores ni de H2. */
public final class Asistencia {
    private final LocalDateTime hora;

    public Asistencia(LocalDateTime hora) {
        this.hora = Objects.requireNonNull(hora, "La hora es obligatoria");
    }

    public LocalDateTime getHora() {
        return hora;
    }

    @Override
    public boolean equals(Object otro) {
        return otro instanceof Asistencia asistencia && hora.equals(asistencia.hora);
    }

    @Override
    public int hashCode() {
        return hora.hashCode();
    }
}
