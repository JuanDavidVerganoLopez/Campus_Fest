# CampusFest — Proyecto de semestre

Aplicación de escritorio en **Java 21 + Maven + JavaFX 21 + H2**, elaborada siguiendo los casos de uso y el contrato suministrados para el proyecto de semestre. Se aplican los principios de **arquitectura limpia y GRASP**: las entidades toman las decisiones del negocio, los casos de uso coordinan, los puertos abstraen el reloj y la persistencia, y los adaptadores aíslan JavaFX/JDBC.

> **Equipo (3 integrantes):** 
>
> - Integrante 1: **Franky Jhoel Camargo Silva** — @**Yuguen0** — dominio, puertos y CU1/CU2.
> - Integrante 2: **Johan Santiago Marín Obando** — @**johan-marin** — CU3/CU4, persistencia y reloj.
> - Integrante 3: **Juan David Vergaño López** — @**JuanDavidVerganoLopez** — JavaFX, FXML, aplicación y configuración.

## Requisitos y ejecución

1. Instalar **JDK 21** y **Apache Maven 3.9 o posterior**, comprobar `java -version` y `mvn -version`.
2. Con internet la primera vez, dentro de la carpeta que contiene `pom.xml`, ejecutar:

```bash
mvn clean test
mvn javafx:run
```

También se puede ejecutar `mvn compile`. La aplicación abre una ventana con cuatro pestañas y crea el archivo local H2 en `datos/campusfest.mv.db` (ruta ignorada por Git). Maven descarga automáticamente las dependencias del POM. Los comandos pueden ejecutarse desde PowerShell, CMD o la terminal de IntelliJ.

**Eventos de demostración:** al arrancar por primera vez se registran `ev-1` (Festival de bienvenida; aforo 120, bloqueados 5) y `ev-2` (Feria de emprendimiento; aforo 60, bloqueados 3). Se pueden consultar y editar. Son datos iniciales de muestra, no requisitos del contrato. Sus fechas se establecen en relación con el primer arranque. La base permanece entre ejecuciones.

## Los cuatro casos de uso

| Caso | Pantalla JavaFX | Clase de aplicación | Acción |
| --- | --- | --- | --- |
| CU1 | Cupos | `ConsultarCupos.consultar` | Cupos libres del evento |
| CU2 | Reservas | `ReservarEntrada.solicitarReserva`, `confirmarReserva` | Generar código, confirmar dentro de 10 minutos |
| CU3 | Eventos | `EditarEvento.obtener`, `editar` | Modificar nombre, fecha, lugar y aforo válido |
| CU4 | Ingresos | `RegistrarIngreso.registrar` | Registrar una sola asistencia con código QR (texto) |

**Ejemplo completo desde la interfaz:** en Cupos consultar `ev-1`; en Reservas indicar evento `ev-1` y estudiante `est-001`, solicitar, copiar el código y confirmar antes de 10 minutos; en Ingresos pegar ese código y registrar (un segundo intento se rechaza); en Eventos cargar `ev-1`, editar datos/aforo dentro de las restricciones y guardar.

**No es necesario tener un lector QR**: la profesora exige que CU4 reciba un `String`; una lectora que escriba texto puede alimentar el campo. El código se genera con UUID; no se dibuja una imagen QR porque el caso de uso solo recibe su contenido.

## Reglas implementadas

- `Evento.cuposDisponibles(ahora)` = aforo − bloqueados − confirmadas − pendientes no vencidas.
- Una pendiente deja de ocupar cupo exactamente a los diez minutos; una confirmada sigue ocupándolo.
- `Reserva.confirmar(ahora)` pasa de pendiente a confirmada o rechaza sin cambiar el estado; una pendiente vencida deja de ocupar cupo por `ocupaCupo`.
- El aforo nunca puede quedar por debajo de bloqueados + reservas que ocupan cupo.
- `Reserva.registrarIngreso(ahora)` exige confirmación y ausencia de asistencia previa.
- No se permite reservar sin cupos, ingresar dos veces, ni usar un código de otro evento.

## Estructura / arquitectura limpia

```text
src/main/java/co/edu/unillanos/campusfest/
├── domain/
│   ├── Evento.java, Reserva.java, EstadoReserva.java, Asistencia.java
├── application/
│   ├── port/             RepositorioEventos.java, Reloj.java
│   └── usecase/          ConsultarCupos.java, ReservarEntrada.java,
│                       EditarEvento.java, RegistrarIngreso.java
├── infrastructure/
│   ├── persistence/      RepositorioEventosEnMemoria.java, RepositorioEventosH2.java
│   ├── reloj/            RelojDelSistema.java
│   └── ui/               cuatro controladores JavaFX
└── App.java, Launcher.java, DatosDeMuestra.java
src/main/resources/
├── co/edu/unillanos/campusfest/vistas/  cuatro FXML y estilos.css
└── db/schema.sql
src/test/java/                         pruebas docentes sin modificar + 2 clases propias (RevisionDisenoTest, RepositorioEventosEnMemoriaAislamientoTest)
```

