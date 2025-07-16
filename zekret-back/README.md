# Zekret Backend

Este proyecto es el backend para una aplicación web que permite a los usuarios guardar y organizar credenciales personales agrupadas por namespaces. Es un sistema de gestión de credenciales seguro basado en Spring Boot con autenticación JWT.

## Versión y Tecnologías

- **Java 21** (definido en pom.xml)
- **Spring Boot 3.3.5**
- **Spring Security** con autenticación JWT
- **MySQL** como base de datos
- **Maven** para gestión de dependencias
- **JPA/Hibernate** para ORM
- **JJWT 0.12.3** para manejo de tokens JWT
- **Jackson** para serialización JSON con control de acceso

## Arquitectura del Proyecto

### Estructura de Directorios

```
src/main/java/com/zekret/
├── ZekretBackApplication.java          # Clase principal de Spring Boot
├── configuration/
│   ├── CORS.java                       # Configuración CORS
│   └── SecurityConfig.java             # Configuración de seguridad
├── controller/
│   ├── UserController.java             # Controlador REST para usuarios
│   ├── AuthenticationController.java   # Controlador REST para autenticación
│   └── NamespaceController.java        # Controlador REST para namespaces
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
│   ├── ICredentialTypeRepo.java
│   ├── INamespaceRepo.java
│   └── ITokenRepo.java
├── security/
│   ├── JwtAuthenticationFilter.java    # Filtro JWT personalizado
│   └── CustomLogoutHandler.java        # Manejador de logout
├── util/
│   └── ZrnGenerator.java               # Utilidad para generar ZRN únicos
└── service/
    ├── ICRUD.java                      # Interface CRUD genérica
    ├── IUserService.java
    ├── ICredentialService.java
    ├── ICredentialTypeService.java
    ├── INamespaceService.java
    └── impl/                           # Implementaciones de servicios
        ├── CRUDImpl.java
        ├── UserServiceImpl.java
        ├── CredentialServiceImpl.java
        ├── CredentialTypeServiceImpl.java
        ├── NamespaceServiceImpl.java
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
  - `createdAt`: LocalDateTime - `@JsonProperty(READ_ONLY)`
  - `enabled`: boolean (para activación de cuenta) - `@JsonIgnore`
- **Serialización JSON**: 
  - Password solo se acepta en requests (WRITE_ONLY)
  - ID y enabled ocultos en respuestas
  - CreatedAt solo se incluye en respuestas (READ_ONLY)

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
- **Relaciones**:
  - `credentialType`: ManyToOne → CredentialType
  - `user`: ManyToOne → User - `@JsonIgnore`
- **Serialización JSON**:
  - ID y relación user ocultos en respuestas
  - ZRN se genera automáticamente (READ_ONLY)

#### CredentialType
- **Propósito**: Define tipos de credenciales (password, ssh, token, archivo, etc.)
- **Campos**:
  - `id`: Long (PK) - `@JsonIgnore`
  - `zrn`: String (único, formato slug) - `@JsonProperty(READ_ONLY)`
  - `name`: String
- **Serialización JSON**:
  - ID oculto en respuestas
  - ZRN generado automáticamente como slug (READ_ONLY)

#### Namespace
- **Propósito**: Agrupa credenciales por categorías/proyectos
- **Campos**:
  - `id`: Long (PK) - `@JsonIgnore`
  - `name`: String
  - `zrn`: String - `@JsonProperty(READ_ONLY)`
  - `description`: String
  - `createdAt`, `updatedAt`: LocalDateTime - `@JsonProperty(READ_ONLY)`
- **Relación**: `user`: ManyToOne → User - `@JsonIgnore`
- **Serialización JSON**:
  - ID y relación user ocultos
  - ZRN y timestamps son READ_ONLY (generados automáticamente)

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

// En CredentialTypeServiceImpl
@Override  
public CredentialType save(CredentialType credentialType) {
    if (credentialType.getZrn() == null || credentialType.getZrn().isEmpty()) {
        credentialType.setZrn(ZrnGenerator.generateCredentialTypeZrn(credentialType.getName()));
    }
    return super.save(credentialType);
}
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

- **POST** `/register`: Crear un nuevo namespace
  - Genera ZRN automáticamente
  - Asigna namespace al usuario autenticado
  - Establece timestamps de creación

- **PUT** `/{zrn}`: Modificar namespace existente
  - Solo permite modificar `name` y `description`
  - Actualiza `updatedAt` automáticamente
  - Valida pertenencia al usuario autenticado

- **GET** `/{zrn}`: Obtener namespace por ZRN
  - Busca namespace específico del usuario autenticado
  - Retorna error 404 si no existe o no pertenece al usuario

- **GET** `/`: Listar todos los namespaces del usuario
  - Filtra automáticamente por usuario autenticado
  - Retorna lista completa de namespaces del usuario

- **DELETE** `/{zrn}`: Eliminar namespace físicamente
  - Eliminación permanente de la base de datos
  - Valida pertenencia al usuario antes de eliminar
  - Retorna APIResponseDTO con AuthenticationResponseDTO

### Gestión de Namespaces

**Nota**: Todos los ejemplos de namespace requieren el header de autorización:
`Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...`

#### Crear Namespace
```bash
curl -X POST http://localhost:8080/v1/namespaces/register \
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
    // Ensure the user relationship is properly set by ID
    if (entity.getUser() != null && entity.getUser().getId() != null) {
        Optional<User> user = userRepo.findById(entity.getUser().getId());
        if (user.isPresent()) {
            entity.setUser(user.get());
        } else {
            throw new RuntimeException("User not found with ID: " + entity.getUser().getId());
        }
    }
    return super.register(entity);
}
```

**Ventajas:**
- Previene errores de entidades transitorias
- Garantiza la integridad referencial
- Manejo explícito de relaciones @ManyToOne y @OneToMany
- Validación de existencia de entidades relacionadas

## 📋 Controladores REST Estándar

### NamespaceController (Optimizado)

**Ruta base:** `/api/v1/namespaces`
**Autenticación:** Requerida en header `Authorization: Bearer <token>`

**Optimizaciones Implementadas:**
- ✅ **AuthenticationUtils:** Autenticación genérica reutilizable
- ✅ **Queries Específicas:** Consultas optimizadas por usuario y ZRN
- ✅ **Performance Mejorada:** No carga datos innecesarios en memoria
- ✅ **Escalabilidad:** Performance constante independiente del crecimiento

**Endpoints REST Optimizados:**

#### 1. Crear Namespace
- **Endpoint:** `POST /api/v1/namespaces`
- **Descripción:** Crea un nuevo namespace para el usuario autenticado
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
    "id": 1,
    "zrn": "zrn:namespace:development-abc123",
    "name": "development", 
    "description": "Development environment namespace",
    "createdAt": "2025-01-15T10:30:00",
    "updatedAt": "2025-01-15T10:30:00"
  },
  "message": "Namespace created successfully",
  "success": true,
  "statusCode": 201
}
```

