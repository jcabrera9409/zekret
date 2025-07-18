# Zekret Backend

Este proyecto es el backend para una aplicación web que permite a los usuarios guardar y organizar credenciales personales agrupadas por namespaces. Es un sistema de gestión de credenciales seguro basado en Spring Boot con autenticación JWT.

## Versión y Tecnologías

- **Java 21** (definido en pom.xml)
- **Spring Boot 3.3.5**
- **Spring Security** con autenticación JWT
- **Spring Boot Actuator** para monitoreo y métricas
- **Spring HATEOAS** para APIs REST con hipervínculos
- **MySQL** como base de datos
- **Maven** para gestión de dependencias
- **JPA/Hibernate** para ORM con timestamps automáticos
- **Hibernate Annotations** para `@CreationTimestamp` y `@UpdateTimestamp`
- **JJWT 0.12.3** para manejo de tokens JWT (api, impl, jackson)
- **Jackson** para serialización JSON con control de acceso

## Arquitectura del Proyecto

### Estructura de Directorios

```
src/main/java/com/zekret/
├── ZekretBackApplication.java          # Clase principal de Spring Boot
├── configuration/
│   ├── CORS.java                       # Configuración CORS
│   ├── SecurityConfig.java             # Configuración de seguridad
│   ├── DataInitializer.java            # **NUEVO** - Inicializador automático de datos
│   └── DataInitializerProperties.java  # **NUEVO** - Propiedades de configuración para carga de datos
├── controller/
│   ├── UserController.java             # Controlador REST para usuarios
│   ├── AuthenticationController.java   # Controlador REST para autenticación
│   ├── CredentialController.java       # Controlador REST para credenciales
│   └── NamespaceController.java        # Controlador REST para namespaces - **COMPLETAMENTE IMPLEMENTADO**
├── dto/
│   ├── APIResponseDTO.java             # DTO genérico para respuestas API
│   └── AuthenticationResponseDTO.java  # DTO para respuestas de autenticación
├── model/                              # Entidades JPA
│   ├── User.java
│   ├── Credential.java
│   ├── CredentialType.java
│   ├── Namespace.java
│   └── Token.java
├── repo/                               # Repositorios JPA
│   ├── IGenericRepo.java
│   ├── IUserRepo.java
│   ├── ICredentialRepo.java
│   ├── ICredentialTypeRepo.java        # **MÉTODO OPTIMIZADO** - Query por ZRN para inicialización
│   ├── INamespaceRepo.java             # **MÉTODOS OPTIMIZADOS** - Queries específicas por usuario
│   └── ITokenRepo.java
├── security/
│   ├── JwtAuthenticationFilter.java    # Filtro JWT personalizado
│   └── CustomLogoutHandler.java        # Manejador de logout
├── util/
│   ├── AuthenticationUtils.java        # **NUEVO** - Utilidad reutilizable para autenticación JWT
│   └── ZrnGenerator.java               # Utilidad para generar ZRN únicos
└── service/
    ├── ICRUD.java                      # Interface CRUD genérica
    ├── IUserService.java
    ├── ICredentialService.java
    ├── INamespaceService.java          # **MÉTODOS ESPECÍFICOS** - Queries optimizadas por usuario
    └── impl/                           # Implementaciones de servicios
        ├── CRUDImpl.java
        ├── UserServiceImpl.java
        ├── CredentialServiceImpl.java
        ├── NamespaceServiceImpl.java   # **IMPLEMENTACIÓN COMPLETA** - CRUD optimizado
        ├── AuthenticationService.java
        ├── JwtService.java
        └── UserDetailsServiceImpl.java
```

## Modelo de Datos

### Entidades Principales

#### User
- **Propósito**: Representa a los usuarios del sistema
- **Implementa**: `UserDetails` (Spring Security)
- **Campos clave**:
  - `id`: Long (PK, auto-generated) - `@JsonIgnore`
  - `email`: String (único, requerido)
  - `username`: String (requerido)
  - `password`: String (encriptado con BCrypt) - `@JsonProperty(WRITE_ONLY)`
  - `createdAt`: LocalDateTime - `@CreationTimestamp` + `@JsonProperty(READ_ONLY)`
  - `updatedAt`: LocalDateTime - `@UpdateTimestamp` + `@JsonProperty(READ_ONLY)`
  - `enabled`: boolean (para activación de cuenta) - `@JsonIgnore`
- **Serialización JSON**: 
  - Password solo se acepta en requests (WRITE_ONLY)
  - ID y enabled ocultos en respuestas
  - Timestamps automáticos incluidos en respuestas (READ_ONLY)
- **Timestamps Automáticos**:
  - `createdAt`: Se establece automáticamente al crear el usuario (no actualizable)
  - `updatedAt`: Se actualiza automáticamente en cada modificación del perfil
  - Ambos campos son de solo lectura en la API JSON

#### Credential
- **Propósito**: Almacena las credenciales de los usuarios
- **Campos clave**:
  - `id`: Long (PK) - `@JsonIgnore`
  - `title`: String (título descriptivo)
  - `zrn`: String (Zekret Resource Name, único) - `@JsonProperty(READ_ONLY)`
  - `username`: String (opcional)
  - `password`: String (opcional)
  - `sshPrivateKey`: TEXT (opcional)
  - `secretText`: String (opcional)
  - `fileName`: String (opcional)
  - `fileContent`: TEXT (opcional)
  - `notes`: TEXT (opcional)
  - `createdAt`: LocalDateTime - `@CreationTimestamp`
  - `updatedAt`: LocalDateTime - `@UpdateTimestamp`
- **Relaciones**:
  - `credentialType`: ManyToOne → CredentialType (read/write en JSON)
  - `namespace`: ManyToOne → Namespace (**WRITE_ONLY** en JSON)
  - `user`: ManyToOne → User - `@JsonIgnore`
- **Serialización JSON**:
  - ID y relación user ocultos en respuestas
  - ZRN se genera automáticamente (READ_ONLY)
  - **createdAt y updatedAt**: Incluidos en respuestas JSON (timestamps automáticos)
  - **credentialType**: Visible en requests y responses para permitir asignación por ZRN
  - **namespace**: Solo acepta datos en requests (WRITE_ONLY), no se incluye en responses para evitar referencia circular
- **Timestamps Automáticos**:
  - `createdAt`: Se establece automáticamente al crear la entidad (no actualizable)
  - `updatedAt`: Se actualiza automáticamente en cada modificación
- **Reglas de Negocio**:
  - Debe asignarse a un namespace al crearse
  - Debe especificar un credentialType al crearse
  - Namespace no puede cambiarse una vez asignado
  - CredentialType puede actualizarse via ZRN
  - Filtrado automático por usuario en todas las consultas

#### CredentialType
- **Propósito**: Define tipos de credenciales (password, ssh, token, archivo, etc.)
- **Campos**:
  - `id`: Long (PK) - `@JsonIgnore`
  - `zrn`: String (único, formato slug) - **Read/Write en JSON**
  - `name`: String
- **Serialización JSON**:
  - ID oculto en respuestas
  - **ZRN completamente accesible** para permitir asignación desde frontend

#### Namespace
- **Propósito**: Agrupa credenciales por categorías/proyectos
- **Campos**:
  - `id`: Long (PK) - `@JsonIgnore`
  - `name`: String
  - `zrn`: String - **Read/Write en JSON**
  - `description`: String
  - `createdAt`: LocalDateTime - `@CreationTimestamp` + `@JsonProperty(READ_ONLY)`
  - `updatedAt`: LocalDateTime - `@UpdateTimestamp` + `@JsonProperty(READ_ONLY)`
- **Relaciones**: 
  - `user`: ManyToOne → User - `@JsonIgnore`
  - `credentials`: OneToMany → Credential - `@JsonProperty(READ_ONLY)`
