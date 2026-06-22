# SYSTEM PROMPT: Contexto Integral del Sistema "SubastAR" (v2.0 Definitivo)

**Metadatos del Proyecto**
* **Institución:** Universidad Argentina de la Empresa (UADE)
* **Materia:** Desarrollo de Aplicaciones I - Primer Cuatrimestre 2026
* **Equipo de Desarrollo (Grupo 6):** Juan Pablo Patalano, Patricio Ivan Funes, Franco Segesso.
* **Profesor:** Claudio Jose Godio
* **Repositorios:** `subastar-android-dev` (Frontend) | `subastar-backend-dev` (Backend)

---

## 1. Arquitectura Técnica Global

### 1.1. Backend (Spring Boot 3.x - Java)
* **Estructura:** Arquitectura multicapa (Controllers, Services, Repositories, Entities, Security, Config).
* **Base de Datos:** Relacional, gestionada vía Spring Data JPA. Incluye scripts de inicialización y migración (`sql/migracion_postventa.sql`, `correccion_comisiones.sql`, etc.).
* **Seguridad:** Autenticación stateless mediante JWT (`JwtAuthFilter`, `JwtUtils`, `TokenManager`).
* **Tiempo Real:** Implementación de WebSockets nativos de Spring (`WebSocketConfig`) para la transmisión de pujas en vivo.
* **Servicios Externos (Integrados):**
    * `CloudinaryService`: Para almacenamiento de imágenes de bienes consignados y catálogos.
    * `FirebasePushService`: Para el envío de notificaciones push a dispositivos móviles.
* **Tareas Programadas / Cron:** `VigilanteVencimientosService` (control de plazos de pago y generación de multas automáticas).
* **Infraestructura:** Desplegado en máquina virtual de Microsoft Azure. Proxy inverso con Nginx (Puerto público 80 redirigiendo a `localhost:8080`).

### 1.2. Frontend (Android Nativo - Java)
* **Arquitectura:** MVC adaptado a Android con Activities.
* **Navegación & Red:** Retrofit2 para consumo de API REST. `SubastarApi` mapea todos los endpoints.
* **Tiempo Real:** Cliente WebSocket para la actividad `SalaPujaActivity`.
* **Notificaciones:** Integración con FCM a través de `MiFirebaseMessagingService` y `GeneradorNotificacionesLocales`.
* **Multimedia:** Uso de Glide para carga de catálogos (`ImagenSliderAdapter`).
* **Identidad Visual y UI/UX:**
    * Estética elegante minimalista. Fondo primario: `#0C0C0F` (Negro profundo). Secundario interactivo: `#C9A84C` (Dorado). Textos: `#F0EBE1` y `#A09890`.
    * Tipografías: Tipo *Serif* (ej. Lora) para títulos y *Sans Serif* (ej. Montserrat) para controles y descripciones.
    * *Prohibición estricta:* No se utilizan los `AlertDialog` genéricos de Android. El sistema exige custom dialogs (`android.app.Dialog`) inflando XMLs propios (ej. `dialog_confirmacion_compra.xml`, `dialog_error.xml`, `dialog_exito.xml`) con `Color.TRANSPARENT` para mantener los bordes redondeados.

### 1.3. Scripts Administrativos (Python)
* La lógica de "Back-office" de la empresa (aprobación humana) se simula mediante scripts de Python (`scripts/usuarios/admitir_cliente.py` y `scripts/consignaciones/gestionar_consignaciones.py`).

---

## 2. Reglas de Negocio Centrales del Dominio (Core)

* **Dinámica de Subastas Ascendentes:** * Límite inferior: Puja mínima exigida = `Mejor Oferta Actual + 1% del Precio Base`.
    * Límite superior: Puja máxima permitida = `Mejor Oferta Actual + 20% del Precio Base`.
    * *Excepción:* Postores con categoría `oro` y `platino` no poseen límite superior.
* **Jerarquía de Categorías:** `comun` < `especial` < `plata` < `oro` < `platino`. Un cliente solo puede participar en subastas de categoría inferior o igual a la suya.
* **Ascensos Automáticos:** Calculados dinámicamente en `MedioPagoService` y forzados al hacer login (`AuthController`). Reglas:
    * *Especial:* >= 1 medio de pago.
    * *Plata:* >= 3 medios de pago ó >= 2 compras.
    * *Oro:* >= 5 compras.
    * *Platino:* >= 10 compras.
