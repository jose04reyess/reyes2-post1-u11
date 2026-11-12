# Post-contenido — Unidad 11: Refactorización con SOLID, DTO y Strategy, y Endurecimiento con Manejo de Errores, Logging y Swagger

**Autor:** Reyes  
**Entorno Tecnológico:** Java 17, Spring Boot 3.2.5, Spring Data JPA, H2 Database (In-Memory), Jakarta Bean Validation (Hibernate Validator), SLF4J / Logback, springdoc-openapi 2.3.0 (Swagger UI / OpenAPI 3).  
**Repositorio:** `reyes2-post1-u11`  
**Módulo del Proyecto:** `pedidos/` (`com.tienda.pedidos`)

---

## 1. Introducción y Propósito

El presente proyecto constituye la solución integral a los desafíos de diseño de software empresarial planteados en la **Unidad 11**, consistentes en:
1. **Refactorización arquitectónica profunda** de un sistema legado de gestión de pedidos acoplado, monolítico y vulnerable.
2. Aplicación rigurosa de principios **SOLID** (en particular SRP, OCP y DIP) y patrones de diseño corporativos (**DTO**, **Strategy**, **Repository**).
3. **Endurecimiento en producción** mediante:
   - Manejo centralizado de excepciones con respuestas de error estandarizadas (`@RestControllerAdvice`, `ApiError`).
   - Trazabilidad estructurada con niveles de severidad y rotación diaria de logs (`SLF4J`, `Logback`).
   - Documentación viva y contrato formal interactivo de API RESTful con anotaciones OpenAPI 3 / Swagger UI (`springdoc-openapi`).

---

## 2. Diagrama de Arquitectura en Capas

La arquitectura implementada desacopla estrictamente las responsabilidades de entrada HTTP, orquestación de negocio, cálculo de políticas promocionales, persistencia y comunicación externa:

```text
+---------------------------------------------------------------------------------------------------+
|                                       CLIENTES HTTP / CONSUMIDORES REST                           |
+---------------------------------------------------------------------------------------------------+
                                                  |
                         (JSON / HTTP POST, GET)  |  (JSON Error / 400, 404, 500)
                                                  v
+---------------------------------------------------------------------------------------------------+
| CAPA DE CONTROLADORES REST (@RestController, OpenAPI 3 / Swagger)                                 |
|                                                                                                   |
|   +-------------------------------------------------------------+                                 |
|   | PedidoController                                            |                                 |
|   |  - POST /api/pedidos (@Valid PedidoRequestDTO) -> 201       |                                 |
|   |  - GET  /api/pedidos/{id}                      -> 200 / 404 |                                 |
|   |  - GET  /api/pedidos                           -> 200       |                                 |
|   +-------------------------------------------------------------+                                 |
|                                  ^                                                                |
|                                  | Delegación de Excepciones                                      |
|   +-------------------------------------------------------------+                                 |
|   | GlobalExceptionHandler (@RestControllerAdvice)              |                                 |
|   |  - RecursoNoEncontradoException -> 404 Not Found (ApiError) |                                 |
|   |  - MethodArgumentNotValidException -> 400 Bad Request       |                                 |
|   |  - Exception (General) -> 500 Internal Server Error         |                                 |
|   +-------------------------------------------------------------+                                 |
+---------------------------------------------------------------------------------------------------+
                                                  | Inyección por Constructor (DIP)
                                                  v
+---------------------------------------------------------------------------------------------------+
| CAPA DE SERVICIO Y LÓGICA DE NEGOCIO (@Service, SLF4J Logging)                                    |
|                                                                                                   |
|   +-------------------------------------------------------------+                                 |
|   | PedidoService (Interfaz) <--- PedidoServiceImpl             |                                 |
|   |  - Orquestación de flujo de negocio (SRP)                   |                                 |
|   |  - Registro centralizado de auditoría en SLF4J              |                                 |
|   |  - Mapeo seguro Entidad <-> PedidoResponseDTO               |                                 |
|   +-------------------------------------------------------------+                                 |
|              |                                        |                                           |
|              | Inyección de Mapa (Strategy)           | Inyección de Interfaz (DIP)               |
|              v                                        v                                           |
|   +----------------------------+           +----------------------------------+                   |
|   | EstrategiaDescuento (Intf) |           | NotificacionService (Interfaz)   |                   |
|   |  - DescuentoRegular        |           |  - NotificacionEmailServiceImpl  |                   |
|   |  - DescuentoVip            |           |    (Log SLF4J, cero System.out)  |                   |
|   |  - DescuentoMayorista      |           +----------------------------------+                   |
|   +----------------------------+                                                                  |
+---------------------------------------------------------------------------------------------------+
                                                  |
                                                  | Operaciones JPA tipadas
                                                  v
+---------------------------------------------------------------------------------------------------+
| CAPA DE ACCESO A DATOS (Spring Data JPA)                                                          |
|                                                                                                   |
|   +-------------------------------------------------------------+                                 |
|   | PedidoRepository extends JpaRepository<Pedido, Long>        |                                 |
|   |  - findByClienteEmail(String clienteEmail)                  |                                 |
|   |  - Métodos CRUD estándar generados dinámicamente            |                                 |
|   +-------------------------------------------------------------+                                 |
|                                                  |                                                |
|                                                  v                                                |
|   +-------------------------------------------------------------+                                 |
|   | Entidad JPA: Pedido (@Table("pedido"))                      |                                 |
|   |  - id, clienteEmail, tipoCliente, subtotal, descuento,      |                                 |
|   |    iva, total, estado, notasInternas, fechaCreacion         |                                 |
|   +-------------------------------------------------------------+                                 |
+---------------------------------------------------------------------------------------------------+
                                                  |
                                                  v
+---------------------------------------------------------------------------------------------------+
| BASE DE DATOS RELACIONAL: H2 In-Memory (jdbc:h2:mem:pedidosdb)                                    |
+---------------------------------------------------------------------------------------------------+
```