- **Configuración de Cascada**:
  - `cascade = {CascadeType.ALL}` - Todas las operaciones se propagan a credenciales
  - `orphanRemoval = true` - Credenciales huérfanas se eliminan automáticamente
- **Serialización JSON**:
  - ID y relación user ocultos
  - **ZRN completamente accesible** para permitir asignación desde frontend
  - **credentials**: Lista de credenciales incluida en responses (READ_ONLY) para mostrar estadísticas
  - Timestamps son READ_ONLY (generados automáticamente por Hibernate)
- **Timestamps Automáticos**:
  - `createdAt`: Se establece automáticamente al crear la entidad (no actualizable)
  - `updatedAt`: Se actualiza automáticamente en cada modificación
  - Ambos campos son de solo lectura en la API JSON

#### Token
- **Propósito**: Gestiona tokens JWT para autenticación
- **Campos**:
  - `id`: Long (PK) - `@JsonIgnore`
  - `accessToken`: String (único)
  - `refreshToken`: String (único)
  - `loggedOut`: boolean
- **Relación**: `user`: ManyToOne → User - `@JsonIgnore`
- **Serialización JSON**:
  - ID y relación user ocultos en respuestas
  - Solo tokens visibles en JSON

### Concepto ZRN (Zekret Resource Name)
- Sistema de identificación único similar a ARN de AWS
- Presente en: Credential, CredentialType, Namespace
- Permite identificación única de recursos en el sistema

#### Generación de ZRN
La clase `ZrnGenerator` proporciona métodos para generar ZRN únicos:

**Formatos de ZRN:**
- **Credential**: `zrn:zekret:credential:20250715:uuid`
- **Namespace**: `zrn:zekret:namespace:20250715:uuid`  
- **CredentialType**: `ssh_credential` (formato slug)

**Ejemplos de uso:**
```java
// Generar ZRN para credencial
String credentialZrn = ZrnGenerator.generateCredentialZrn();
// Resultado: zrn:zekret:credential:20250715:a1b2c3d4-e5f6-7890-abcd-ef1234567890

// Generar ZRN para namespace
String namespaceZrn = ZrnGenerator.generateNamespaceZrn();
// Resultado: zrn:zekret:namespace:20250715:b2c3d4e5-f6g7-8901-bcde-f23456789012

// Generar ZRN slug para tipo de credencial
String typeZrn = ZrnGenerator.generateCredentialTypeZrn("SSH Credential");
// Resultado: ssh_credential

// Validar ZRN
boolean isValid = ZrnGenerator.isValidZrn("ssh_credential"); // true
boolean isValid2 = ZrnGenerator.isValidZrn("zrn:zekret:credential:20250715:uuid"); // true
```

**Ejemplos de tipos de credenciales y sus ZRN slug:**
- "SSH Credential" → `ssh_credential`
- "Database Password" → `database_password`
- "API Token" → `api_token`
- "Certificate File" → `certificate_file`
- "Secret Note" → `secret_note`
- "2FA Code" → `2fa_code`

**Métodos disponibles:**
- `generateCredentialZrn()`: ZRN completo para credenciales
- `generateNamespaceZrn()`: ZRN completo para namespaces  
- `generateCredentialTypeZrn(String name)`: Slug para tipos de credencial
- `isValidZrn(String zrn)`: Validación de ZRN y slugs
- `extractResourceType(String zrn)`: Extrae tipo de recurso
- `generateSlug(String name)`: Convierte nombre a slug

## Integración del ZrnGenerator

### Ejemplo de uso en servicios

```java
// En CredentialServiceImpl
@Override
public Credential save(Credential credential) {
    if (credential.getZrn() == null || credential.getZrn().isEmpty()) {
        credential.setZrn(ZrnGenerator.generateCredentialZrn());
    }
    return super.save(credential);
}

// En NamespaceServiceImpl  
@Override
public Namespace save(Namespace namespace) {
    if (namespace.getZrn() == null || namespace.getZrn().isEmpty()) {
        namespace.setZrn(ZrnGenerator.generateNamespaceZrn());
    }
    return super.save(namespace);
}

// Para CredentialType - manejo directo en controlador o inicialización
// Los tipos de credenciales pueden ser pre-poblados en la base de datos
// o creados programáticamente con:
String credentialTypeZrn = ZrnGenerator.generateCredentialTypeZrn("SSH Credential");
```

## Configuración de Seguridad

### SecurityConfig
- **Endpoints públicos**:
  - `/v1/auth/login/**`
  - `/v1/auth/recover_password/**`
  - `/v1/auth/reset_password/**`
  - `/v1/users/register`
  - `/actuator/health/**`
- **Configuración**:
  - Sesiones stateless (JWT)
  - CSRF deshabilitado
  - Filtro JWT personalizado
  - Manejo de logout seguro
  - Encriptación BCrypt para contraseñas

### Servicios de Autenticación
- **AuthenticationService**: Maneja login y generación de tokens
- **JwtService**: Generación y validación de tokens JWT
- **UserDetailsServiceImpl**: Carga detalles de usuario para Spring Security
- **JwtAuthenticationFilter**: Filtro para validar tokens en cada request

### Autenticación por Token JWT

#### Obtención del Usuario desde el Header Authorization
Los endpoints protegidos obtienen el usuario autenticado desde el token JWT en el header:

```java
// En NamespaceController
private User getAuthenticatedUserFromToken(String authorizationHeader) {
    if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
        throw new RuntimeException("Invalid or missing Authorization header");
    }
    
    String token = authorizationHeader.substring(7); // Remove "Bearer " prefix
    String username = jwtService.extractUsername(token);
    
    // Search user in database
    List<User> allUsers = userService.getAll();
    return allUsers.stream()
        .filter(user -> user.getUsername().equals(username) || user.getEmail().equals(username))
        .findFirst()
        .orElseThrow(() -> new RuntimeException("User not found: " + username));
}
```

#### Características de Seguridad
- **Persistent Session:** La sesión persiste entre reinicios del backend via JWT
- **Header Authorization:** Formato `Authorization: Bearer <token>`
- **User Filtering:** Todos los recursos se filtran automáticamente por usuario
- **Relationship Validation:** Verificación explícita de relaciones por ID en servicios
- **AuthenticationUtils:** **NUEVA UTILIDAD** - Extracción y validación de usuarios JWT reutilizable
- **Optimized Queries:** Consultas específicas por usuario en lugar de cargar todos los datos
- **Performance Escalable:** Rendimiento constante independiente del crecimiento de datos

## API REST

### Endpoints Disponibles

#### Usuarios (`/v1/users`)
- **POST** `/register`: Registro de nuevos usuarios
  - Valida email y username únicos
  - Encripta password con BCrypt
  - Retorna APIResponseDTO con usuario creado

#### Autenticación (`/v1/auth`)
- **POST** `/login`: Autenticación de usuarios
  - Acepta login con **username** o **email** (campo username)
  - Valida credenciales contra la base de datos
  - Genera tokens JWT (access y refresh)
  - Revoca tokens anteriores del usuario
  - Retorna APIResponseDTO con AuthenticationResponseDTO

#### Namespaces (`/v1/namespaces`) 🔒
**Nota**: Todos los endpoints requieren autenticación JWT y filtran automáticamente por usuario.

- **POST** `/`: Crear un nuevo namespace
  - Genera ZRN automáticamente
  - Asigna namespace al usuario autenticado
  - Establece timestamps de creación

- **PUT** `/{zrn}`: Modificar namespace existente
  - Solo permite modificar `name` y `description`
  - Actualiza `updatedAt` automáticamente
  - Valida pertenencia al usuario autenticado
  - **OPTIMIZADO**: Usa query específica por ZRN y usuario

- **GET** `/{zrn}`: Obtener namespace por ZRN
  - Busca namespace específico del usuario autenticado
  - Retorna error 404 si no existe o no pertenece al usuario
  - **OPTIMIZADO**: Query directa sin cargar datos innecesarios