#### 2. Actualizar Namespace
- **Endpoint:** `PUT /api/v1/namespaces/{zrn}`
- **Descripción:** Actualiza un namespace existente
- **Body Request:**
```json
{
  "name": "production",
  "description": "Production environment namespace"
}
```

#### 3. Obtener Namespace por ZRN
- **Endpoint:** `GET /api/v1/namespaces/{zrn}`
- **Descripción:** Obtiene un namespace específico por su ZRN

#### 4. Listar Todos los Namespaces
- **Endpoint:** `GET /api/v1/namespaces`
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
- **Endpoint:** `DELETE /api/v1/namespaces/{zrn}`
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
- **Relaciones de entidades**: Para prevenir referencias circulares
- **Campos internos**: Como `enabled` en User

#### `@JsonProperty(access = JsonProperty.Access.WRITE_ONLY)`
Campos que solo se aceptan en requests (no aparecen en responses):
- **password** en User: Se puede enviar para login/registro pero nunca se devuelve

#### `@JsonProperty(access = JsonProperty.Access.READ_ONLY)`
Campos que solo aparecen en responses (se ignoran en requests):
- **ZRN**: Generados automáticamente por el sistema
- **Timestamps**: createdAt, updatedAt
- **Campos calculados**: No pueden ser modificados por el cliente

### Beneficios de Seguridad

1. **Protección de passwords**: Nunca se exponen en respuestas JSON
2. **Ocultación de IDs**: Las claves primarias no son visibles
3. **Prevención de manipulación**: Campos READ_ONLY no pueden ser alterados
4. **Referencias limpias**: Las relaciones no causan loops infinitos

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
  "notes": "Servidor de producción"
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
    "zrn": "ssh_credential",
    "name": "SSH Credential"
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

## 🚀 Optimizaciones de Performance

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
     * Extracts and validates the authenticated user from JWT token
     */
    public User getAuthenticatedUserFromToken(String authorizationHeader) {
        // Validation and token extraction logic
        String token = authorizationHeader.substring(7);
        String username = jwtService.extractUsername(token);
        
        // User lookup and validation
        return userService.findByUsernameOrEmail(username);
    }
}
```

**Ventajas:**
- **Reutilizable:** Misma lógica de autenticación en todos los controladores
- **Centralizada:** Un solo lugar para modificar la lógica de autenticación
- **Testeable:** Fácil de mockear y probar independientemente
- **Mantenible:** Cambios en autenticación se propagan automáticamente

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

**Ventajas de usar Convenciones JPA:**
- ✅ **Código más limpio:** No necesita `@Query` ni `@Param`
- ✅ **Menos propenso a errores:** Spring genera las consultas automáticamente
- ✅ **Autocompletado:** IDEs pueden ayudar con la nomenclatura
- ✅ **Mantenimiento:** Cambios en entidades se reflejan automáticamente
- ✅ **Estándar:** Sigue las convenciones de Spring Data JPA
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

### Comparación de Performance

#### ❌ **Antes (Ineficiente):**
```java
// Carga TODOS los namespaces de TODOS los usuarios
List<Namespace> allNamespaces = namespaceService.getAll(); // SELECT * FROM namespace

// Filtra en memoria Java (costoso)
List<Namespace> userNamespaces = allNamespaces.stream()
    .filter(ns -> ns.getUser().getId().equals(authenticatedUser.getId()))
    .toList();
```

#### ✅ **Ahora (Optimizado):**
```java
// Solo carga namespaces del usuario específico
List<Namespace> userNamespaces = namespaceService.getNamespacesByUserId(authenticatedUser.getId());
// SELECT n FROM Namespace n WHERE n.user.id = :userId
```

**Mejora de Performance:**
- **Tiempo de consulta:** Reducido de O(n) a O(log n) con índices
- **Uso de memoria:** Reducido dramáticamente (solo datos del usuario)
- **Transferencia de red:** Menor cantidad de datos transferidos
- **Escalabilidad:** Performance no se degrada con crecimiento de usuarios