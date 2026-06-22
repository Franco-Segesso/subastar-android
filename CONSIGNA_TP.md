# CONSIGNA OFICIAL: Sistema de Subastas (Desarrollo de Aplicaciones I)

## 1. Descripción General
La aplicación móvil debe permitir a los usuarios participar de forma on-line en subastas presenciales y ofrecer artículos propios para futuras subastas. La app debe consumir y actualizar información de un sistema local existente de la empresa.

## 2. Modalidad de Subasta
* **Tipo:** Subasta dinámica ascendente.
* **Visibilidad:** Los catálogos son públicos, pero solo los usuarios registrados pueden ver el precio base de venta.
* **Streaming:** Se brindará un servicio de streaming de video, pero **no forma parte del desarrollo de la app**.

## 3. Reglas de Participación y Pujas
* **Requisitos para ingresar:** El usuario debe estar registrado, tener al menos un medio de pago verificado y su categoría debe ser mayor o igual a la de la subasta.
* **Modo Observador:** Quien no cumpla los requisitos anteriores, solo podrá ver la subasta sin pujar.
* **Límites de Puja:**
    * **Mínimo:** La puja debe superar a la mejor oferta actual sumando al menos el 1% del valor base del bien.
    * **Máximo:** La puja no puede superar a la mejor oferta actual por más del 20% del valor base del bien.
    * **Excepción:** Estos límites máximos no aplican a las subastas de categorías Oro y Platino.
* **Concurrencia:** Los usuarios no pueden estar conectados en más de una subasta a la vez.

## 4. Usuarios, Registración y Categorías
* **Registración en dos etapas:**
    1. Ingreso de datos personales y fotos del documento (frente y dorso).
    2. Tras la aprobación externa, se envía un mail para completar el registro y generar la clave.
* **Categorías:** Común, Especial, Plata, Oro y Platino. La diversidad de medios de pago y la actividad mejoran la categoría del usuario.

## 5. Medios de Pago y Cobros
* **Tipos aceptados:** Cuentas bancarias (nacionales o extranjeras), tarjetas de crédito y cheques certificados.
* **Moneda:** Las subastas pueden ser en pesos o en dólares (no bimonetarias). Las de dólares se cancelan exclusivamente en esa moneda.
* **Límite por Cheque:** Si se usa un cheque como garantía, las compras del usuario no pueden superar dicho monto.
* **Multas:** Si al momento de pagar no posee el dinero, recibe una multa del 10% del valor ofertado. No podrá participar en otra subasta hasta abonarla y tiene 72hs para presentar los fondos de la compra.

## 6. Post-Venta, Entregas y Seguros
* **Envíos:** El envío está a cargo del comprador y se incluye en la factura.
* **Retiro personal:** El usuario puede retirar el bien personalmente, pero en ese caso pierde la cobertura del seguro.
* **Seguros de piezas:** Cada bien recibido tiene un seguro. La app debe permitir al dueño ver la ubicación del bien y la póliza contratada.
* **Bienes sin pujas:** Si nadie puja, la empresa compra el artículo por el valor base al finalizar.

## 7. Consignación de Bienes (Publicar artículos)
* El usuario debe ingresar datos del bien, al menos 6 fotos, y declarar que le pertenece sin impedimentos legales.
* Debe poder acreditar el origen lícito de los bienes si es requerido.
* La empresa evalúa el bien. Si lo acepta, propone valor base y comisiones; el usuario debe aceptarlas o rechazarlas.
* La empresa puede agrupar artículos numerosos en una "colección" con el nombre del usuario.

## 8. Entregables Esperados
* **Primera Entrega:** Maquetado, wireframes (Figma), icono, splash screen y diseño de la API REST (Swagger/Endpoints).
* **Segunda Entrega:** Backend y Frontend al 50%. App conectada a la API con al menos un circuito completo funcionando. Manejo de errores definido.
* **Tercera Entrega (Final):** Aplicación 100% funcional. Backend desplegado en línea. Frontend instalable en un dispositivo físico. Estricta trazabilidad con el diseño inicial.