- **GET** `/`: Listar todos los namespaces del usuario
  - Filtra automáticamente por usuario autenticado
  - Retorna lista completa de namespaces del usuario
  - **OPTIMIZADO**: Query específica por usuario

- **DELETE** `/{zrn}`: Eliminar namespace físicamente
  - Eliminación permanente de la base de datos
  - Valida pertenencia al usuario antes de eliminar
  - **OPTIMIZADO**: Validación previa con query específica
  - Retorna confirmación de eliminación exitosa

### Gestión de Namespaces

**Nota**: Todos los ejemplos de namespace requieren el header de autorización:
`Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9`

#### Crear Namespace
```bash
curl -X POST http://localhost:8080/v1/namespaces \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -d '{
    "name": "Desarrollo",
    "description": "Namespaces para credenciales de desarrollo"
  }'
```

#### Respuesta de Namespace Creado
```json
{
  "success": true,
  "message": "Namespace registered successfully",
  "data": {
    "name": "Desarrollo",
    "zrn": "zrn:zekret:namespace:20250715:uuid-here",
    "description": "Namespaces para credenciales de desarrollo",
    "createdAt": "2025-07-15T22:15:00",
    "updatedAt": "2025-07-15T22:15:00"
  },
  "statusCode": 201,
  "timestamp": "2025-07-15T22:15:00"
}
```

#### Modificar Namespace
```bash
curl -X PUT http://localhost:8080/v1/namespaces/zrn:zekret:namespace:20250715:uuid-here \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -d '{
    "name": "Desarrollo Actualizado",
    "description": "Descripción actualizada del namespace"
  }'
```

#### Obtener Namespace por ZRN
```bash
curl -X GET http://localhost:8080/v1/namespaces/zrn:zekret:namespace:20250715:uuid-here \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

#### Listar Todos los Namespaces
```bash
curl -X GET http://localhost:8080/v1/namespaces \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

#### Eliminar Namespace
```bash
curl -X DELETE http://localhost:8080/v1/namespaces/zrn:zekret:namespace:20250715:uuid-here \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

#### Respuesta de Eliminación Exitosa
```json
{
  "success": true,
  "message": "Namespace deleted successfully",
  "data": "Namespace 'zrn:zekret:namespace:20250715:uuid-here' has been permanently deleted",
  "statusCode": 200,
  "timestamp": "2025-07-15T22:15:00"
}
```

## 🔧 Servicios con Relaciones Explícitas

### NamespaceServiceImpl
La implementación del servicio maneja explícitamente las relaciones JPA por ID para evitar problemas de cascada:

```java
@Override
public Namespace register(Namespace entity) {
    logger.info("Registering namespace with explicit user relationship handling");
    
    // User is already validated and set by the controller, no need to re-fetch
    logger.info("User relationship already established: {}", entity.getUser().getUsername());
    
    return super.register(entity);
}
```

**Ventajas:**
- Previene errores de entidades transitorias
- Garantiza la integridad referencial
- Manejo explícito de relaciones @ManyToOne y @OneToMany
- Validación de existencia de entidades relacionadas

### AuthenticationUtils - **NUEVA UTILIDAD REUTILIZABLE**

Clase utilitaria que centraliza la extracción y validación de usuarios JWT para todos los controladores:

```java
@Component
public class AuthenticationUtils {
    
    @Autowired
    private JwtService jwtService;
    
    @Autowired
    private IUserService userService;
    
    /**
     * Extracts and validates the authenticated user from the JWT token
     */
    public User getAuthenticatedUserFromToken(String authorizationHeader) {
        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
            throw new RuntimeException("Invalid or missing Authorization header");
        }
        
        String token = authorizationHeader.substring(7);
        String username = jwtService.extractUsername(token);
        
        // Search in all users
        List<User> allUsers = userService.getAll();
        return allUsers.stream()
                .filter(user -> user.getUsername().equals(username) || user.getEmail().equals(username))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("User not found: " + username));
    }
}
```

**Beneficios:**
- ✅ **Reutilización**: Un solo punto para autenticación JWT
- ✅ **Consistencia**: Misma lógica en todos los controladores  
- ✅ **Mantenibilidad**: Cambios centralizados
- ✅ **Logging**: Trazabilidad unificada
- ✅ **Error Handling**: Manejo consistente de errores

### INamespaceService - **MÉTODOS OPTIMIZADOS**

Interface extendida con consultas específicas por usuario para optimizar rendimiento:

```java
public interface INamespaceService extends ICRUD<Namespace, Long> {
    
    /**
     * Get all namespaces for a specific user
     */
    List<Namespace> getNamespacesByUserId(Long userId);
    
    /**
     * Get a namespace by ZRN and user ID - OPTIMIZED QUERY
     */
    Optional<Namespace> getNamespaceByZrnAndUserId(String zrn, Long userId);
    
