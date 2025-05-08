
# Documentación Técnica - Sistema de Gestión de Productos e Inventario

## 1. Descripción General

Esta solución está compuesta por:

-   **Frontend Angular**: Interfaz de usuario para la gestión de productos.
    
-   **Microservicio de Productos (Java 21 + Spring Boot)**: Gestión CRUD de productos y consumo del microservicio de inventario.
    
-   **Microservicio de Inventario (Java 21 + Spring Boot)**: Operaciones relacionadas con la cantidad disponible por producto.
    

Comunicación entre componentes:

-   El **Frontend** interactúa con el **microservicio de productos**, que a su vez se comunica con el **microservicio de inventario**.
    
-   La autenticación entre componentes se realiza mediante **API Key** en header (`X-API-KEY`).
    

## 2. Arquitectura General

-   Arquitectura: Hexagonal, basada en la organización por capas de responsabilidad: `config`, `application`, `domain`, `infrastructure` y `shared`, siguiendo el patrón de puertos y adaptadores aunque no de forma estrictamente explícita con el fin de mejorar comprencion a otros devs.
    
-   Organización de paquetes alineada con los principios de arquitectura limpia: separación de casos de uso, lógica de dominio, y adaptadores.
    
-   Aplicación de principios SOLID, destacando:
    
    -   Principio de Responsabilidad Única (SRP): cada clase tiene una única razón para cambiar.
        
    -   Principio de Inversión de Dependencias (DIP): uso de interfaces y sus implementaciones para permitir desacoplamiento.
        
-   Patrón Singleton en servicios clave mediante inyección de dependencias con anotaciones de Spring (`@Service`, `@Component`), asegurando una sola instancia por contexto.
    
-   Lenguaje: Java 21
    
-   Framework: Spring Boot 3+
    
-   Seguridad: API Key y mejora propuesta con JWT
    
-   Comunicación: REST JSON:API
    
-   Orquestación: Docker Compose (local) (puertos y adaptadores implícitos)
    

## 3. Diagrama de Arquitectura (Propuesto a mejora)

```mermaid
graph TD
    A[Frontend Angular - S3 y CloudFront] -->|HTTPS| B(API Gateway)
    B -->|X-API-KEY + JWT| C(Microservicio Productos)
    B -->|X-API-KEY + JWT| D(Microservicio Inventario)
    C -->|REST JSON:API| D
    C -->|JDBC| E[Base de Datos PostgreSQL]
    D -->|JDBC| E

```

## 4. Microservicios

### 4.1. Microservicio de Productos

**Responsabilidad**:

-   Crear, leer, actualizar, eliminar productos
    
-   Gestionar inventario vía cliente REST hacia microservicio de inventario
    

**Endpoints relevantes**:

-   `POST /api/v1/productos`
    
-   `GET /api/v1/productos/{id}`
    
-   `PUT /api/v1/productos/{id}`
    
-   `DELETE /api/v1/productos/{id}`
    
-   `POST /api/v1/productos/filtro`
    

**DTOs y Mappers**:

-   Uso de clases internas anidadas estilo JSON:API
    
-   Mapeo manual con componentes `@Component`
    

### 4.2. Microservicio de Inventario

**Responsabilidad**:

-   Crear y actualizar cantidades de inventario por producto
    
-   Descontar inventario
    
-   Obtener listado por IDs
    

**Endpoints relevantes**:

-   `POST /api/v1/inventarios`
    
-   `PUT /api/v1/inventarios`
    
-   `POST /api/v1/inventarios/descontar`
    
-   `POST /api/v1/inventarios/buscar`
    

**Validación y errores**:

-   `@RestControllerAdvice` para manejo global
    
-   JSON:API-compliant error responses
    

## 5. Seguridad

### Actual

-   API Key (`X-API-KEY`) validada en cada microservicio
    
-   Configurada vía variables de entorno en Docker Compose
    

### Mejora propuesta

-   Autenticación de usuarios vía JWT para uso en frontend
    
-   API Gateway que valide JWT y reenvíe API Key internamente
    

## 6. Despliegue (Mejoras propuestas)

### Backend (EC2 - AWS)

-   Contenedores desplegados con Docker Compose
    
-   CI/CD con GitHub Actions
    
-   Acceso restringido mediante VPC Link
    

### Frontend (S3 + CloudFront)

-   Angular build (`ng build`) desplegado a un bucket S3
    
-   Distribución global mediante CloudFront
    

### API Gateway

-   Enlace entre público y microservicios privados (con VPC Link)
    
-   Validación de JWT tokens y redirección
    

## 7. Instrucciones de Despliegue Local

1.  Clonar repositorio y navegar a raíz del proyecto.
    
2.  Construir contenedores:
    

```bash
docker-compose build

```

3.  Levantar servicios:
    

```bash
docker-compose up -d

```

4.  Acceder a:
    

-   Productos: `http://localhost:8081/dev/productos`
    
-   Inventario: `http://localhost:8082/dev/inventario`
    

## 8. Diagramas de Arquitectura Hexagonal (Markdown)

### Microservicio de Productos

```mermaid
graph TD
    A[REST Controller] --> B[Application Service]
    B --> C[Dominio Producto]
    B --> D[Cliente REST a Inventario]
    B --> E[Repositorio JPA PostgreSQL]

```

### Microservicio de Inventario

```mermaid
graph TD
    A[REST Controller] --> B[Application Service]
    B --> C[Dominio Inventario]
    B --> D[Repositorio JPA PostgreSQL]

```

### Mejora Propuesta: Tolerancia a Fallos y Comunicación Asíncrona

Actualmente se utilizan reintentos simples entre microservicios. Como mejora, se propone usar **Resilience4j**, una herramienta ligera para manejar fallos de forma más robusta. Permite agregar:

-   **Retry**: Reintentos automáticos con tiempos de espera.
    
-   **Circuit Breaker**: Evita saturar servicios caídos.
    
-   **Rate Limiter**: Limita el número de llamadas a servicios.
    
-   **Bulkhead**: Aísla fallos para que no afecten a todo el sistema.
    

Su integración con Spring Boot es sencilla y mejora la estabilidad general.

También se recomienda usar **colas de mensajería** (como RabbitMQ o Kafka) para enviar eventos entre microservicios sin depender de respuestas inmediatas. Esto ayuda a:

-   No bloquear procesos si un servicio está inactivo.
    
-   Separar responsabilidades (por ejemplo, registrar una venta y actualizar inventario).
    
-   Permitir que los servicios trabajen de forma más escalable y resiliente.
    

Estas dos mejoras apuntan a hacer el sistema más tolerante a errores y preparado para alto tráfico.
