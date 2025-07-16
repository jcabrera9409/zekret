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

## Arquitectura del Proyecto

### Estructura de Directorios

```
src/main/java/com/zekret/
├── ZekretBackApplication.java          # Clase principal de Spring Boot
├── configuration/
│   ├── CORS.java                       # Configuración CORS
│   └── SecurityConfig.java             # Configuración de seguridad
├── controller/
│   └── UserController.java             # Controlador REST para usuarios
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
  - `id`: Long (PK, auto-generated)
  - `email`: String (único, requerido)
  - `username`: String (requerido)
  - `password`: String (encriptado con BCrypt)
  - `createdAt`: LocalDateTime
  - `enabled`: boolean (para activación de cuenta)

#### Credential
- **Propósito**: Almacena las credenciales de los usuarios
- **Campos clave**:
  - `id`: Long (PK)
  - `title`: String (título descriptivo)
  - `zrn`: String (Zekret Resource Name, único)
  - `username`: String (opcional)
  - `password`: String (opcional)
  - `sshPrivateKey`: TEXT (opcional)
  - `secretText`: String (opcional)
  - `fileName`: String (opcional)
  - `fileContent`: TEXT (opcional)
  - `notes`: TEXT (opcional)
- **Relaciones**:
  - `credentialType`: ManyToOne → CredentialType
  - `user`: ManyToOne → User

#### CredentialType
- **Propósito**: Define tipos de credenciales (password, ssh, token, archivo, etc.)
- **Campos**:
  - `id`: Long (PK)
  - `zrn`: String (único)
  - `name`: String

#### Namespace
- **Propósito**: Agrupa credenciales por categorías/proyectos
- **Campos**:
  - `id`: Long (PK)
  - `name`: String
  - `zrn`: String
  - `description`: String
  - `createdAt`, `updatedAt`: LocalDateTime
- **Relación**: `user`: ManyToOne → User

#### Token
- **Propósito**: Gestiona tokens JWT para autenticación
- **Campos**:
  - `id`: Long (PK)
  - `accessToken`: String (único)
  - `refreshToken`: String (único)
  - `loggedOut`: boolean
- **Relación**: `user`: ManyToOne → User

### Concepto ZRN (Zekret Resource Name)
- Sistema de identificación único similar a ARN de AWS
- Presente en: Credential, CredentialType, Namespace
- Permite identificación única de recursos en el sistema

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

## API REST

### Endpoints Disponibles

#### Usuarios (`/v1/users`)
- **POST** `/register`: Registro de nuevos usuarios
  - Valida email y username únicos
  - Encripta password con BCrypt
  - Retorna APIResponseDTO con usuario creado

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