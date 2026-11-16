package co.edu.unillanos.campusfest.infrastructure.ui;

import co.edu.unillanos.campusfest.application.usecase.ConsultarCupos;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;

/** Solo adapta la interacción de pantalla al CU1. */
public class PantallaCuposController {
    private final ConsultarCupos casoDeUso;

    @FXML private TextField campoEventoId;
    @FXML private Label etiquetaMensaje;

    public PantallaCuposController(ConsultarCupos casoDeUso) {
        this.casoDeUso = casoDeUso;
    }

    @FXML
    private void consultar() {
        try {
            int cupos = casoDeUso.consultar(campoEventoId.getText().trim());
            etiquetaMensaje.setText("Cupos disponibles: " + cupos);
        } catch (RuntimeException error) {
            etiquetaMensaje.setText(error.getMessage());
        }
    }
}
