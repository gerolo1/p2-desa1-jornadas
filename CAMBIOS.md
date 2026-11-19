# Cambios

## Compilación
- Se declara `okhttp:4.12.0`. Retrofit 2.11 trae OkHttp 3.14.9, y `ServicioLocal` usa `toMediaType` y `toResponseBody`, que existen recién en OkHttp 4.

## 41 · Programa sin señal
- Nueva tabla `edicion` que registra que hubo una descarga exitosa, aunque el programa venga vacío (v4).
- La base pasa a la versión 2 con `MIGRACION_1_2`. Si el usuario ya tenía charlas, cuentan como edición previa, y no se pierden datos.
- `sincronizar()` reemplaza la instantánea completa en una transacción y borra las charlas que ya no vienen en la respuesta.
- En el catálogo:
  - con edición guardada y falla: la lista sigue visible con el aviso de datos guardados;
  - sin edición y falla: «Reintentar»;
  - con edición vacía: «Sin charlas programadas».

## 42 · Agenda personal
- La sincronización ya no pisa la columna `favorita`:
  - las charlas nuevas se insertan con `IGNORE`;
  - las existentes se actualizan solo en sus datos de contenido (`Contenido` con `@Update(entity = Charla::class)`).
- Las marcas se conservan aunque cambien el título o la sala, incluso si se marcan o desmarcan durante una petición.
- Una charla retirada se elimina del catálogo y de Mi agenda. Si vuelve, vuelve sin marcar.

## 43 · Volver al punto de lectura
- Los filtros de día y tema pasan al `ProgramaModel` con `SavedStateHandle`. Sobreviven a abrir una ficha, a la rotación y a la muerte del proceso. Una tarea nueva arranca sin filtros.
- El estado de la lista (`LazyListState`) pasa a `Portada` para conservar la posición al volver de una ficha.

## 44 · Control de descargas
- Se eliminan las recargas de `MainActivity.onStart()` y del `LaunchedEffect` de `Catalogo`.
- `JornadasApp` mantiene una única instancia de `Servicios` por proceso: una sola base Room, un solo cliente Retrofit y un solo contador.
- Hay una única carga automática por proceso, controlada con `tomarCargaAutomatica()`.
- Si hay una petición en curso, los toques en «Actualizar» se agrupan en un solo intento extra al terminar.
- `servicio_disponible` se aplica antes de la primera petición.
