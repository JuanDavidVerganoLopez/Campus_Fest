package co.edu.unillanos.campusfest.domain;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/** Agregado del evento: controla aforo y crea reservas (GRASP Experto/Creador). */
public class Evento {
    private final String id;
    private String nombre;
    private LocalDateTime fecha;
    private String lugar;
    private int aforo;
    private final int bloqueados;
    private final List<Reserva> reservas;

    public Evento(String id, String nombre, LocalDateTime fecha, String lugar,
                  int aforo, int bloqueados) {
        this(id, nombre, fecha, lugar, aforo, bloqueados, List.of());
    }

    /** Segundo constructor utilizado por H2 al reconstruir el agregado. */
    public Evento(String id, String nombre, LocalDateTime fecha, String lugar,
                  int aforo, int bloqueados, List<Reserva> reservas) {
        validarTexto(id, "El identificador del evento es obligatorio");
        validarDatos(nombre, fecha, lugar);
        if (bloqueados < 0 || aforo < 0 || bloqueados > aforo) {
            throw new IllegalArgumentException("El aforo debe cubrir los cupos bloqueados");
        }
        this.id = id;
        this.nombre = nombre;
        this.fecha = fecha;
        this.lugar = lugar;
        this.aforo = aforo;
        this.bloqueados = bloqueados;
        this.reservas = new ArrayList<>(Objects.requireNonNull(reservas, "Reservas obligatorias"));
    }

    public int cuposDisponibles(LocalDateTime ahora) {
        return aforo - bloqueados - cuposOcupadosPorReservas(ahora);
    }

    private int cuposOcupadosPorReservas(LocalDateTime ahora) {
        int ocupados = 0;
        for (Reserva reserva : reservas) {
            if (reserva.ocupaCupo(ahora)) {
                ocupados++;
            }
        }
        return ocupados;
    }

    public Reserva reservar(String estudianteId, LocalDateTime ahora) {
        if (cuposDisponibles(ahora) <= 0) {
            throw new IllegalStateException("No hay cupos disponibles para este evento");
        }
        Reserva reserva = Reserva.pendiente(estudianteId, ahora);
        reservas.add(reserva);
        return reserva;
    }

    public Reserva buscarReservaPorCodigo(String codigo) {
        for (Reserva reserva : reservas) {
            if (reserva.getCodigo().equals(codigo)) {
                return reserva;
            }
        }
        throw new IllegalArgumentException("El código no corresponde a una reserva de este evento");
    }

    public void cambiarDatos(String nombre, LocalDateTime fecha, String lugar) {
        validarDatos(nombre, fecha, lugar);
        this.nombre = nombre;
        this.fecha = fecha;
        this.lugar = lugar;
    }

    /** No cambia el aforo si el resultado dejaría reservas o bloqueados sin cupo. */
    public void cambiarAforo(int nuevoAforo, LocalDateTime ahora) {
        int ocupados = cuposOcupadosPorReservas(ahora);
        int minimo = bloqueados + ocupados;
        if (nuevoAforo < minimo) {
            throw new IllegalStateException("Hay " + ocupados
                    + " cupos ocupados por reservas y " + bloqueados
                    + " bloqueados: el aforo no puede ser menor que " + minimo);
        }
        this.aforo = nuevoAforo;
    }

    /** Cambio completo y atómico para que un formulario inválido no deje cambios parciales. */
    public void actualizar(String nombre, LocalDateTime fecha, String lugar,
                           int nuevoAforo, LocalDateTime ahora) {
        validarDatos(nombre, fecha, lugar);
        cambiarAforo(nuevoAforo, ahora);
        cambiarDatos(nombre, fecha, lugar);
    }

    private static void validarDatos(String nombre, LocalDateTime fecha, String lugar) {
        validarTexto(nombre, "El nombre del evento es obligatorio");
        Objects.requireNonNull(fecha, "La fecha del evento es obligatoria");
        validarTexto(lugar, "El lugar del evento es obligatorio");
    }

    private static void validarTexto(String valor, String mensaje) {
        if (valor == null || valor.isBlank()) {
            throw new IllegalArgumentException(mensaje);
        }
    }

    public String getId() { return id; }
    public String getNombre() { return nombre; }
    public LocalDateTime getFecha() { return fecha; }
    public String getLugar() { return lugar; }
    public int getAforo() { return aforo; }
    public int getBloqueados() { return bloqueados; }
    public List<Reserva> getReservas() { return List.copyOf(reservas); }
}