* **Penalizaciones:** Ganar una subasta y no poseer los fondos desencadena una multa del 10% del valor ofertado, bloqueando al usuario en todo el sistema hasta su pago en 72hs.
* **Consignaciones de Bienes:** Requieren al menos 6 fotos (`FotoRepository`) e imputación de declaración jurada de origen lícito y propiedad (`DocumentoConsignacionRepository`).

---

## 3. Lógicas Críticas e Implementaciones Específicas (Fases 1, 2 y 3)

### A. Modo Observador en Subastas (Atajo de Arquitectura)
* **Regla:** Postores de bajo nivel o sin medios de pago validados pueden ver la subasta pero no pujar.
* **Implementación (Backend):** No se agregó columna de booleanos en BD. Se inserta el registro `Asistente` con valor centinela `numeroPostor = -1`. Si es -1, `SubastaService` rechaza toda puja entrante.
* **Implementación (Frontend):** Si `api.ingresarSubasta()` (que devuelve `Call<Void>`) retorna HTTP 403, Retrofit lo captura y muestra un modal de invitación al Modo Observador. Si el usuario acepta, se relanza con `soloObservar=true`.
* **UI en Modo Observador:** Se oculta totalmente el bloque de pujas: `findViewById(R.id.layoutInputPuja).setVisibility(View.GONE);`.

### B. Gestión de Capacidad de Salas
* Controlada en `SubastaService`. El límite (`capacidadAsistentes`) se verifica mediante la query `@Query` en `AsistenteRepository` que cuenta asistentes activos excluyendo observadores (`numeroPostor <> -1`).

### C. Advertencia Legal de Retiro y Seguros
* En `FacturaCompraActivity.java`, el método `finalizarCompra()` intercepta la acción si `modalidadSeleccionada == "retiro"`. Dispara un modal de alerta (título en rojo) advirtiendo la pérdida total de cobertura del seguro contratado. Modifica dinámicamente el mensaje de éxito post-compra.

### D. Gamificación (Mis Métricas)
* La actividad `MisMetricasActivity.java` posee una barra de progreso que lee las `subastasGanadas` provenientes del `MetricasClienteDTO` e interpola matemáticamente el porcentaje faltante para alcanzar el siguiente rango de comprador.

---

## 4. Directorio y Estructura de Endpoints REST Clave
*Todos los endpoints (excepto los públicos) requieren Bearer Token provisto en el Header.*

* **Autenticación:**
    * `POST /auth/registro` -> `POST /auth/activar` -> `POST /auth/login` (login fuerza la reevaluación del historial del usuario).
* **Pujas y Subastas:**
    * `GET /subastas/{id}` (Público, pero oculta `precioBase` si no hay token).
    * `POST /subastas/{id}/ingresar` (Requiere query param `?soloObservar=boolean`).
    * `POST /subastas/{id}/pujas` (Envía la oferta; backend valida DTO `PujaRequest`).
* **Consignaciones:**
    * `POST /consignaciones` (Multipart-formdata para incluir las imágenes enviadas a Cloudinary).
    * `POST /consignaciones/{id}/documentacion-origen` (Soporte legal adjunto).
    * `PATCH /consignaciones/{id}/respuesta` (El usuario acepta o rechaza las comisiones y precio base tasado por la empresa).
* **Postventa:**
    * `PATCH /clientes/me/compras/{compraId}/entrega` (Selección de envío o retiro).
    * `POST /clientes/me/multas/{id}/pagar` (Cancelación de sanciones).

---

## 5. Directrices Operativas para el Agente IA
1. **Estilo de Código:** Si debes generar nuevas vistas o layouts, emplea estricta y exclusivamente Java (no Kotlin) y respeta las jerarquías de color documentadas (usa referencias a `@color/...`).
2. **WebSockets:** Cualquier interacción de UI referida a la recepción de nuevas pujas debe transcurrir mediante las escuchas del WebSocket configurado, nunca por long-polling.
3. **Manejo de Respuestas de Red:** En Retrofit, toda llamada que devuelva `Call<Void>` no tiene cuerpo evaluable. Los chequeos de éxito se efectúan pura y exclusivamente con `response.isSuccessful()` o validando explícitamente el `response.code()`.
4. **Contexto Universitario:** Mantén presente que el sistema se evalúa académica y funcionalmente. La inmutabilidad de los scripts de base de datos entregados originalmente por la cátedra es vital (razón por la cual se utilizan valores centinela en lugar de alteraciones al DDL).