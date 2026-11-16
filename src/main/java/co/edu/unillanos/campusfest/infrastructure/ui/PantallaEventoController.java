package co.edu.unillanos.campusfest.infrastructure.ui;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import co.edu.unillanos.campusfest.application.usecase.EditarEvento;
import co.edu.unillanos.campusfest.domain.Evento;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;

/** Formato de fechas de la interfaz, sin reglas del negocio. */
public class PantallaEventoController {
    private static final DateTimeFormatter FORMATO = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
    private final EditarEvento casoDeUso;

    @FXML private TextField campoEventoId;
    @FXML private TextField campoNombre;
    @FXML private TextField campoFecha;
    @FXML private TextField campoLugar;
    @FXML private TextField campoAforo;
    @FXML private Label etiquetaMensaje;

    public PantallaEventoController(EditarEvento casoDeUso) {
        this.casoDeUso = casoDeUso;
    }

    @FXML
    private void obtener() {
        try {
            Evento evento = casoDeUso.obtener(campoEventoId.getText().trim());
            campoNombre.setText(evento.getNombre());
            campoFecha.setText(evento.getFecha().format(FORMATO));
            campoLugar.setText(evento.getLugar());
            campoAforo.setText(String.valueOf(evento.getAforo()));
            etiquetaMensaje.setText("Evento encontrado: " + evento.getId());
        } catch (RuntimeException error) {
            etiquetaMensaje.setText(error.getMessage());
        }
    }

    @FXML
    private void editar() {
        try {
            LocalDateTime fecha = LocalDateTime.parse(campoFecha.getText().trim(), FORMATO);
            int aforo = Integer.parseInt(campoAforo.getText().trim());
            casoDeUso.editar(campoEventoId.getText().trim(), campoNombre.getText().trim(),
                    fecha, campoLugar.getText().trim(), aforo);
            etiquetaMensaje.setText("Evento actualizado correctamente");
        } catch (RuntimeException error) {
            etiquetaMensaje.setText("No se pudo editar: " + error.getMessage());
        }
    }
}
