package co.edu.unillanos.campusfest.domain;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

/** Experto en vigencia, estado, cupos e ingreso de una reserva. */
public class Reserva {
    private static final int MINUTOS_DE_VIGENCIA = 10;

    private final String codigo;
    private final String estudianteId;
    private final LocalDateTime venceEn;
    private EstadoReserva estado;
    private Asistencia asistencia;

    private Reserva(String codigo, String estudianteId, LocalDateTime venceEn,
                    EstadoReserva estado, Asistencia asistencia) {
        this.codigo = Objects.requireNonNull(codigo, "El código es obligatorio");
        this.estudianteId = Objects.requireNonNull(estudianteId, "El estudiante es obligatorio");
        this.venceEn = Objects.requireNonNull(venceEn, "La fecha límite es obligatoria");
        this.estado = Objects.requireNonNull(estado, "El estado es obligatorio");
        this.asistencia = asistencia;
    }

    public static Reserva pendiente(String estudianteId, LocalDateTime ahora) {
        if (estudianteId == null || estudianteId.isBlank()) {
            throw new IllegalArgumentException("El estudiante es obligatorio");
        }
        Objects.requireNonNull(ahora, "La hora es obligatoria");
        return new Reserva(UUID.randomUUID().toString(), estudianteId,
                ahora.plusMinutes(MINUTOS_DE_VIGENCIA), EstadoReserva.PENDIENTE, null);
    }

    /** Reconstitución desde almacenamiento: preserva fielmente el estado persistido. */
    public static Reserva reconstruir(String codigo, String estudianteId,
                                      LocalDateTime venceEn, EstadoReserva estado,
                                      Asistencia asistencia) {
        return new Reserva(codigo, estudianteId, venceEn, estado, asistencia);
    }

    public boolean ocupaCupo(LocalDateTime ahora) {
        Objects.requireNonNull(ahora, "La hora es obligatoria");
        if (estado == EstadoReserva.CONFIRMADA) {
            return true;
        }
        return estado == EstadoReserva.PENDIENTE && ahora.isBefore(venceEn);
    }

    public void confirmar(LocalDateTime ahora) {
        Objects.requireNonNull(ahora, "La hora es obligatoria");
        if (estado != EstadoReserva.PENDIENTE) {
            throw new IllegalStateException("Solo una reserva pendiente puede confirmarse");
        }
        if (!ahora.isBefore(venceEn)) {
            // Rechazo sin efectos: el estado no cambia; ocupaCupo(ahora) ya libera el cupo.
            throw new IllegalStateException("La reserva venció: el plazo de "
                    + MINUTOS_DE_VIGENCIA + " minutos terminó");
        }
        estado = EstadoReserva.CONFIRMADA;
    }

    public void cancelar() {
        if (estado != EstadoReserva.PENDIENTE && estado != EstadoReserva.CONFIRMADA) {
            throw new IllegalStateException("No es posible cancelar esta reserva");
        }
        estado = EstadoReserva.CANCELADA;
    }

    public Asistencia registrarIngreso(LocalDateTime ahora) {
        Objects.requireNonNull(ahora, "La hora es obligatoria");
        if (estado != EstadoReserva.CONFIRMADA) {
            throw new IllegalStateException("La reserva no está confirmada");
        }
        if (asistencia != null) {
            throw new IllegalStateException("El ingreso ya fue registrado para esta reserva");
        }
        asistencia = new Asistencia(ahora);
        return asistencia;
    }

    public String getCodigo() { return codigo; }
    public String getEstudianteId() { return estudianteId; }
    public LocalDateTime getVenceEn() { return venceEn; }
    public EstadoReserva getEstado() { return estado; }
    public Asistencia getAsistencia() { return asistencia; }
}