---

## 3. Violaciones SOLID y Antipatrones Corregidos

El código de partida presentaba severas falencias estructurales y de seguridad que fueron completamente subsanadas:

| Aspecto | Código de Partida (Antipatrón) | Código Refactorizado (Clean Architecture / SOLID) | Principio Aplicado |
| :--- | :--- | :--- | :--- |
| **Responsabilidad Única** | Un único controlador realizaba validación manual, lógica de negocio condicional, persistencia SQL manual y envío de notificaciones. | Separación estricta de capas: Controlador (I/O HTTP y contratos), `PedidoService` (orquestación), `EstrategiaDescuento` (política de descuento), `PedidoRepository` (persistencia) y `NotificacionService` (comunicación). | **SRP (Single Responsibility Principle)** |
| **Abierto/Cerrado** | Cadena de condicionales `if-else` o `switch` en el servicio para cada tipo de cliente. Agregar un descuento requería modificar la clase principal. | Patrón **Strategy** con inyección dinámica del mapa de Beans Spring (`Map<String, EstrategiaDescuento>`). Nuevas políticas promocionales se agregan como nuevos componentes `@Component("NUEVO")` sin alterar código existente. | **OCP (Open/Closed Principle)** |
| **Inversión de Dependencias** | El controlador y el servicio instanciaban implementaciones directas mediante el operador `new` (p. ej. `new NotificacionEmailService()`). | Dependencia absoluta hacia abstracciones (`PedidoService`, `NotificacionService`, `EstrategiaDescuento`). Inyección por constructor administrada por el contenedor de Spring. | **DIP (Dependency Inversion Principle)** |
| **Seguridad de Datos** | Vulnerabilidad crítica a Inyección SQL por concatenación de parámetros en strings de consulta directa. | Sustitución completa por `JpaRepository<Pedido, Long>` con consultas preparadas automáticas y métodos semánticos derivados. | **Seguridad & Principio de Menor Privilegio** |
| **Encapsulamiento y Fuga de Datos** | La entidad JPA `Pedido` se recibía y retornaba directamente en la API HTTP, exponiendo campos internos como `notasInternas`. | Implementación estricta de DTOs (`PedidoRequestDTO` y `PedidoResponseDTO`). El campo `notasInternas` permanece encapsulado en la base de datos y nunca trasciende a las respuestas JSON ni a los logs públicos. | **Encapsulamiento & DTO Pattern** |
| **Manejo de Errores** | Bloques `try/catch` dispersos que silenciaban excepciones arrojando `null` o respuestas vacías sin códigos de estado HTTP semánticos. | Manejo centralizado mediante `@RestControllerAdvice` (`GlobalExceptionHandler`) transformando excepciones de dominio y validación en `ApiError` estándar con HTTP 400, 404 y 500. | **Robustez & Estandarización REST** |
| **Trazabilidad y Logging** | Uso de `System.out.println` sin niveles de severidad ni soporte para auditoría persistente o rotación en producción. | Migración integral a `org.slf4j.Logger` parametrizado con configuración Logback (`logback-spring.xml`) en consola y archivo rotativo (`logs/pedidos.log`). | **Observabilidad de Producción** |