    /**
     * Check if a namespace exists for the user - EXISTENCE CHECK
     */
    boolean existsNamespaceByZrnAndUserId(String zrn, Long userId);
}
```

**Optimizaciones de Performance:**
- ✅ **Queries Específicas**: Consultas directas por usuario evitando cargar datos innecesarios
- ✅ **Existence Checks**: Validaciones rápidas sin transferir entidades completas
- ✅ **Escalabilidad**: Rendimiento constante independiente del crecimiento de datos
- ✅ **Memory Efficiency**: Menor uso de memoria al evitar cargar colecciones grandes

## 📋 Controladores REST Estándar

### NamespaceController - **COMPLETAMENTE IMPLEMENTADO Y OPTIMIZADO**

**Ruta base:** `/v1/namespaces`
**Autenticación:** Requerida en header `Authorization: Bearer <token>`

**Optimizaciones Implementadas:**
- ✅ **AuthenticationUtils:** Autenticación genérica reutilizable
- ✅ **Queries Específicas:** Consultas optimizadas por usuario y ZRN
- ✅ **Performance Mejorada:** No carga datos innecesarios en memoria
- ✅ **Escalabilidad:** Performance constante independiente del crecimiento
- ✅ **Logging Detallado:** Trazabilidad completa de operaciones
- ✅ **Error Handling:** Manejo robusto de errores y casos edge

**Endpoints REST Completamente Implementados:**

#### 1. Crear Namespace
- **Endpoint:** `POST /v1/namespaces`
- **Descripción:** Crea un nuevo namespace para el usuario autenticado
- **Optimizaciones:** Validación de usuario automática, generación de ZRN
- **Body Request:**
```json
{
  "name": "development",
  "description": "Development environment namespace"
}
```
- **Response:**
```json
{
  "data": {
    "zrn": "zrn:zekret:namespace:20250717:abc123-uuid",
    "name": "development", 
    "description": "Development environment namespace",
    "createdAt": "2025-07-17T19:30:45.123456",
    "updatedAt": "2025-07-17T19:30:45.123456",
    "credentials": []
  },
  "message": "Namespace created successfully",
  "success": true,
  "statusCode": 201
}
```

#### 2. Actualizar Namespace
- **Endpoint:** `PUT /v1/namespaces/{zrn}`
- **Descripción:** Actualiza un namespace existente
- **Optimizaciones:** Query directa por ZRN y usuario, validación previa
- **Body Request:**
```json
{
  "name": "production",
  "description": "Production environment namespace"
}
```

#### 3. Obtener Namespace por ZRN
- **Endpoint:** `GET /v1/namespaces/{zrn}`
- **Descripción:** Obtiene un namespace específico del usuario
- **Optimizaciones:** Query optimizada evitando carga de datos innecesarios

#### 4. Listar Namespaces del Usuario
- **Endpoint:** `GET /v1/namespaces`
- **Descripción:** Lista todos los namespaces del usuario autenticado
- **Optimizaciones:** Query específica por usuario, filtrado automático

#### 5. Eliminar Namespace
- **Endpoint:** `DELETE /v1/namespaces/{zrn}`
- **Descripción:** Elimina permanentemente un namespace
- **Optimizaciones:** Validación previa de existencia y pertenencia
- **Response:**
```json
{
  "data": "Namespace 'zrn:zekret:namespace:20250717:abc123' has been permanently deleted",
  "message": "Namespace deleted successfully",
  "success": true,
  "statusCode": 200
}
```

### Ejemplo de Implementación Optimizada

```java
@PutMapping("/{zrn}")
public ResponseEntity<APIResponseDTO<Namespace>> updateNamespace(
        @RequestHeader("Authorization") String authorizationHeader,
        @PathVariable String zrn, 
        @RequestBody Namespace request) {
    try {
        // Reutilizable authentication utility
        User authenticatedUser = authenticationUtils.getAuthenticatedUserFromToken(authorizationHeader);
        
        // Optimized query - direct lookup by ZRN and user
        Optional<Namespace> existingNamespaceOpt = namespaceService.getNamespaceByZrnAndUserId(zrn, authenticatedUser.getId());
        
        if (!existingNamespaceOpt.isPresent()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(APIResponseDTO.error("Namespace not found or access denied", HttpStatus.NOT_FOUND.value()));
        }
        
        // Update only allowed fields
        Namespace existingNamespace = existingNamespaceOpt.get();
        existingNamespace.setName(request.getName());
        existingNamespace.setDescription(request.getDescription());
        
        Namespace updatedNamespace = namespaceService.modify(existingNamespace);
        
        return ResponseEntity.ok(APIResponseDTO.success("Namespace updated successfully", updatedNamespace, HttpStatus.OK.value()));
        
    } catch (Exception e) {
        logger.error("Error updating namespace: {}", e.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(APIResponseDTO.error("Failed to update namespace: " + e.getMessage(), HttpStatus.BAD_REQUEST.value()));
    }
}
```

#### 3. Obtener Namespace por ZRN
- **Endpoint:** `GET /v1/namespaces/{zrn}`
- **Descripción:** Obtiene un namespace específico por su ZRN

#### 4. Listar Todos los Namespaces
- **Endpoint:** `GET /v1/namespaces`
- **Descripción:** Obtiene todos los namespaces del usuario autenticado
- **Response:**
```json
{
  "data": [
    {
      "id": 1,
      "zrn": "zrn:namespace:development-abc123",
      "name": "development",
      "description": "Development environment namespace",
      "createdAt": "2025-01-15T10:30:00",
      "updatedAt": "2025-01-15T10:30:00"
    }
  ],
  "message": "Namespaces retrieved successfully",
  "success": true,
  "statusCode": 200
}
```

#### 5. Eliminar Namespace
- **Endpoint:** `DELETE /v1/namespaces/{zrn}`
- **Descripción:** Elimina físicamente un namespace del usuario
- **Response:**
```json
{
  "data": "Namespace 'zrn:namespace:development-abc123' has been permanently deleted",
  "message": "Namespace deleted successfully",
  "success": true,
  "statusCode": 200
}
```

## Configuración de Base de Datos

### application.properties
```properties
spring.datasource.url=${DATASOURCE_BD:jdbc:mysql://localhost:3306/zekretdb}
spring.datasource.username=${USER_BD:root}
spring.datasource.password=${PASSWORD_BD:root}
spring.jpa.hibernate.ddl-auto=update
security.jwt.secret-key=${JWT_SECRET_KEY}
security.jwt.access-token-expiration=43200000    # 12 horas
security.jwt.refresh-token-expiration=86400000   # 24 horas
```

### Inicialización Automática de Datos

#### DataInitializer - Carga de Tipos de Credenciales
El sistema incluye un componente de inicialización automática que carga los tipos de credenciales predefinidos al arrancar la aplicación:

```java
@Component
public class DataInitializer implements CommandLineRunner {
    
    @Autowired
    private DataInitializerProperties properties;
    
    @Autowired
    private ICredentialTypeRepo credentialTypeRepo;
    
    @Override
    public void run(String... args) throws Exception {
        initializeCredentialTypes();
    }
}
```

**Características del Inicializador:**
- ✅ **Carga Automática**: Se ejecuta al iniciar la aplicación
- ✅ **Idempotente**: No duplica datos existentes, actualiza si es necesario
- ✅ **Configurable**: Tipos definidos en `data-initializer.yml`
- ✅ **Resiliente**: Valida configuración antes de procesar

#### Configuración de Tipos Predefinidos
Archivo `src/main/resources/data-initializer.yml`:

```yaml
data-initializer:
  credentialType:
    - zrn: username_password
      name: Username/Password
    - zrn: ssh_username
      name: SSH Username
    - zrn: secret_text
      name: Secret Text
    - zrn: file
      name: File
```

**Tipos de Credenciales Inicializados:**
- **username_password**: Para credenciales tradicionales de usuario/contraseña
- **ssh_username**: Para claves SSH con usuario
- **secret_text**: Para tokens, códigos o texto secreto
- **file**: Para archivos como certificados o configuraciones

#### ICredentialTypeRepo - Repository Optimizado
```java
public interface ICredentialTypeRepo extends IGenericRepo<CredentialType, Long> {
    /**
     * Find credential type by ZRN using Spring Data JPA naming convention
     */
    Optional<CredentialType> findByZrn(String zrn);
}
```

**Ventajas del Sistema de Inicialización:**
- **Datos Consistentes**: Garantiza que tipos básicos siempre estén disponibles
- **Flexibilidad**: Fácil agregar nuevos tipos modificando el YAML
- **Performance**: Carga una sola vez al inicio de la aplicación
- **Mantenimiento**: Actualiza automáticamente nombres si cambian en configuración

### Variables de Entorno Requeridas
- `JWT_SECRET_KEY`: Clave secreta para firmar tokens JWT
- `DATASOURCE_BD`: URL de conexión a MySQL (opcional)
- `USER_BD`: Usuario de MySQL (opcional)
- `PASSWORD_BD`: Contraseña de MySQL (opcional)

## Patrones de Diseño Implementados

### 1. Repository Pattern
- `IGenericRepo<T, ID>`: Repositorio base genérico
- Repositorios específicos extienden la funcionalidad base
- Abstrae la lógica de acceso a datos

### 2. Service Layer Pattern
- `ICRUD<T, ID>`: Operaciones CRUD genéricas
- `CRUDImpl<T, ID>`: Implementación base genérica
- Servicios específicos para lógica de negocio particular

### 3. DTO Pattern
- `APIResponseDTO<T>`: Respuesta estándar para todas las APIs
- `AuthenticationResponseDTO`: Respuesta específica de autenticación
- Separación entre entidades de BD y objetos de transferencia

### 4. Filter Pattern
- `JwtAuthenticationFilter`: Filtro personalizado para JWT
- Integrado en la cadena de filtros de Spring Security

## Logging y Monitoreo

- **SLF4J**: Framework de logging estándar
- **Spring Boot Actuator**: Para métricas y health checks
- Logs detallados en servicios críticos (autenticación, registro)

## Dependencias Maven Principales

```xml
<!-- Spring Boot Starters -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-data-jpa</artifactId>
</dependency>
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-web</artifactId>
</dependency>
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-security</artifactId>
</dependency>
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-hateoas</artifactId>
</dependency>
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-actuator</artifactId>
</dependency>

<!-- Base de Datos -->
<dependency>
    <groupId>com.mysql</groupId>
    <artifactId>mysql-connector-j</artifactId>
</dependency>

<!-- JWT -->
<dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt-api</artifactId>
    <version>0.12.3</version>
</dependency>
<dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt-impl</artifactId>
    <version>0.12.3</version>
</dependency>
<dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt-jackson</artifactId>
    <version>0.12.3</version>
</dependency>
```

## Ejecución del Proyecto

### Requisitos Previos
1. **Java 21** instalado
2. **MySQL 8+** corriendo en puerto 3306
3. Base de datos `zekretdb` creada
4. Variables de entorno configuradas

### Comando de Ejecución
```bash
mvn spring-boot:run
```

### Compilación
```bash
mvn clean compile
mvn clean package
```

## Configuración de Desarrollo

### Variables de Entorno (.env)
```properties
JWT_SECRET_KEY=tu_clave_secreta_muy_larga_y_segura_aqui
DATASOURCE_BD=jdbc:mysql://localhost:3306/zekretdb
USER_BD=root
PASSWORD_BD=tu_password_mysql
```

### Configuración de Base de Datos
```sql
-- Crear base de datos
CREATE DATABASE zekretdb CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- Crear usuario (opcional)
CREATE USER 'zekret_user'@'localhost' IDENTIFIED BY 'password';
GRANT ALL PRIVILEGES ON zekretdb.* TO 'zekret_user'@'localhost';
FLUSH PRIVILEGES;
```

## Estructura de Respuestas API

### APIResponseDTO Estándar
```json
{
  "success": true,
  "message": "Mensaje descriptivo",
  "data": { /* objeto de datos */ },
  "statusCode": 200,
  "timestamp": "2025-07-15T19:23:45"
}
```

### AuthenticationResponseDTO
```json
{
  "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "message": "User authenticated successfully"
}
```

## Ejemplos de Uso de la API

### Registro de Usuario
```bash
curl -X POST http://localhost:8080/v1/users/register \
  -H "Content-Type: application/json" \
  -d '{
    "username": "johndoe",
    "email": "john@example.com",
    "password": "securePassword123"
  }'
```

### Login de Usuario (con username)
```bash
curl -X POST http://localhost:8080/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "johndoe",
    "password": "securePassword123"
  }'
