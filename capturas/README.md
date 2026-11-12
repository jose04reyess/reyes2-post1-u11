# Directorio de Capturas y Evidencias de Evaluación (R2-Lab) - Unidad 11

Este directorio documenta las evidencias de ejecución y validación técnica del proyecto `pedidos` para el estudiante **Reyes**, correspondiente a la Unidad 11: *"Refactorización de un Proyecto con SOLID, DTO y Strategy, y Endurecimiento con Manejo de Errores, Logging y Swagger"*.

---

## Índice de Evidencias Requeridas

1. **`01-swagger-ui.png`**:
   - Visualización de la interfaz Swagger UI en `http://localhost:8080/swagger-ui.html`.
   - Comprobación del título *"API de Pedidos"*, versión *"1.0"*, descripción, endpoints `/api/pedidos` (`POST`, `GET`, `GET /{id}`) y esquemas OpenAPI (`PedidoRequestDTO`, `PedidoResponseDTO`, `ApiError`).

2. **`02-post-pedido-vip.png`**:
   - Ejecución de `POST /api/pedidos` con cliente `VIP` y subtotal `100000.00`.
   - Respuesta HTTP `201 Created` con `descuento: 15000.0`, `iva: 16150.0`, `total: 101150.0` y estado `"CREADO"`.

3. **`03-post-pedido-regular-mayorista.png`**:
   - Validación de la ejecución dinámica de `DescuentoRegular` (descuento 0.0) y `DescuentoMayorista` (descuento 25%).
   - Verificación de la inyección por mapa `Map<String, EstrategiaDescuento>` sin condicionales `switch/case` o `if/else` en cascada.

4. **`04-post-pedido-bad-request-400.png`**:
   - Envío de un payload inválido (`clienteEmail: ""`, `tipoCliente: ""`, `subtotal: -10`).
   - Respuesta HTTP `400 Bad Request` con estructura uniforme `ApiError` indicando los errores de validación de Bean Validation.

5. **`05-get-pedido-404-not-found.png`**:
   - Consulta de `GET /api/pedidos/999` para un ID inexistente.
   - Captura de la excepción `RecursoNoEncontradoException` manejada por `GlobalExceptionHandler` con HTTP `404 Not Found` y payload `ApiError`.

6. **`06-logs-rotacion-archivo.png`**:
   - Inspección del archivo `pedidos/logs/pedidos.log`.
   - Trazabilidad con niveles `DEBUG`, `INFO` y `WARN`, demostrando el registro de auditoría del descuento y la estricta omisión del campo confidencial `notasInternas`.

7. **`07-tests-automatizados.png`**:
   - Ejecución de la suite completa de pruebas unitarias y de integración (`./mvnw clean test`).
   - Resultado `BUILD SUCCESS` con 13 tests ejecutados, 0 fallos y 0 errores.

