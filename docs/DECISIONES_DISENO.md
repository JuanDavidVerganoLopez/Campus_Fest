\# Decisiones de diseño y revisión de propuestas de IA

## Regla de aforo

**Propuesta conceptual descartada:** calcular la cantidad de reservas confirmadas y pendientes en `EditarEvento.editar` para determinar si el aforo nuevo era válido.

**Problema:** violaría la alta cohesión, la pauta GRASP de Experto en Información y la arquitectura limpia, porque el caso de uso empezaría a conocer reglas del agregado.

**Decisión implementada:** `Evento.cambiarAforo(nuevoAforo, ahora)` delega internamente en `cuposOcupadosPorReservas`, y `Reserva.ocupaCupo(ahora)` es la única responsable de decidir si una reserva ocupa plaza. El método `Evento.actualizar` valida los datos antes de ejecutar cambios; `EditarEvento.editar` delega, después guarda una sola vez.

**Evidencia real en Git:** los commits de corrección que cada integrante subió sobre el aporte de un compañero están enlazados en la tabla de la sección «Declaración de uso de inteligencia artificial» del `README.md`.

## QR como String

Un lector de QR no se incorpora al núcleo: `RegistrarIngreso.registrar(eventoId, codigo)` recibe texto y `Reserva.registrarIngreso(ahora)` protege que la reserva esté confirmada y no haya ingresado previamente. La interfaz representa el escaneo mediante un campo donde se pega el contenido.

## H2 por puerto

La interfaz `RepositorioEventos` pertenece al anillo de aplicación. Sus dos implementaciones (memoria y H2) pertenecen a infraestructura. `App` instancia el H2 real; no es necesario cambiar casos de uso o entidades si se sustituye el almacenamiento.

## Pendiente vencida

`Reserva.ocupaCupo(ahora)` es una consulta sin efectos secundarios: a los diez minutos deja de ocupar cupo aunque el registro persistido siga en estado PENDIENTE. `Reserva.confirmar(ahora)`, al intentar confirmar fuera de plazo, rechaza la acción **sin modificar el estado** (una operación rechazada no deja efectos parciales; antes marcaba VENCIDA y luego lanzaba la excepción, un cambio que además nunca se persistía). El caso de uso no llama a `guardar` cuando se rechaza, tal como exige el contrato.

## Validación y observabilidad

Se mantienen mensajes explícitos de error en las entidades; las pantallas solo exhiben `getMessage()` y la lectura de entrada. No se añaden funciones CRUD no solicitadas ni se insertan reglas del negocio en JavaFX.

## Cambios tras la revisión de diseño

Revisión estática del proyecto asistida por Claude (sin ejecutar Maven). Los cambios se subieron como commits de corrección sobre los PR 1 a 3 (ver la tabla de evidencias del `README.md`):

- **Agregado como único punto de entrada:** `Evento.confirmarReserva(codigo, ahora)` y `Evento.registrarIngreso(codigo, ahora)` delegan en la `Reserva`; los casos de uso ya no obtienen la reserva para manipularla (Ley de Deméter). `buscarReservaPorCodigo` se conserva por contrato.
- **Duplicación:** `RepositorioEventos.obtener` (método por defecto) concentra la verificación «el evento existe»; antes se repetía en los cuatro casos de uso.
- **`RepositorioEventosEnMemoria` entrega y guarda copias**, igual que H2, para que ambos adaptadores sean intercambiables (sustitución de Liskov).
- **`Evento`:** una fecha nula lanza `IllegalArgumentException` (convención del contrato) en lugar de `NullPointerException`.
- **`Reserva`:** constante `MINUTOS_DE_VIGENCIA` en lugar del 10 repetido en código y mensaje.
- **`Asistencia`:** clase `final` con `equals`/`hashCode` por valor.
- **H2:** las reservas se leen ordenadas por `VENCE_EN` (equivale al orden de creación) y luego por código.
- **`PantallaEventoController`:** mensajes claros para fecha o aforo mal escritos.
- **`DatosDeMuestra`:** los eventos de demostración salen de `App`, que conserva solo el ensamblaje.
- **Nombres sin `ñ`** en identificadores de `App` (`pestanas`, `crearPestana`).
- **Pruebas propias nuevas** (no sustituyen las docentes): `RevisionDisenoTest` (5) y `RepositorioEventosEnMemoriaAislamientoTest` (3).

Decisiones que se dejaron como están: excepciones propias de negocio, DTO para la pantalla de eventos, consulta de conteo en el puerto y reglas no pedidas (reserva duplicada por estudiante, eventos pasados), porque cambian el contrato o amplían el alcance.
