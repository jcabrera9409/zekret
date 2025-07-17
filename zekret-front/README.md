# Zekret Frontend

Aplicación frontend para el sistema de gestión de credenciales Zekret, desarrollada con Angular 17.

## 📋 Información General

- **Nombre del proyecto**: zekret-front
- **Versión**: 0.0.0
- **Framework**: Angular 17.3.0
- **Arquitectura**: Standalone Components (sin NgModules)
- **Estilo**: TailwindCSS + Angular Material
- **Backend**: Integración con API REST (Spring Boot)

## 🛠️ Tecnologías y Dependencias

### Dependencias Principales
- **Angular Core**: ^17.3.0
- **Angular Material**: ^17.3.10 (UI Components)
- **Angular CDK**: ^17.3.10 (Component Development Kit)
- **TailwindCSS**: ^3.4.17 (Utility-first CSS)
- **RxJS**: ~7.8.0 (Reactive Extensions)
- **TypeScript**: ~5.4.2
- **@auth0/angular-jwt**: ^5.2.0 (JWT Authentication)

### Herramientas de Desarrollo
- **Angular CLI**: ^17.3.11
- **Karma + Jasmine**: Testing framework
- **PostCSS + Autoprefixer**: CSS processing

## 🏗️ Arquitectura del Proyecto

### Estructura de Carpetas
```
src/
├── app/
│   ├── _model/           # Modelos y tipos de datos
│   │   ├── user.ts       # Modelo de usuario
│   │   ├── credential.ts # Modelo de credenciales
│   │   ├── namespace.ts  # Modelo de namespaces
│   │   ├── dto.ts        # DTOs para API responses
│   │   └── credential-type.ts # Tipos de credenciales
│   ├── _service/         # Servicios de la aplicación
│   │   ├── auth.service.ts     # Autenticación
│   │   ├── user.service.ts     # Gestión de usuarios
│   │   └── generic.service.ts  # Servicio genérico CRUD
│   ├── util/             # Utilidades y helpers
│   │   └── util.ts       # JWT utilities y métodos helper
│   ├── modals/           # Componentes de diálogos/modales
│   ├── pages/            # Páginas principales de la aplicación
│   │   ├── login/        # Página de autenticación
│   │   ├── layout/       # Layout principal
│   │   ├── header/       # Componente de encabezado
│   │   ├── namespace/    # Gestión de namespaces
│   │   └── stats-namespace/  # Estadísticas de namespaces
│   ├── app.component.*   # Componente raíz
│   ├── app.config.ts     # Configuración de la aplicación
│   └── app.routes.ts     # Configuración de rutas
├── environments/         # Configuraciones por ambiente
├── assets/               # Recursos estáticos
├── custom-theme.scss     # Tema personalizado de Angular Material
├── styles.css           # Estilos globales (incluye TailwindCSS)
└── main.ts              # Punto de entrada de la aplicación
```

## 🔐 Sistema de Autenticación

### JWT Implementation
- **JWT Helper**: @auth0/angular-jwt para manejo de tokens
- **Token Storage**: LocalStorage con configuración de dominio
- **Interceptors**: Configuración automática para requests autenticados
- **Token Validation**: Métodos utilitarios para validación y extracción de datos

### Configuración de Seguridad
```typescript
// environment.development.ts
{
  apiUrl: 'http://localhost:8080/v1',
  token_name: 'access_token',
  domains: ['localhost:8080'],
  disallowedRoutes: [...]
}
```

## 🎯 Funcionalidades Principales

### 1. Autenticación y Autorización
- **Componente**: `LoginComponent`
- **Servicio**: `AuthService`
- **Características**:
  - Formularios reactivos para login y registro
  - Integración con JWT para autenticación
  - Validación de campos con Angular Validators
  - Manejo de estados de carga y errores
  - Navegación automática después del login
  - Gestión de tokens en localStorage

### 2. Gestión de Usuarios
- **Servicio**: `UserService` (extiende GenericService)
- **Modelo**: `User` con propiedades: email, username, password, enabled
- **Integración**: API REST para registro y gestión de usuarios

