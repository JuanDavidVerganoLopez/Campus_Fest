package co.edu.unillanos.campusfest.infrastructure.ui;

import co.edu.unillanos.campusfest.application.usecase.RegistrarIngreso;
import co.edu.unillanos.campusfest.domain.Asistencia;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;

/** El QR se pega o escanea como texto; no depende de un lector físico. */
public class PantallaIngresoController {
    private final RegistrarIngreso casoDeUso;

    @FXML private TextField campoEventoId;
    @FXML private TextField campoCodigo;
    @FXML private Label etiquetaMensaje;

    public PantallaIngresoController(RegistrarIngreso casoDeUso) {
        this.casoDeUso = casoDeUso;
    }

    @FXML
    private void registrarIngreso() {
        try {
            Asistencia asistencia = casoDeUso.registrar(
                    campoEventoId.getText().trim(), campoCodigo.getText().trim());
            etiquetaMensaje.setText("Ingreso registrado a las " + asistencia.getHora());
        } catch (RuntimeException error) {
            etiquetaMensaje.setText(error.getMessage());
        }
    }
}
