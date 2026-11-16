package co.edu.unillanos.campusfest.infrastructure.persistence;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import co.edu.unillanos.campusfest.application.port.RepositorioEventos;
import co.edu.unillanos.campusfest.domain.Asistencia;
import co.edu.unillanos.campusfest.domain.EstadoReserva;
import co.edu.unillanos.campusfest.domain.Evento;
import co.edu.unillanos.campusfest.domain.Reserva;

/** Adaptador H2: JDBC y SQL no entran a domain/application. */
public class RepositorioEventosH2 implements RepositorioEventos {
    private final Connection conexion;

    public RepositorioEventosH2(Connection conexion) {
        this.conexion = Objects.requireNonNull(conexion, "La conexión es obligatoria");
        inicializarEsquema();
    }

    private void inicializarEsquema() {
        try (InputStream archivo = getClass().getResourceAsStream("/db/schema.sql")) {
            if (archivo == null) {
                throw new IllegalStateException("No se encontró db/schema.sql");
            }
            String sql = new String(archivo.readAllBytes(), StandardCharsets.UTF_8);
            StringBuilder limpio = new StringBuilder();
            for (String linea : sql.split("\\R")) {
                if (!linea.trim().startsWith("--")) {
                    limpio.append(linea).append('\n');
                }
            }
            try (Statement sentencia = conexion.createStatement()) {
                for (String instruccion : limpio.toString().split(";")) {
                    if (!instruccion.isBlank()) {
                        sentencia.execute(instruccion.trim());
                    }
                }
            }
        } catch (SQLException | IOException error) {
            throw new IllegalStateException("No se pudo crear el esquema H2", error);
        }
    }

    @Override
    public Evento buscar(String eventoId) {
        String sqlEvento = "SELECT ID, NOMBRE, FECHA, LUGAR, AFORO, BLOQUEADOS FROM EVENTO WHERE ID = ?";
        try (PreparedStatement consulta = conexion.prepareStatement(sqlEvento)) {
            consulta.setString(1, eventoId);
            try (ResultSet filas = consulta.executeQuery()) {
                if (!filas.next()) {
                    return null;
                }
                String id = filas.getString("ID");
                String nombre = filas.getString("NOMBRE");
                LocalDateTime fecha = filas.getTimestamp("FECHA").toLocalDateTime();
                String lugar = filas.getString("LUGAR");
                int aforo = filas.getInt("AFORO");
                int bloqueados = filas.getInt("BLOQUEADOS");
                return new Evento(id, nombre, fecha, lugar, aforo, bloqueados, buscarReservas(id));
            }
        } catch (SQLException error) {
            throw new IllegalStateException("No se pudo consultar el evento H2", error);
        }
    }

    private List<Reserva> buscarReservas(String eventoId) throws SQLException {
        String sql = "SELECT R.CODIGO, R.ESTUDIANTE_ID, R.VENCE_EN, R.ESTADO, A.HORA "
                + "FROM RESERVA R LEFT JOIN ASISTENCIA A ON A.CODIGO_RESERVA = R.CODIGO "
                + "WHERE R.EVENTO_ID = ? ORDER BY R.CODIGO";
        List<Reserva> reservas = new ArrayList<>();
        try (PreparedStatement consulta = conexion.prepareStatement(sql)) {
            consulta.setString(1, eventoId);
            try (ResultSet filas = consulta.executeQuery()) {
                while (filas.next()) {
                    Timestamp hora = filas.getTimestamp("HORA");
                    Asistencia asistencia = hora == null ? null : new Asistencia(hora.toLocalDateTime());
                    reservas.add(Reserva.reconstruir(filas.getString("CODIGO"),
                            filas.getString("ESTUDIANTE_ID"),
                            filas.getTimestamp("VENCE_EN").toLocalDateTime(),
                            EstadoReserva.valueOf(filas.getString("ESTADO")), asistencia));
                }
            }
        }
        return reservas;
    }

    /** Guarda el agregado entero en una transacción, sin duplicar hijos. */
    @Override
    public void guardar(Evento evento) {
        Objects.requireNonNull(evento, "El evento es obligatorio");
        boolean autoCommitOriginal;
        try {
            autoCommitOriginal = conexion.getAutoCommit();
        } catch (SQLException error) {
            throw new IllegalStateException("No se pudo consultar la transacción H2", error);
        }
        try {
            conexion.setAutoCommit(false);
            guardarEvento(evento);
            borrarHijos(evento.getId());
            guardarReservas(evento);
            conexion.commit();
        } catch (SQLException error) {
            try {
                conexion.rollback();
            } catch (SQLException errorRollback) {
                error.addSuppressed(errorRollback);
            }
            throw new IllegalStateException("No se pudo guardar el evento en H2", error);
        } finally {
            try {
                conexion.setAutoCommit(autoCommitOriginal);
            } catch (SQLException error) {
                throw new IllegalStateException("No se pudo restaurar la conexión H2", error);
            }
        }
    }

    private void guardarEvento(Evento evento) throws SQLException {
        String sql = "MERGE INTO EVENTO (ID,NOMBRE,FECHA,LUGAR,AFORO,BLOQUEADOS) "
                + "KEY(ID) VALUES (?,?,?,?,?,?)";
        try (PreparedStatement sentencia = conexion.prepareStatement(sql)) {
            sentencia.setString(1, evento.getId());
            sentencia.setString(2, evento.getNombre());
            sentencia.setTimestamp(3, Timestamp.valueOf(evento.getFecha()));
            sentencia.setString(4, evento.getLugar());
            sentencia.setInt(5, evento.getAforo());
            sentencia.setInt(6, evento.getBloqueados());
            sentencia.executeUpdate();
        }
    }

    private void borrarHijos(String eventoId) throws SQLException {
        String borrarAsistencias = "DELETE FROM ASISTENCIA WHERE CODIGO_RESERVA IN "
                + "(SELECT CODIGO FROM RESERVA WHERE EVENTO_ID = ?)";
        try (PreparedStatement sentencia = conexion.prepareStatement(borrarAsistencias)) {
            sentencia.setString(1, eventoId);
            sentencia.executeUpdate();
        }
        try (PreparedStatement sentencia = conexion.prepareStatement(
                "DELETE FROM RESERVA WHERE EVENTO_ID = ?")) {
            sentencia.setString(1, eventoId);
            sentencia.executeUpdate();
        }
    }

    private void guardarReservas(Evento evento) throws SQLException {
        String sqlReserva = "INSERT INTO RESERVA "
                + "(CODIGO,EVENTO_ID,ESTUDIANTE_ID,VENCE_EN,ESTADO) VALUES (?,?,?,?,?)";
        String sqlAsistencia = "INSERT INTO ASISTENCIA (CODIGO_RESERVA,HORA) VALUES (?,?)";
        try (PreparedStatement reservas = conexion.prepareStatement(sqlReserva);
             PreparedStatement asistencias = conexion.prepareStatement(sqlAsistencia)) {
            for (Reserva reserva : evento.getReservas()) {
                reservas.setString(1, reserva.getCodigo());
                reservas.setString(2, evento.getId());
                reservas.setString(3, reserva.getEstudianteId());
                reservas.setTimestamp(4, Timestamp.valueOf(reserva.getVenceEn()));
                reservas.setString(5, reserva.getEstado().name());
                reservas.executeUpdate();
                if (reserva.getAsistencia() != null) {
                    asistencias.setString(1, reserva.getCodigo());
                    asistencias.setTimestamp(2, Timestamp.valueOf(reserva.getAsistencia().getHora()));
                    asistencias.executeUpdate();
                }
            }
        }
    }
}