### 3. Gestión de Namespaces
- **Componente principal**: `NamespaceComponent`
- **Modelo**: `Namespace` con ZRN, nombre, descripción y timestamps
- **Subcomponentes**:
  - `IndexNamespaceComponent`: Lista y gestión de namespaces
  - `StatsNamespaceComponent`: Estadísticas de namespaces
  - `CredentialsComponent`: Gestión de credenciales por namespace

### 4. Gestión de Credenciales
- **Modelo**: `Credential` con soporte para múltiples tipos
- **Tipos de credenciales soportados**:
  - Username/Password
  - SSH Private Key
  - Secret Text
  - File Content
- **Características**:
  - ZRN (Zekret Resource Name) como identificador único
  - Timestamps de creación y actualización
  - Asociación con namespace y tipo de credencial
- **Componentes**:
  - `CredentialEditionDialogComponent`: Edición de credenciales
  - `CredentialDetailDialogComponent`: Visualización de detalles

### 5. Sistema de Servicios
- **GenericService**: Servicio base con operaciones CRUD
  - `getAll()`: Obtener todos los recursos
  - `getByZrn(zrn)`: Obtener por identificador ZRN
  - `register(entity)`: Crear nuevo recurso
  - `modify(entity)`: Actualizar recurso
  - `delete(id)`: Eliminar recurso
- **Especialización**: UserService extiende GenericService para funcionalidades específicas

### 6. Sistema de DTOs y Modelos
- **APIResponseDTO**: Estructura estándar para respuestas de API
- **AuthenticationResponseDTO**: Tokens de acceso y refresh
- **CredentialTypeDTO**: Definición de tipos de credenciales
- **Modelos de Entidad**: User, Credential, Namespace con propiedades completas

### 7. Utilidades JWT
- **UtilMethods**: Clase con métodos estáticos para:
  - Gestión de tokens JWT
  - Extracción de campos del token
  - Validación de autenticación
  - Decodificación segura de tokens

## 🎨 Sistema de Estilos

### Angular Material Theme
- **Paleta primaria**: Indigo
- **Paleta de acento**: Pink (A200, A100, A400)
- **Paleta de advertencia**: Red
- **Tema**: Light theme con configuración de tipografía y densidad

### TailwindCSS
- Configurado para procesar archivos HTML y TypeScript
- Variables CSS personalizadas para esquema de colores
- Estilos utilitarios para layout y componentes

### Variables de Color Personalizadas
```css
:root {
  --color-primary-50: #eef2ff;
  --color-primary-500: #4f46e5;
  --color-success-50: #dcfce7;
  --color-success-500: #10b981;
  /* ... más variables */
}
```

## 🚀 Configuración y Scripts

### Scripts Disponibles
```bash
npm run ng          # Angular CLI
npm run start       # Servidor de desarrollo (ng serve)
npm run build       # Build de producción
npm run watch       # Build en modo watch
npm run test        # Ejecutar tests
```

### Configuración de Build
- **Output path**: `dist/zekret-front`
- **Presupuestos de tamaño**:
  - Inicial: 500KB (warning), 1MB (error)
  - Estilos de componente: 2KB (warning), 4KB (error)
- **Hashing**: Habilitado para producción

## 🛣️ Sistema de Rutas

### Rutas Principales
```typescript
Routes = [
  { path: 'login', component: LoginComponent },
  { path: '', redirectTo: '/login', pathMatch: 'full' },
  { path: ':username', component: LayoutComponent, children: pagesRoutes }
]
```

### Rutas Anidadas (pagesRoutes)
- Ruta principal: `NamespaceComponent`
- Estructura preparada para rutas adicionales

## 🔧 Configuración Técnica

### TypeScript
- **Target**: ES2022
- **Configuraciones**:
  - `tsconfig.app.json`: Configuración para la aplicación
  - `tsconfig.spec.json`: Configuración para tests
  - `tsconfig.json`: Configuración base

### Angular Configuration
- **Prefix**: `app`
- **Source root**: `src`
- **Standalone components**: Sí (arquitectura moderna)
- **Strict mode**: Habilitado

## 📦 Modelos de Datos