```

### Login de Usuario (con email)
```bash
curl -X POST http://localhost:8080/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "john@example.com",
    "password": "securePassword123"
  }'
```

### Respuesta de Login Exitoso
```json
{
  "success": true,
  "message": "User authenticated successfully",
  "data": {
    "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "message": "User authenticated successfully"
  },
  "statusCode": 200,
  "timestamp": "2025-07-15T21:37:08"
}
```

## Serialización JSON

El proyecto utiliza **Jackson** para la serialización/deserialización JSON con las siguientes configuraciones:

### Anotaciones Utilizadas

#### `@JsonIgnore`
Campos completamente ocultos en JSON (ni lectura ni escritura):
- **IDs de entidades**: Para evitar exposición de claves primarias
- **Relaciones sensibles**: Como `user` para prevenir manipulación
- **Campos internos**: Como `enabled` en User

#### `@JsonProperty(access = JsonProperty.Access.WRITE_ONLY)`
Campos que solo se aceptan en requests (no aparecen en responses):
- **password** en User: Se puede enviar para login/registro pero nunca se devuelve

#### `@JsonProperty(access = JsonProperty.Access.READ_ONLY)`
Campos que solo aparecen en responses (se ignoran en requests):
- **ZRN de Credential**: Generado automáticamente por el sistema
- **Timestamps**: createdAt, updatedAt en Namespace
- **Campos calculados**: No pueden ser modificados por el cliente

#### **Campos Read/Write (Sin anotaciones restrictivas)**
Campos completamente accesibles para lectura y escritura:
- **ZRN en CredentialType y Namespace**: Permiten asignación desde frontend
- **Relaciones credential ↔ namespace/credentialType**: Para establecer relaciones por ZRN

### Beneficios de Seguridad

1. **Protección de passwords**: Nunca se exponen en respuestas JSON
2. **Ocultación de IDs**: Las claves primarias no son visibles
3. **Prevención de manipulación**: Campos READ_ONLY no pueden ser alterados
4. **Flexibilidad de asignación**: ZRNs accesibles para establecer relaciones
5. **Referencias limpias**: Las relaciones no causan loops infinitos

### Cambios Importantes en Serialización

⚠️ **Actualización crítica**: Los campos `zrn` en `CredentialType` y `Namespace` ahora son **completamente accesibles** (read/write) para permitir que el frontend pueda:

- **Enviar credentialType.zrn** en requests de creación/actualización
- **Enviar namespace.zrn** en requests de creación  
- **Recibir objetos completos** en responses con toda la información

**Antes (problemático):**
```java
@JsonProperty(access = JsonProperty.Access.READ_ONLY)
private String zrn; // ❌ No se podía escribir desde frontend
```

**Ahora (correcto):**
```java
private String zrn; // ✅ Completamente accesible
```

### Ejemplos de JSON

**Request de registro/login:**
```json
{
  "username": "johndoe",
  "email": "john@example.com",
  "password": "securePassword123"
}
```

**Response de usuario registrado:**
```json
{
  "username": "johndoe", 
  "email": "john@example.com",
  "createdAt": "2025-07-15T21:30:00"
}
```

**Request de creación de credencial:**
```json
{
  "title": "Mi servidor SSH",
  "username": "root",
  "sshPrivateKey": "-----BEGIN PRIVATE KEY-----...",
  "notes": "Servidor de producción",
  "namespace": {
    "zrn": "zrn:zekret:namespace:20250715:uuid-here"
  },
  "credentialType": {
    "zrn": "ssh_username"
  }
}
```

**Response de credencial creada:**
```json
{
  "title": "Mi servidor SSH",
  "zrn": "zrn:zekret:credential:20250715:uuid-here",
  "username": "root",
  "sshPrivateKey": "-----BEGIN PRIVATE KEY-----...",
  "notes": "Servidor de producción",
  "credentialType": {
    "zrn": "ssh_username",
    "name": "SSH Username"
  },
  "namespace": {
    "zrn": "zrn:zekret:namespace:20250715:uuid-here",
    "name": "Production",
    "description": "Production environment"
  }
}
```

## Próximas Funcionalidades Identificadas

Basado en la estructura actual, el sistema está preparado para:

1. **Gestión de Credenciales**:
   - CRUD completo de credenciales
   - Búsqueda y filtrado por tipo
   - Encriptación de datos sensibles

2. **Gestión de Namespaces**:
   - Organización jerárquica de credenciales
   - Permisos por namespace

3. **Tipos de Credenciales**:
   - Contraseñas tradicionales
   - Claves SSH
   - Tokens de API
   - Archivos secretos
   - Notas seguras
   - Códigos 2FA

4. **Autenticación Avanzada**:
   - Recuperación de contraseña
   - Reset de contraseña
   - Refresh de tokens

## Buenas Prácticas Implementadas

### Seguridad
- ✅ Contraseñas encriptadas con BCrypt
- ✅ Autenticación JWT stateless
- ✅ Validación de entrada en controladores
- ✅ Separación de responsabilidades
- ✅ Configuración de CORS
- ✅ Control de serialización JSON (WRITE_ONLY, READ_ONLY)
- ✅ Ocultación de IDs y relaciones sensibles

### Arquitectura
- ✅ Patrón Repository para acceso a datos
- ✅ Patrón Service Layer para lógica de negocio
- ✅ DTOs para transferencia de datos
- ✅ Logging estructurado con SLF4J
- ✅ Configuración externalizada

### Base de Datos
- ✅ Entidades JPA bien definidas
- ✅ Relaciones apropiadas entre entidades
- ✅ Índices únicos donde corresponde
- ✅ DDL automático para desarrollo

## Contribución y Desarrollo

### Flujo de Desarrollo
1. Fork del repositorio
2. Crear rama feature: `git checkout -b feature/nueva-funcionalidad`
3. Commit cambios: `git commit -am 'Agregar nueva funcionalidad'`
4. Push a rama: `git push origin feature/nueva-funcionalidad`
5. Crear Pull Request

### Estándares de Código
- Seguir convenciones de Java
- Documentar métodos públicos
- Logging apropiado
- Manejo de excepciones

## Troubleshooting

### Problemas Comunes

**Error de conexión a MySQL**:
```
Causa: Verificar que MySQL esté corriendo y las credenciales sean correctas
Solución: Revisar variables de entorno y estado del servicio MySQL
```

**JWT Secret Key faltante**:
```
Causa: Variable JWT_SECRET_KEY no configurada
Solución: Agregar clave secreta en .env o variables de entorno
```

**Dependencias Maven**:
```bash
mvn clean install -U  # Forzar actualización de dependencias
```

## Licencia

Este proyecto está bajo la licencia MIT - ver el archivo [LICENSE](../LICENSE) para más detalles.

## � **Resolución de Problemas de Serialización**

### Problema Identificado y Solucionado

Durante el desarrollo se identificó un problema crítico con la deserialización JSON de relaciones en credenciales:

#### ❌ **Problema Original:**
- Los campos `zrn` en `CredentialType` y `Namespace` tenían `@JsonProperty(access = READ_ONLY)`
- El frontend no podía enviar `credentialType.zrn` y `namespace.zrn` en requests
- Error: `"Namespace ZRN is required"` aunque se enviara correctamente en JSON

#### ✅ **Solución Implementada:**
1. **Removido `@JsonProperty(access = READ_ONLY)`** de los campos `zrn`
2. **Permitido acceso completo** (read/write) a los ZRNs de relaciones
3. **Mantenido `@JsonIgnore`** solo en IDs y relación `user`

#### 🎯 **Resultado:**
- ✅ Frontend puede enviar `namespace.zrn` y `credentialType.zrn` 
- ✅ Backend deserializa correctamente las relaciones
- ✅ Responses incluyen objetos completos con toda la información
- ✅ Sistema funciona end-to-end desde frontend hasta base de datos

### Arquitectura de Serialización Actualizada

```java
// ✅ CORRECTO - Acceso completo para asignación por ZRN
@Entity
public class CredentialType {
    @JsonIgnore private Long id;           // Oculto
    private String zrn;                     // ✅ Read/Write
    private String name;                    // ✅ Read/Write
}