Las dependencias apuntan hacia el interior. `domain` nunca conoce `application`, `infrastructure`, JavaFX, H2 o JDBC. Los casos de uso dependen únicamente de entidades y puertos. Solo `RelojDelSistema` consulta `LocalDateTime.now()`. Solo `App` instancia los adaptadores concretos y hace `FXMLLoader.setController(...)`.

### Persistencia

El adaptador `RepositorioEventosH2` recibe una conexión JDBC abierta, ejecuta un esquema idempotente y usa tres tablas: `EVENTO`, `RESERVA` y `ASISTENCIA`. Conserva claves foráneas y guarda el agregado completo de forma transaccional. Los errores JDBC se encapsulan como `IllegalStateException`. `RepositorioEventosEnMemoria` implementa el mismo puerto para facilitar pruebas sin base de datos.

## Pruebas y limitaciones conocidas

`src/test/java` contiene las **90 pruebas de la docente** (10 clases de prueba y 2 dobles auxiliares), copiadas **sin modificar** del material entregado (`4. campusfest-PRUEBAS-para-estudiantes.zip`), más **8 pruebas propias** en `RevisionDisenoTest` (5) y `RepositorioEventosEnMemoriaAislamientoTest` (3): **98 métodos `@Test`** en total. Las dos clases que llegaron después, `RepositorioEventosH2Test` (16) y `PantallasTest` (14), se añadieron sin tocar su contenido; la primera entrega traía 60 pruebas en 8 clases.

Resultado esperado de `mvn clean test`: **98 pruebas en verde**. [INFO] Tests run: 98, Failures: 0, Errors: 0, Skipped: 0


## Declaración de uso de inteligencia artificial (rúbrica 8)

- **Herramienta:** ChatGPT (OpenAI), noviembre de 2026.
- **Herramienta (revisión de diseño):** Claude (Anthropic), noviembre de 2026: revisión estática de errores, modularidad, GRASP y abstracción, y propuesta de las correcciones descritas en `docs/DECISIONES_DISENO.md`.
- **Finalidad:** organización del proyecto Maven, propuesta de arquitectura, generación de código inicial, pantallas FXML y CSS, documentación y revisión de los archivos de prueba recibidos.
- **Código asistido:** `domain/*`, `application/port/*`, `application/usecase/*`, `infrastructure/*`, `App`, `Launcher`, los archivos FXML, CSS, `schema.sql` y `pom.xml`.

| Revisó y corrigió | Pull request | Commits de corrección | Qué se corrigió |
| --- | --- | --- | --- |
| Integrante 2, sobre el aporte del 1 | [PR #02](https://github.com/JuanDavidVerganoLopez/Campus_Fest/pull/2) | [`fb29435`](https://github.com/JuanDavidVerganoLopez/Campus_Fest/pull/2/commits/fb29435efbad8eed39f93a338ad42612a99dc9a8), [`403434b`](https://github.com/JuanDavidVerganoLopez/Campus_Fest/pull/2/commits/403434b7899bc569589fa7cdb231c36afa30beda), [`ce5c435`](https://github.com/JuanDavidVerganoLopez/Campus_Fest/pull/2/commits/ce5c435dfb270cfd5ee84ea6726bb58e09f701ef), [`4fde3e7`](https://github.com/JuanDavidVerganoLopez/Campus_Fest/pull/2/commits/4fde3e7b70a8a4003c23426a0b56247f995e4fd8) | Confirmación vencida sin efectos; `Evento` confirma e ingresa por código; método `obtener` del puerto; `Asistencia` por valor |
| Integrante 3, sobre el aporte del 2 | [PR #03](https://github.com/JuanDavidVerganoLopez/Campus_Fest/pull/3) | [`b11f1e0`](https://github.com/JuanDavidVerganoLopez/Campus_Fest/pull/3/commits/b11f1e09d61c255b5282884c3d0dcfb76b5deedc), [`a67384b`](https://github.com/JuanDavidVerganoLopez/Campus_Fest/pull/3/commits/a67384bb55bbc0331348dc1978bf245962362daa), [`7c73007`](https://github.com/JuanDavidVerganoLopez/Campus_Fest/pull/3/commits/7c73007102a6b64affda8e6c0cec7168cde3c85f), [`eea008e`](https://github.com/JuanDavidVerganoLopez/Campus_Fest/pull/3/commits/eea008e9203c2d480242b320e2db94c4aa6886fa) | Casos de uso con `obtener`; repositorio en memoria con copias; orden de lectura en H2 |
| Integrante 1, sobre el aporte del 3 | [PR #04](https://github.com/JuanDavidVerganoLopez/Campus_Fest/pull/4) | [`4fdfb62`](https://github.com/JuanDavidVerganoLopez/Campus_Fest/pull/4/commits/4fdfb625465dac22acfbc18fe4d8094c2cf64140), [`8d4214e`](https://github.com/JuanDavidVerganoLopez/Campus_Fest/pull/4/commits/8d4214e437e4fd61542a670cecb080883360ed87) | Mensajes claros en la pantalla de eventos; `DatosDeMuestra`; nombres sin `ñ` |