### User
```typescript
export class User {
    email: string;
    username: string;
    password: string;
    enabled: boolean;
}
```

### Credential
```typescript
export class Credential {
    title: string;
    zrn: string;           // Zekret Resource Name
    username: string;
    password: string;
    sshPrivateKey: string;
    secretText: string;
    fileContent: string;
    createdAt: Date;
    updatedAt: Date;
    credentialType: CredentialTypeDTO;
    namespace: Namespace;
}
```

### Namespace
```typescript
export class Namespace {
    name: string;
    zrn: string;           // Zekret Resource Name
    description: string;
    createdAt: Date;
    updatedAt: Date;
}
```

### DTOs de API
```typescript
export interface APIResponseDTO<T> {
    statusCode: number;
    message: string;
    success: boolean;
    timestamp: string;
    data: T;
}

export interface AuthenticationResponseDTO {
    access_token: string;
    refresh_token: string;
    message: number;
}
```

## 🎪 Estado del Desarrollo

### Componentes Implementados
- ✅ Sistema de autenticación completo con JWT
- ✅ Login y registro de usuarios
- ✅ Layout principal con header
- ✅ Gestión básica de namespaces
- ✅ Listado y selección de credenciales
- ✅ Diálogos para edición de credenciales
- ✅ Servicios para integración con backend
- ✅ Modelos de datos completos
- ✅ Utilidades para manejo de JWT
- ✅ Configuración de interceptors HTTP

### Funcionalidades Pendientes
- 🔲 Implementación completa de diálogos modales
- 🔲 Validaciones avanzadas de formularios
- 🔲 Manejo de errores HTTP más robusto
- 🔲 Implementación de refresh token
- 🔲 Guards de rutas para protección
- 🔲 Tests unitarios e integración
- 🔲 Internacionalización (i18n)
- 🔲 Optimización de rendimiento
- 🔲 Documentación de componentes

### API Integration Status
- ✅ AuthService integrado con backend
- ✅ UserService con operaciones CRUD
- ✅ GenericService como base reutilizable
- ✅ Configuración de environment para diferentes entornos
- 🔲 NamespaceService implementation
- 🔲 CredentialService implementation
- 🔲 Error handling and retry mechanisms

## 🚀 Cómo Empezar

1. **Prerequisitos**:
   ```bash
   Node.js 18+ y npm
   Angular CLI 17+
   ```

2. **Instalar dependencias**:
   ```bash
   npm install
   ```

3. **Configurar environment**:
   - Verificar `src/environments/environment.development.ts`
   - Ajustar `apiUrl` según configuración del backend

4. **Ejecutar en desarrollo**:
   ```bash
   npm start
   # La aplicación estará disponible en http://localhost:4200
   ```

5. **Build para producción**:
   ```bash
   npm run build
   # Output en dist/zekret-front/
   ```

6. **Ejecutar tests**:
   ```bash
   npm test
   ```

## 📝 Notas de Desarrollo

### Arquitectura y Buenas Prácticas
- La aplicación utiliza **standalone components**, siguiendo las mejores prácticas de Angular 17
- Los formularios están implementados con **Reactive Forms** para mejor control y validación
- Se usa **Angular Material** para componentes UI complejos y **TailwindCSS** para estilos utilitarios
- **Inyección de dependencias** con servicios siguiendo el patrón Service Layer
- **Separación de responsabilidades** clara entre componentes, servicios y modelos

### Patrones de Diseño Implementados
- **Generic Service Pattern**: Servicio base reutilizable para operaciones CRUD
- **DTO Pattern**: Separación entre modelos de dominio y transferencia de datos
- **Observer Pattern**: Uso de RxJS Observables para comunicación asíncrona
- **Environment Pattern**: Configuración por ambiente para desarrollo y producción

### Convenciones de Código
- **Naming**: PascalCase para clases, camelCase para propiedades y métodos
- **File Structure**: Agrupación por funcionalidad (_model, _service, pages, modals)
- **Component Architecture**: Standalone components con imports explícitos
- **Service Layer**: Servicios especializados que extienden funcionalidad genérica

---

*Este README refleja el estado actual del proyecto y será actualizado conforme evolucione el desarrollo.*