---

## 4. Decisiones de Diseño Justificadas

### 1. `JpaRepository` en vez de `JdbcTemplate`
- **Justificación:** `JpaRepository` abstrae completamente el dialecto SQL subyacente y genera consultas parametrizadas seguras bajo el estándar JDBC, eliminando de raíz cualquier vector de Inyección SQL. Adicionalmente, minimiza el código repetitivo (*boilerplate*), automatiza el mapeo relacional de entidades y simplifica drásticamente el mantenimiento y la portabilidad entre diferentes motores de bases de datos.

### 2. Patrón Strategy para Descuentos vs Método Privado `toResponseDTO` para Mapeo
- **Justificación:** Se aplicó el principio de pragmatismo arquitectónico y equilibrio entre flexibilidad y sobreingeniería:
  * **Estrategias de Descuento (Strategy):** Existe **alta volatilidad de negocio**. Los porcentajes y algoritmos de descuento cambian con promociones, campañas de marketing o segmentaciones comerciales. Separarlos en componentes aislados (`DescuentoRegular`, `DescuentoVip`, `DescuentoMayorista`) satisface OCP y facilita pruebas unitarias independientes sin tocar la orquestación central.
  * **Mapeo Entidad-DTO (`toResponseDTO`):** Corresponde a una transformación determinista, estática y 1:1 entre campos de memoria sin variabilidad condicional. Introducir un framework externo pesado o un Strategy de mapeo para este escenario incurriría en sobreingeniería innecesaria. Un método privado limpio y tipado en el servicio cumple fielmente con las directrices KISS (*Keep It Simple, Stupid*) y DRY (*Don't Repeat Yourself*).

### 3. Punto Único de Auditoría SLF4J en el Servicio
- **Justificación:** Centralizar el log de auditoría del descuento (`log.info("Descuento aplicado: clienteEmail={}, tipoCliente={}, descuento={}")`) en `PedidoServiceImpl` evita duplicar llamadas de logging en cada una de las implementaciones concretas de la estrategia. Esto garantiza un formato homogéneo de auditoría, previene inconsistencias entre estrategias y preserva a las clases de descuento como funciones puras de cálculo matemático.

### 4. Exclusión Estricta de `notasInternas` en DTOs y Logs
- **Justificación:** El campo `notasInternas` almacena directivas operativas confidenciales ("*Pedido creado automaticamente, revisar stock antes de despachar*").
  * Si se expone en `PedidoResponseDTO`, cualquier cliente de la API o consumidor de frontend tendría visibilidad de reglas de bodega interna.
  * Si se registra en los archivos de log (`pedidos.log`), se vulneran políticas de privacidad, cumplimiento normativo y retención segura de logs.
  * Por diseño arquitectónico, el campo se persiste en la entidad de base de datos pero está deliberadamente omitido tanto en el DTO de respuesta como en los argumentos parametrizados del Logger SLF4J.

---

## 5. Estructura del Proyecto

```text
reyes2-post1-u11/
├── capturas/
│   └── README.md                                          <- Guía de capturas de evaluación R2-Lab
├── pedidos/
│   ├── .mvn/wrapper/                                     <- Maven Wrapper binaries y configuración
│   ├── logs/
│   │   └── pedidos.log                                   <- Archivo de log rotativo diario
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/tienda/pedidos/
│   │   │   │   ├── controller/
│   │   │   │   │   └── PedidoController.java             <- REST Controller documentado con OpenAPI
│   │   │   │   ├── dto/
│   │   │   │   │   ├── PedidoRequestDTO.java             <- DTO con Bean Validation y @Schema
│   │   │   │   │   └── PedidoResponseDTO.java            <- DTO seguro sin notasInternas
│   │   │   │   ├── entity/
│   │   │   │   │   └── Pedido.java                       <- Entidad JPA con persistencia completa
│   │   │   │   ├── exception/
│   │   │   │   │   ├── ApiError.java                     <- Modelo uniforme de error REST
│   │   │   │   │   ├── GlobalExceptionHandler.java       <- Controller Advice centralizado
│   │   │   │   │   └── RecursoNoEncontradoException.java <- Excepción semántica 404
│   │   │   │   ├── repository/
│   │   │   │   │   └── PedidoRepository.java             <- Spring Data JPA Repository
│   │   │   │   ├── service/
│   │   │   │   │   ├── NotificacionService.java          <- Interfaz de notificaciones
│   │   │   │   │   ├── NotificacionEmailServiceImpl.java <- Implementación con Logger SLF4J
│   │   │   │   │   ├── PedidoService.java                <- Interfaz de negocio
│   │   │   │   │   └── PedidoServiceImpl.java            <- Orquestador de negocio y auditoría
│   │   │   │   ├── strategy/
│   │   │   │   │   ├── DescuentoMayorista.java           <- Estrategia 25% (@Component("MAYORISTA"))
│   │   │   │   │   ├── DescuentoRegular.java             <- Estrategia 0% (@Component("REGULAR"))
│   │   │   │   │   ├── DescuentoVip.java                 <- Estrategia 15% (@Component("VIP"))
│   │   │   │   │   └── EstrategiaDescuento.java          <- Interfaz del patrón Strategy
│   │   │   │   └── PedidosApplication.java               <- Main Spring Boot con @OpenAPIDefinition
│   │   │   └── resources/
│   │   │       ├── application.properties                <- Configuración H2, OpenAPI y servidor
│   │   │       └── logback-spring.xml                    <- Configuración Logback consola y rotación
│   │   └── test/
│   │       └── java/com/tienda/pedidos/
│   │           ├── controller/
│   │           │   └── PedidoControllerIntegrationTest.java <- Pruebas de integración MockMvc (201, 400, 404, 200)
│   │           ├── service/
│   │           │   └── PedidoServiceTest.java            <- Pruebas unitarias de orquestación con Mockito
│   │           └── strategy/
│   │               └── EstrategiaDescuentoTest.java      <- Pruebas unitarias de cálculo de descuentos
│   ├── mvnw                                              <- Script ejecutor Unix
│   ├── mvnw.cmd                                          <- Script ejecutor Windows
│   └── pom.xml                                           <- Descriptor Maven (Spring Boot 3.2.5)
└── README.md                                             <- Este documento
```

---

## 6. Endpoints de la API y Especificación OpenAPI

| Método | Ruta | Código HTTP | Descripción | Parámetros / Request Body |
| :--- | :--- | :--- | :--- | :--- |
| **POST** | `/api/pedidos` | `201 Created` | Registra un pedido, calcula descuento e IVA, y notifica | `PedidoRequestDTO` (JSON validado) |
| **POST** | `/api/pedidos` | `400 Bad Request` | Falla de validación en campos obligatorios o valores no positivos | `PedidoRequestDTO` con errores |
| **GET** | `/api/pedidos/{id}` | `200 OK` | Consulta el detalle de un pedido existente | `@PathVariable Long id` |
| **GET** | `/api/pedidos/{id}` | `404 Not Found` | El identificador solicitado no existe en base de datos | `@PathVariable Long id` |
| **GET** | `/api/pedidos` | `200 OK` | Lista todos los pedidos registrados en el sistema | Ninguno |

### Ejemplos de Petición y Respuesta

#### 1. Creación de Pedido VIP (201 Created)
**Petición:**
```http
POST /api/pedidos HTTP/1.1
Content-Type: application/json

{
  "clienteEmail": "ana@correo.com",
  "tipoCliente": "VIP",
  "subtotal": 100000.00
}
```

**Respuesta:**
```json
{
  "id": 1,
  "clienteEmail": "ana@correo.com",
  "tipoCliente": "VIP",
  "subtotal": 100000.0,
  "descuento": 15000.0,
  "iva": 16150.0,
  "total": 101150.0,
  "estado": "CREADO"
}
```
*(Nótese la ausencia total del campo `notasInternas` en el JSON).*

#### 2. Error de Validación de Campos (400 Bad Request)
**Petición:**
```http
POST /api/pedidos HTTP/1.1
Content-Type: application/json

{
  "clienteEmail": "",
  "tipoCliente": "",
  "subtotal": -50.00
}
```

**Respuesta (`ApiError`):**
```json
{
  "status": 400,
  "error": "Bad Request",
  "mensaje": "El email del cliente es obligatorio, El subtotal debe ser mayor a cero, El tipo de cliente es obligatorio",
  "timestamp": "2026-11-12T10:51:59.312189700",
  "path": "/api/pedidos"
}
```

#### 3. Recurso No Encontrado (404 Not Found)
**Petición:**
```http
GET /api/pedidos/999 HTTP/1.1
```

**Respuesta (`ApiError`):**
```json
{
  "status": 404,
  "error": "Not Found",
  "mensaje": "Pedido con id 999 no encontrado.",
  "timestamp": "2026-11-12T10:51:59.288351500",
  "path": "/api/pedidos/999"
}
```

---

## 7. Instrucciones de Compilación, Pruebas y Ejecución

### Prerrequisitos
- JDK 17 o superior instalado.
- Terminal compatible con PowerShell, CMD o Bash.

### Ejecución de Pruebas Automatizadas
Desde el directorio `pedidos/`:
```bash
# En Windows PowerShell o CMD:
.\mvnw.cmd clean test

# En Linux o macOS:
./mvnw clean test
```
*Resultado esperado:* **BUILD SUCCESS** con 13 pruebas unitarias y de integración pasando exitosamente al 100%.

### Construcción del Empaquetado JAR
```bash
.\mvnw.cmd clean package
```
Genera el artefacto ejecutable en `target/pedidos-1.0.0.jar`.

### Ejecución de la Aplicación en Modo Desarrollo
```bash
.\mvnw.cmd spring-boot:run
```
O ejecutando directamente el binario empaquetado:
```bash
java -jar target/pedidos-1.0.0.jar
```

---

## 8. Enlaces de Interés y Acceso a Servicios

- **Swagger UI (Documentación Interactiva):**  
  [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)
- **OpenAPI 3 JSON Spec:**  
  [http://localhost:8080/api-docs](http://localhost:8080/api-docs)
- **Consola de Base de Datos H2 (en memoria):**  
  URL JDBC: `jdbc:h2:mem:pedidosdb` | Usuario: `sa` | Contraseña: *(vacía)*
- **Ubicación de Archivo de Log:**  
  `pedidos/logs/pedidos.log` (rotación automática diaria en `pedidos/logs/pedidos.YYYY-MM-DD.log` con retención de 30 días).

---

## 9. Evidencias de Evaluación (R2-Lab)

Las capturas gráficas de validación solicitadas por la rúbrica se encuentran referenciadas en el directorio [`capturas/`](file:///c:/Users/MIGUEL%20RIZO/reyes2-post1-u11/capturas/README.md):
- `01-swagger-ui.png`: Contrato formal de API en Swagger UI con esquemas y modelos.
- `02-post-pedido-vip.png`: Cálculo de descuento VIP (15%), IVA y total con respuesta HTTP 201.
- `03-post-pedido-regular-mayorista.png`: Ejecución de estrategias Regular (0%) y Mayorista (25%).
- `04-post-pedido-bad-request-400.png`: Respuesta estandarizada 400 ante payloads inválidos.
- `05-get-pedido-404-not-found.png`: Respuesta estandarizada 404 manejada por `@RestControllerAdvice`.
- `06-logs-rotacion-archivo.png`: Trazabilidad Logback y omisión de `notasInternas`.
- `07-tests-automatizados.png`: Suite de 13 pruebas automatizadas en verde.
