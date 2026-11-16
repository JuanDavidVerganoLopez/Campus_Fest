package co.edu.unillanos.campusfest.infrastructure.ui;

import co.edu.unillanos.campusfest.application.usecase.ReservarEntrada;
import co.edu.unillanos.campusfest.domain.Reserva;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;

/** Solo recoge los datos y muestra el resultado del CU2. */
public class PantallaReservaController {
    private final ReservarEntrada casoDeUso;

    @FXML private TextField campoEventoId;
    @FXML private TextField campoEstudianteId;
    @FXML private TextField campoCodigoReserva;
    @FXML private Label etiquetaMensaje;

    public PantallaReservaController(ReservarEntrada casoDeUso) {
        this.casoDeUso = casoDeUso;
    }

    @FXML
    private void solicitarReserva() {
        try {
            Reserva reserva = casoDeUso.solicitarReserva(
                    campoEventoId.getText().trim(), campoEstudianteId.getText().trim());
            campoCodigoReserva.setText(reserva.getCodigo());
            etiquetaMensaje.setText("Reserva pendiente. Código QR: " + reserva.getCodigo()
                    + ". Vence: " + reserva.getVenceEn());
        } catch (RuntimeException error) {
            etiquetaMensaje.setText(error.getMessage());
        }
    }

    @FXML
    private void confirmarReserva() {
        try {
            Reserva reserva = casoDeUso.confirmarReserva(
                    campoEventoId.getText().trim(), campoCodigoReserva.getText().trim());
            etiquetaMensaje.setText("Entrada confirmada. Código: " + reserva.getCodigo());
        } catch (RuntimeException error) {
            etiquetaMensaje.setText(error.getMessage());
        }
    }
}