@Entity  
public class Namespace {
    @JsonIgnore private Long id;           // Oculto
    private String zrn;                     // ✅ Read/Write  
    private String name;                    // ✅ Read/Write
    @JsonProperty(READ_ONLY) 
    private LocalDateTime createdAt;        // Solo lectura
}

@Entity
public class Credential {
    @JsonIgnore private Long id;           // Oculto
    @JsonProperty(READ_ONLY) 
    private String zrn;                     // Solo lectura (auto-generado)
    private String title;                   // ✅ Read/Write
    private CredentialType credentialType; // ✅ Read/Write (para asignación)
    private Namespace namespace;            // ✅ Read/Write (para asignación)
    @JsonIgnore private User user;         // Oculto (seguridad)
}
```

## �🚀 Optimizaciones de Performance

### AuthenticationUtils - Clase Utilitaria Genérica
Creada una clase utilitaria reutilizable para autenticación JWT que puede ser utilizada en todos los controladores:

```java
@Component
public class AuthenticationUtils {
    
    @Autowired
    private JwtService jwtService;
    
    @Autowired
    private IUserService userService;
    
    /**
     * Extracts and validates the authenticated user from the JWT token in the Authorization header
     * @param authorizationHeader The Authorization header containing the Bearer token
     * @return The authenticated User entity
     * @throws RuntimeException if the header is invalid or user is not found
     */
    public User getAuthenticatedUserFromToken(String authorizationHeader) {
        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
            throw new RuntimeException("Invalid or missing Authorization header");
        }
        
        String token = authorizationHeader.substring(7); // Remove "Bearer " prefix
        String username = jwtService.extractUsername(token);
        
        // Search in all users by username or email
        List<User> allUsers = userService.getAll();
        Optional<User> userOpt = allUsers.stream()
                .filter(user -> user.getUsername().equals(username) || user.getEmail().equals(username))
                .findFirst();
        
        if (userOpt.isPresent()) {
            return userOpt.get();
        } else {
            throw new RuntimeException("User not found: " + username);
        }
    }
    
    /**
     * Validates that the Authorization header has the correct Bearer format
     */
    public boolean isValidAuthorizationHeader(String authorizationHeader) {
        return authorizationHeader != null && authorizationHeader.startsWith("Bearer ");
    }
    
    /**
     * Extracts the token from the Authorization header
     */
    public String extractTokenFromHeader(String authorizationHeader) {
        if (!isValidAuthorizationHeader(authorizationHeader)) {
            throw new RuntimeException("Invalid Authorization header format");
        }
        return authorizationHeader.substring(7);
    }
}
```

**Ventajas:**
- **Reutilizable:** Misma lógica de autenticación en todos los controladores
- **Centralizada:** Un solo lugar para modificar la lógica de autenticación  
- **Flexible:** Busca usuarios por username O email
- **Validación Robusta:** Múltiples métodos de validación de headers
- **Testeable:** Fácil de mockear y probar independientemente
- **Mantenible:** Cambios en autenticación se propagan automáticamente
- **Logging Integrado:** Trazabilidad completa de autenticación

### Queries Optimizadas con Spring Data JPA
Implementación de consultas específicas usando **convenciones de nomenclatura automática** de Spring Data JPA:

```java
public interface INamespaceRepo extends IGenericRepo<Namespace, Long> {
    
    // Spring Data JPA genera automáticamente:
    // SELECT n FROM Namespace n WHERE n.user.id = :userId
    List<Namespace> findByUserId(Long userId);
    
    // Spring Data JPA genera automáticamente:
    // SELECT n FROM Namespace n WHERE n.zrn = :zrn AND n.user.id = :userId
    Optional<Namespace> findByZrnAndUserId(String zrn, Long userId);
    
    // Spring Data JPA genera automáticamente:
    // SELECT COUNT(n) > 0 FROM Namespace n WHERE n.zrn = :zrn AND n.user.id = :userId
    boolean existsByZrnAndUserId(String zrn, Long userId);
}
```

### Métodos de Servicio Optimizados
Servicios que utilizan las consultas optimizadas del repository:

```java
@Override
public List<Namespace> getNamespacesByUserId(Long userId) {
    return namespaceRepo.findByUserId(userId);
}

