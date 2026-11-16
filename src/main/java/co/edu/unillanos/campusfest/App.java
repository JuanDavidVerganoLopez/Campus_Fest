package co.edu.unillanos.campusfest;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import co.edu.unillanos.campusfest.application.port.RepositorioEventos;
import co.edu.unillanos.campusfest.application.port.Reloj;
import co.edu.unillanos.campusfest.application.usecase.ConsultarCupos;
import co.edu.unillanos.campusfest.application.usecase.EditarEvento;
import co.edu.unillanos.campusfest.application.usecase.RegistrarIngreso;
import co.edu.unillanos.campusfest.application.usecase.ReservarEntrada;
import co.edu.unillanos.campusfest.domain.Evento;
import co.edu.unillanos.campusfest.infrastructure.persistence.RepositorioEventosH2;
import co.edu.unillanos.campusfest.infrastructure.reloj.RelojDelSistema;
import co.edu.unillanos.campusfest.infrastructure.ui.PantallaCuposController;
import co.edu.unillanos.campusfest.infrastructure.ui.PantallaEventoController;
import co.edu.unillanos.campusfest.infrastructure.ui.PantallaIngresoController;
import co.edu.unillanos.campusfest.infrastructure.ui.PantallaReservaController;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

/** Raíz de composición: única clase que selecciona los adaptadores concretos. */
public class App extends Application {
    private static final String VISTAS = "/co/edu/unillanos/campusfest/vistas/";
    private Connection conexion;

    @Override
    public void start(Stage escenario) throws Exception {
        // El almacenamiento real es H2; los casos de uso y entidades no conocen JDBC.
        Files.createDirectories(Path.of("datos"));
        conexion = DriverManager.getConnection("jdbc:h2:file:./datos/campusfest", "sa", "");
        RepositorioEventos repositorio = new RepositorioEventosH2(conexion);
        Reloj reloj = new RelojDelSistema();
        crearEventosDeMuestraSiNoExisten(repositorio, reloj);

        ConsultarCupos consultarCupos = new ConsultarCupos(repositorio, reloj);
        ReservarEntrada reservarEntrada = new ReservarEntrada(repositorio, reloj);
        EditarEvento editarEvento = new EditarEvento(repositorio, reloj);
        RegistrarIngreso registrarIngreso = new RegistrarIngreso(repositorio, reloj);

        PantallaCuposController pantallaCupos = new PantallaCuposController(consultarCupos);
        PantallaReservaController pantallaReserva = new PantallaReservaController(reservarEntrada);
        PantallaEventoController pantallaEvento = new PantallaEventoController(editarEvento);
        PantallaIngresoController pantallaIngreso = new PantallaIngresoController(registrarIngreso);

        TabPane pestañas = new TabPane();
        pestañas.getTabs().add(crearPestaña("01  Cupos", "pantalla-cupos.fxml", pantallaCupos));
        pestañas.getTabs().add(crearPestaña("02  Reservas", "pantalla-reserva.fxml", pantallaReserva));
        pestañas.getTabs().add(crearPestaña("03  Eventos", "pantalla-evento.fxml", pantallaEvento));
        pestañas.getTabs().add(crearPestaña("04  Ingresos", "pantalla-ingreso.fxml", pantallaIngreso));
        pestañas.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);

        Label marca = new Label("CAMPUSFEST  /  UNILLANOS");
        marca.getStyleClass().add("marca");
        Label subtitulo = new Label("Gestión de eventos universitarios");
        subtitulo.getStyleClass().add("subtitulo");
        VBox cabecera = new VBox(4, marca, subtitulo);
        cabecera.getStyleClass().add("cabecera");
        BorderPane raiz = new BorderPane();
        raiz.setTop(cabecera);
        raiz.setCenter(pestañas);

        Scene escena = new Scene(raiz, 970, 665);
        escena.getStylesheets().add(getClass().getResource(VISTAS + "estilos.css").toExternalForm());
        escenario.setTitle("CampusFest · Panel de eventos");
        escenario.setMinWidth(760);
        escenario.setMinHeight(560);
        escenario.setScene(escena);
        escenario.show();
    }

    private Tab crearPestaña(String nombre, String archivo, Object controlador) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource(VISTAS + archivo));
        loader.setController(controlador);
        Parent contenido = loader.load();
        return new Tab(nombre, contenido);
    }

    private void crearEventosDeMuestraSiNoExisten(RepositorioEventos repositorio, Reloj reloj) {
        if (repositorio.buscar("ev-1") == null) {
            repositorio.guardar(new Evento("ev-1", "Festival de bienvenida",
                    reloj.ahora().plusDays(7), "Auditorio principal", 120, 5));
        }
        if (repositorio.buscar("ev-2") == null) {
            repositorio.guardar(new Evento("ev-2", "Feria de emprendimiento",
                    reloj.ahora().plusDays(10), "Plazoleta central", 60, 3));
        }
    }

    @Override
    public void stop() throws SQLException {
        if (conexion != null && !conexion.isClosed()) {
            conexion.close();
        }
    }
}