@Override
public Optional<Namespace> getNamespaceByZrnAndUserId(String zrn, Long userId) {
    return namespaceRepo.findByZrnAndUserId(zrn, userId);
}
```

**Beneficios de Performance:**
- **Reducción de Memoria:** No carga todos los namespaces en memoria
- **Queries Específicas:** Solo trae los datos necesarios de la base de datos
- **Índices Optimizados:** Consultas que aprovechan índices de user_id y zrn
- **Escalabilidad:** Performance constante independiente del crecimiento de datos

### CredentialController (Optimizado)

**Ruta base:** `/v1/credentials`
**Autenticación:** Requerida en header `Authorization: Bearer <token>`

**Características Clave:**
- ✅ **Asignación de Namespace:** Cada credential debe ser asignado a un namespace del usuario
- ✅ **Asignación de Credential Type:** Cada credential debe especificar un tipo vía ZRN
- ✅ **Namespace Inmutable:** Una vez asignado, el namespace no puede cambiarse
- ✅ **Credential Type Mutable:** El tipo de credencial puede actualizarse vía ZRN
- ✅ **Queries Optimizadas:** Consultas específicas por usuario, ZRN y namespace
- ✅ **Validación de Pertenencia:** Verifica que namespace y credential pertenezcan al usuario
- ✅ **Validación de Tipo:** Verifica que el credential type exista en el sistema
- ✅ **AuthenticationUtils:** Reutiliza autenticación genérica

**Endpoints REST Optimizados:**

#### 1. Crear Credential
- **Endpoint:** `POST /v1/credentials`
- **Descripción:** Crea una nueva credencial asignada a un namespace del usuario
- **Body Request:**
```json
{
  "title": "Production SSH Server",
  "username": "admin",
  "sshPrivateKey": "-----BEGIN PRIVATE KEY-----\n...",
  "notes": "Main production server access",
  "namespace": {
    "zrn": "zrn:namespace:production-xyz789"
  },
  "credentialType": {
    "zrn": "ssh_username"
  }
}
```
- **Response:**
```json
{
  "data": {
    "zrn": "zrn:credential:production-ssh-abc123",
    "title": "Production SSH Server",
    "username": "admin",
    "sshPrivateKey": "-----BEGIN PRIVATE KEY-----\n...",
    "notes": "Main production server access",
    "createdAt": "2025-07-15T19:30:45.123456",
    "updatedAt": "2025-07-15T19:30:45.123456",
    "credentialType": {
      "zrn": "ssh_username",
      "name": "SSH Username"
    }
  },
  "message": "Credential created successfully",
  "success": true,
  "statusCode": 201
}
```

#### 2. Actualizar Credential
- **Endpoint:** `PUT /v1/credentials/{zrn}`
- **Descripción:** Actualiza una credencial existente (namespace no puede cambiarse, credential type sí)
- **Body Request:**
```json
{
  "title": "Updated Production SSH Server",
  "username": "root",
  "sshPrivateKey": "-----BEGIN PRIVATE KEY-----\nUPDATED_KEY_CONTENT...",
  "notes": "Updated server access credentials",
  "credentialType": {
    "zrn": "username_password"
  }
}
```

#### 3. Obtener Credential por ZRN
- **Endpoint:** `GET /v1/credentials/{zrn}`
- **Descripción:** Obtiene una credencial específica por su ZRN

#### 4. Listar Todas las Credentials
- **Endpoint:** `GET /v1/credentials`
- **Descripción:** Obtiene todas las credenciales del usuario autenticado
- **Response:**
```json
{
  "data": [
    {
      "zrn": "zrn:credential:production-ssh-abc123",
      "title": "Production SSH Server",
      "username": "admin",
      "notes": "Main production server access"
    }
  ],
  "message": "Credentials retrieved successfully",
  "success": true,
  "statusCode": 200
}
```

#### 5. Eliminar Credential
- **Endpoint:** `DELETE /v1/credentials/{zrn}`
- **Descripción:** Elimina físicamente una credencial del usuario

#### 6. Listar Credentials por Namespace
- **Endpoint:** `GET /v1/credentials/namespace/{namespaceZrn}`
- **Descripción:** Obtiene todas las credenciales de un namespace específico del usuario
- **Response:**
```json
{
  "data": [
    {
      "zrn": "zrn:credential:prod-db-def456",
      "title": "Production Database",
      "username": "dbuser",
      "password": "encrypted_password",
      "notes": "Main production database",
      "credentialType": {
        "zrn": "username_password",
        "name": "Username/Password"
      }
    },
    {
      "zrn": "zrn:credential:prod-ssh-abc123",
      "title": "Production SSH Server", 
      "username": "admin",
      "sshPrivateKey": "-----BEGIN PRIVATE KEY-----\n...",
      "notes": "Main production server access",
      "credentialType": {
        "zrn": "ssh_username",
        "name": "SSH Username"
      }
    }
  ],
  "message": "Credentials retrieved successfully for namespace",
  "success": true,
  "statusCode": 200
}
```

### Ejemplos de Diferentes Tipos de Credenciales

#### Credential con Username/Password
```json
{
  "title": "Database Connection",
  "username": "dbuser",
  "password": "secure_password_123",
  "notes": "Production database credentials",
  "namespace": {
    "zrn": "zrn:namespace:production-xyz789"
  },
  "credentialType": {
    "zrn": "username_password"
  }
}
```

#### Credential con SSH Key
```json
{
  "title": "Server SSH Access",
  "username": "admin",
  "sshPrivateKey": "-----BEGIN RSA PRIVATE KEY-----\nMIIEpAIBAAKCAQEA...",
  "notes": "SSH access to production server",
  "namespace": {
    "zrn": "zrn:namespace:production-xyz789"
  },
  "credentialType": {
    "zrn": "ssh_username"
  }
}
```

#### Credential con Secret Text
```json
{
  "title": "API Token",
  "secretText": "sk_live_abc123def456ghi789...",
  "notes": "Stripe API production token",
  "namespace": {
    "zrn": "zrn:namespace:production-xyz789"
  },
  "credentialType": {
    "zrn": "secret_text"
  }
}
```

#### Credential con File
```json
{
  "title": "SSL Certificate",
  "fileName": "server.crt",
  "fileContent": "-----BEGIN CERTIFICATE-----\nMIIDXTCCAkWgAwIBAgIJA...",
  "notes": "Production SSL certificate",
  "namespace": {
    "zrn": "zrn:namespace:production-xyz789"
  },
  "credentialType": {
    "zrn": "file"
  }
}
```

### Tipos de Credenciales Disponibles

El sistema maneja diferentes tipos de credenciales identificados por ZRN:

| ZRN | Nombre | Campos Requeridos | Descripción |
|-----|--------|------------------|-------------|
| `username_password` | Username/Password | `username`, `password` | Credenciales tradicionales de usuario y contraseña |
| `ssh_username` | SSH Username | `username`, `sshPrivateKey` | Acceso SSH con clave privada |
| `secret_text` | Secret Text | `secretText` | Tokens, códigos, o texto secreto |
| `file` | File | `fileName`, `fileContent` | Archivos como certificados, llaves, configuraciones |

**Campos Opcionales para Todos los Tipos:**
- `title`: Título descriptivo (requerido)
- `notes`: Notas adicionales sobre la credencial
- `namespace`: Namespace de organización (requerido)
- `credentialType`: Tipo de credencial (requerido)

### Inicialización Automática de Credential Types

#### Sistema de Carga Predefinida
Los tipos de credenciales se cargan automáticamente al arrancar la aplicación mediante el componente `DataInitializer`:

**Proceso de Inicialización:**
1. **Lectura de Configuración**: Carga tipos desde `data-initializer.yml`
2. **Validación de Existencia**: Verifica si el tipo ya existe por ZRN
3. **Inserción o Actualización**: Crea nuevos tipos o actualiza nombres existentes
4. **Logging**: Registra el proceso para auditoria

**Configuración Actual (`data-initializer.yml`):**
```yaml
data-initializer:
  credentialType:
    - zrn: username_password
      name: Username/Password
    - zrn: ssh_username  
      name: SSH Username
    - zrn: secret_text
      name: Secret Text
    - zrn: file
      name: File
```

**Ventajas del Sistema:**
- ✅ **Automático**: No requiere intervención manual en despliegues
- ✅ **Idempotente**: Puede ejecutarse múltiples veces sin duplicar datos
- ✅ **Actualizable**: Permite modificar nombres de tipos existentes
- ✅ **Extensible**: Fácil agregar nuevos tipos modificando el YAML
- ✅ **Resiliente**: Maneja errores de configuración graciosamente

**Agregar Nuevos Tipos:**
Para agregar un nuevo tipo de credencial:
1. Modificar `data-initializer.yml` agregando el nuevo tipo
2. Reiniciar la aplicación (se carga automáticamente)
3. El nuevo tipo estará disponible inmediatamente para uso

**Ejemplo de Nuevo Tipo:**
```yaml
- zrn: api_key
  name: API Key
```

### Optimizaciones de Credential Repository
Consultas específicas usando convenciones de Spring Data JPA:

```java
public interface ICredentialRepo extends IGenericRepo<Credential, Long> {
    
    // SELECT c FROM Credential c WHERE c.user.id = :userId
    List<Credential> findByUserId(Long userId);
    
    // SELECT c FROM Credential c WHERE c.zrn = :zrn AND c.user.id = :userId
    Optional<Credential> findByZrnAndUserId(String zrn, Long userId);
    
    // SELECT c FROM Credential c WHERE c.namespace.zrn = :namespaceZrn AND c.user.id = :userId
    List<Credential> findByNamespaceZrnAndUserId(String namespaceZrn, Long userId);
    
    // SELECT COUNT(c) > 0 FROM Credential c WHERE c.zrn = :zrn AND c.user.id = :userId
    boolean existsByZrnAndUserId(String zrn, Long userId);
}
```

### Relación Credential ↔ Namespace
Cada credencial está asignada a un namespace específico:

```java
@Entity
public class Credential {
    @ManyToOne
    @JoinColumn(name = "id_namespace", nullable = true)
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private Namespace namespace;
    
    // ...otros campos
}
```

**Reglas de Negocio:**
- ✅ **Asignación Requerida:** Cada credential debe especificar `namespace.zrn` y `credentialType.zrn` al crearse
- ✅ **Validación de Pertenencia:** El namespace debe pertenecer al usuario autenticado
- ✅ **Validación de Tipo:** El credential type debe existir en el sistema
- ❌ **Namespace Inmutable:** Una vez asignado, el namespace no puede cambiarse en updates
- ✅ **Credential Type Mutable:** El tipo de credencial puede actualizarse en updates
- ✅ **Filtrado Automático:** Todas las consultas filtran por usuario automáticamente

---

## 🔄 **Mejoras Recientes Implementadas**

### Optimización de Relaciones y Serialización JSON

#### 1. **Relación OneToMany en Namespace**
- **Agregado**: Relación `@OneToMany` de `Namespace` hacia `Credential`
- **Configuración**: `cascade = {CascadeType.ALL}, orphanRemoval = true`
- **Propósito**: Acceso directo a credenciales desde namespace para estadísticas
- **Impacto**: Permite obtener el conteo de credenciales automáticamente

```java
@OneToMany(mappedBy = "namespace", cascade = {CascadeType.ALL}, orphanRemoval = true)
@JsonProperty(access = JsonProperty.Access.READ_ONLY)
private List<Credential> credentials;
```

#### 2. **Optimización de Serialización JSON**
- **Modificado**: Campo `namespace` en `Credential` ahora es `WRITE_ONLY`
- **Beneficio**: Evita referencias circulares en respuestas JSON
- **Resultado**: Mejora del rendimiento y estructura de datos más limpia

```java
@ManyToOne
@JoinColumn(name = "id_namespace", nullable = true)
@JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
private Namespace namespace;
```

### Impacto en la Arquitectura

#### **Gestión de Cascadas**
- Las operaciones en `Namespace` se propagan automáticamente a sus `Credential`
- Eliminación segura: credenciales huérfanas se eliminan automáticamente
- Consistencia de datos garantizada a nivel de JPA

#### **API Response Optimization**
- **Namespace responses**: Incluyen lista de credenciales para conteo
- **Credential responses**: Ya no incluyen namespace completo (evita ciclos)
- **Frontend benefits**: Datos estructurados optimizados para UI

### Estado Actual de Integración

✅ **Completado**:
- Relaciones bidireccionales optimizadas
- Eliminación de referencias circulares JSON
- Cascadas automáticas configuradas
- Serialización eficiente implementada

📋 **Beneficios Obtenidos**:
- Mejor rendimiento en respuestas API
- Estructura de datos más consistente
- Eliminación automática de datos huérfanos
- Integración frontend simplificada

---

## 🔐 **Arquitectura de Seguridad JWT**

### Implementación de Autenticación

#### 1. **JwtService - Gestión de Tokens**
Servicio centralizado para manejo de tokens JWT con soporte para access y refresh tokens:

```java
@Service
public class JwtService {
    
    // Generación de tokens con diferentes tiempos de expiración
    public String generateAccessToken(User usuario)    // Configurable via application.properties
    public String generateRefreshToken(User usuario)   // Configurable via application.properties
    
    // Validación de tokens con verificación en base de datos
    public boolean isValid(String token, UserDetails user)
    public boolean isValidRefreshToken(String token, User usuario)
    
    // Extracción de información del token
    public String extractUsername(String token)
    public <T> T extractClaim(String token, Function<Claims, T> resolver)
}
```

**Características Implementadas:**
- ✅ **Doble Token System**: Access token (corta duración) + Refresh token (larga duración)
- ✅ **Revocación de Tokens**: Validación con base de datos para logout instantáneo
- ✅ **Claims Personalizados**: Username embebido en token para extracción rápida
- ✅ **Configuración Externa**: Tiempos de expiración y secret key via properties

#### 2. **JwtAuthenticationFilter - Interceptor de Requests**
Filtro que procesa cada request HTTP para validar autenticación:

```java
@Component
public final class JwtAuthenticationFilter extends OncePerRequestFilter {
    
    @Override
    protected void doFilterInternal(HttpServletRequest request, 
                                   HttpServletResponse response, 
                                   FilterChain filterChain) {
        // 1. Extrae Bearer token del header Authorization
        // 2. Valida formato y existencia del token
        // 3. Extrae username del token JWT
        // 4. Valida token contra base de datos
        // 5. Establece contexto de seguridad si es válido
        // 6. Continúa cadena de filtros
    }
}
```

**Proceso de Validación:**
1. **Header Parsing**: Busca `Authorization: Bearer <token>`
2. **Token Extraction**: Extrae JWT del header (substring(7))
3. **Username Resolution**: Decodifica username del token sin validar signature
4. **Database Validation**: Verifica que token no esté marcado como `loggedOut`
5. **Security Context**: Establece `UsernamePasswordAuthenticationToken` si válido
6. **Logging**: Detalla cada paso para debugging y auditoria

#### 3. **AuthenticationUtils - Utilidad de Contexto**
Clase utilitaria para acceso al usuario autenticado desde cualquier parte del código:

```java
@Component
public class AuthenticationUtils {
    
    public User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        if (authentication != null && authentication.isAuthenticated() && 
            !(authentication instanceof AnonymousAuthenticationToken)) {
            
            Object principal = authentication.getPrincipal();
            if (principal instanceof UserPrincipal userPrincipal) {
                return userPrincipal.getUser();
            }
        }
        throw new RuntimeException("Usuario no autenticado");
    }
}
```

**Beneficios de la Implementación:**
- ✅ **Acceso Centralizado**: Un punto para obtener el usuario actual en cualquier controller/service
- ✅ **Type Safety**: Retorna directamente entidad `User` en lugar de `Object`
- ✅ **Exception Handling**: Manejo consistente de usuarios no autenticados
- ✅ **Principal Validation**: Verifica el tipo correcto de principal (`UserPrincipal`)

### Integración con Controllers

Todos los controllers utilizan `AuthenticationUtils` para operaciones específicas del usuario:

```java
@RestController
public class NamespaceController {
    
    @Autowired
    private AuthenticationUtils authUtils;
    
    @GetMapping
    public ResponseEntity<GenericResponseDTO<List<NamespaceDTO>>> listar() {
        User currentUser = authUtils.getCurrentUser();
        // Operaciones específicas del usuario autenticado
    }
}
```

### Configuración de Seguridad

Properties requeridas en `application.properties`:

```properties
# JWT Configuration
security.jwt.secret-key=base64_encoded_secret_key_here
security.jwt.access-token-expiration=86400000   # 24 horas en milliseconds
security.jwt.refresh-token-expiration=604800000 # 7 días en milliseconds
```

### Estado de Implementación

✅ **Completado**:
- Sistema completo de autenticación JWT
- Filtro de autenticación por request
- Utilidad de acceso al usuario actual
- Revocación segura de tokens
- Logging y auditoria completos

📋 **Características de Seguridad**:
- Tokens con expiración configurable
- Revocación inmediata via logout
- Validación en cada request
- Context de seguridad Spring estándar
- Manejo de errores robusto

---

*Documentación actualizada: Enero 2025*