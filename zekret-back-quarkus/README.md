# Zekret Backend - Quarkus

A secure credential management system built with Quarkus, providing RESTful APIs for managing namespaces, credentials, and user authentication.

## 🚀 Overview

Zekret Backend is a modern Java application built on the Quarkus framework, designed to provide secure storage and management of credentials and secrets. The system implements JWT-based authentication, role-based access control, and follows RESTful API best practices.

## 📋 Table of Contents

- [Features](#-features)
- [Technology Stack](#-technology-stack)
- [Prerequisites](#-prerequisites)
- [Getting Started](#-getting-started)
- [Configuration](#-configuration)
- [API Endpoints](#-api-endpoints)
- [Architecture](#-architecture)
- [Testing](#-testing)
- [Documentation](#-documentation)
- [Deployment](#-deployment)
- [Contributing](#-contributing)

## ✨ Features

- 🔐 **JWT-based Authentication**: Secure token-based authentication system
- 👤 **User Management**: Complete user registration and authentication
- 📦 **Namespace Management**: Organize credentials in hierarchical namespaces
- 🔑 **Credential Storage**: Secure storage and retrieval of sensitive credentials
- 🎯 **ZRN (Zekret Resource Name)**: Unique resource identifier system
- 🛡️ **Role-based Access Control**: Fine-grained permission management
- 📝 **Comprehensive Error Handling**: Centralized exception management with standardized responses
- ✅ **Input Validation**: Bean Validation (Jakarta Validation) for all inputs
- 📊 **Logging**: Structured logging with file rotation
- 🐳 **Docker Support**: Ready for containerized deployment

## 🛠️ Technology Stack

- **Framework**: Quarkus 3.27.0
- **Language**: Java 21
- **Build Tool**: Maven
- **Database**: MySQL 8.x
- **ORM**: Hibernate ORM with Panache
- **Security**: SmallRye JWT, BCrypt
- **Testing**: JUnit 5, REST Assured, Mockito, AssertJ
- **API**: JAX-RS (RESTEasy Reactive)

### Key Dependencies

- `quarkus-rest`: RESTful web services
- `quarkus-rest-jackson`: JSON serialization
- `quarkus-hibernate-orm-panache`: Simplified ORM with Panache pattern
- `quarkus-jdbc-mysql`: MySQL database driver
- `quarkus-smallrye-jwt`: JWT authentication and authorization
- `quarkus-hibernate-validator`: Bean validation
- `jbcrypt`: Password hashing

## 📦 Prerequisites

- **Java 21** or higher
- **Maven 3.9+**
- **MySQL 8.x**
- **Docker** (optional, for containerized deployment)

## 🚀 Getting Started

### 1. Clone the Repository

```bash
git clone https://github.com/jcabrera9409/zekret.git
cd zekret/zekret-back-quarkus
```

### 2. Database Setup

Start the MySQL database using Docker Compose:

```bash
cd ../dev
docker-compose up -d
```

The database will be initialized with the schema defined in `dev/init/00-Scripts.nogit.sql`.

### 3. Generate JWT Keys

Generate RSA key pair for JWT signing:

```bash
# Generate private key
openssl genpkey -algorithm RSA -out privateKey.pem -pkeyopt rsa_keygen_bits:2048

# Extract public key
openssl rsa -pubout -in privateKey.pem -out publicKey.pem
```

Store the keys securely outside the project directory.

### 4. Configure Environment Variables

Create a `.env` file in the project root or set the following environment variables:

```bash
# Database Configuration
export DATASOURCE_URL=jdbc:mysql://localhost:3306/zekretdb
export MYSQL_USER=root
export MYSQL_PASSWORD=root

# JWT Configuration (paths to your generated keys)
export JWT_PUBLIC_KEY_PATH=/path/to/publicKey.pem
export JWT_PRIVATE_KEY_PATH=/path/to/privateKey.pem

# Application Path (for logs)
export APP_PATH=/path/to/zekret-back-quarkus
```

**Environment Variables Reference:**

| Variable | Description | Required | Default | Example |
|----------|-------------|----------|---------|---------|
| `DATASOURCE_URL` | MySQL JDBC connection URL | No | `jdbc:mysql://localhost:3306/zekretdb` | `jdbc:mysql://db-server:3306/zekretdb` |
| `MYSQL_USER` | Database username | No | `root` | `zekret_user` |
| `MYSQL_PASSWORD` | Database password | No | `root` | `secure_password` |
| `JWT_PUBLIC_KEY_PATH` | Path to RSA public key for JWT verification | Yes | - | `/keys/publicKey.pem` |
| `JWT_PRIVATE_KEY_PATH` | Path to RSA private key for JWT signing | Yes | - | `/keys/privateKey.pem` |
| `APP_PATH` | Application base path for log files | No | `.` (current dir) | `/opt/zekret` |

### 5. Run in Development Mode

```bash
mvn quarkus:dev
```

The application will start on `http://localhost:8080`.

## ⚙️ Configuration

The application configuration is located in `src/main/resources/application.properties`. It supports multiple profiles:

- **dev**: Development mode with debug logging and CORS enabled
- **test**: Testing mode with minimal logging
- **prod**: Production mode with optimized settings

Key configuration sections:

### HTTP Server
```properties
quarkus.http.port=8080
```

### Database
```properties
quarkus.datasource.jdbc.url=${DATASOURCE_URL}
quarkus.datasource.username=${MYSQL_USER}
quarkus.datasource.password=${MYSQL_PASSWORD}
```

### JWT Security
```properties
mp.jwt.verify.issuer=https://zekret.com
jwt.expiration.time=3600
```

### CORS (Development)
```properties
%dev.quarkus.http.cors.origins=http://localhost:4200
```

### Health Checks
```properties
quarkus.smallrye-health.root-path=/v1/health
quarkus.http.auth.permission.health.policy=permit
```

Health check endpoints are publicly accessible (no authentication required):
- `/v1/health` - Overall health status
- `/v1/health/live` - Liveness probe
- `/v1/health/ready` - Readiness probe
- `/v1/health/started` - Startup probe

See `application.properties` for complete configuration options.

## 🌐 API Endpoints

### Authentication (`/v1/auth`)

- `POST /v1/auth/login` - User login
- `POST /v1/auth/logout` - User logout

### Credentials (`/v1/credentials`)

- `GET /v1/credentials` - List all credentials
- `POST /v1/credentials` - Create new credential
- `GET /v1/credentials/{zrn}` - Get credential by ZRN
- `PUT /v1/credentials/{zrn}` - Update credential
- `DELETE /v1/credentials/{zrn}` - Delete credential

### Health (`/v1/health`) 🔓 Public

- `GET /v1/health` - Overall application health status
- `GET /v1/health/live` - Liveness probe (is the app running?)
- `GET /v1/health/ready` - Readiness probe (is the app ready to serve traffic?)
- `GET /v1/health/started` - Startup probe (has the app started successfully?)

*Note: Health endpoints are publicly accessible and do not require authentication.*

For detailed API documentation and request/response examples, refer to the Bruno collection in `../zekret-collection/`.
- `PUT /v1/namespaces/{zrn}` - Update namespace
- `DELETE /v1/namespaces/{zrn}` - Delete namespace

### Credentials (`/v1/credentials`)

- `GET /v1/credentials` - List all credentials
- `POST /v1/credentials` - Create new credential
- `GET /v1/credentials/{zrn}` - Get credential by ZRN
- `PUT /v1/credentials/{zrn}` - Update credential
- `DELETE /v1/credentials/{zrn}` - Delete credential

For detailed API documentation and request/response examples, refer to the Bruno collection in `../zekret-collection/`.

## 🏗️ Architecture

### Project Structure

```
src/main/java/com/zekret/
├── configuration/      # Application configuration classes
├── controller/         # REST API endpoints
├── dto/               # Data Transfer Objects
├── exception/         # Custom exceptions
├── mapper/            # Entity-DTO mappers
├── model/             # JPA entities
├── repository/        # Data access layer (Panache repositories)
├── service/           # Business logic layer
└── util/              # Utility classes
```

### Key Components

- **Controllers**: Handle HTTP requests and responses
- **Services**: Contain business logic and orchestration
- **Repositories**: Data access using Panache pattern
- **DTOs**: Request/response data structures
- **Mappers**: Convert between entities and DTOs
- **Exceptions**: Custom exception hierarchy for error handling

### Domain Model

- **User**: System users with authentication credentials
- **Token**: JWT access and refresh tokens
- **Namespace**: Logical grouping for credentials
- **Credential**: Stored secrets and credentials

## 🧪 Testing

### Run All Tests

```bash
mvn test
```

### Run Specific Test Class

```bash
mvn test -Dtest=CredentialServiceTest
```

### Test Coverage

The project includes comprehensive unit tests for:
- Service layer logic
- Repository operations
- Controller endpoints
- Exception handling
- JWT token generation and validation

Testing frameworks used:
- **JUnit 5**: Test framework
- **Mockito**: Mocking framework
- **REST Assured**: REST API testing
- **AssertJ**: Fluent assertions

## 📚 Documentation

For detailed information about specific features and updates, please refer to:

- **[Error Handling System](ERROR_HANDLING.md)**: Complete guide to the centralized error handling system, including custom exceptions, standardized responses, and implementation examples.

- **[Token Schema Update](TOKEN_SCHEMA_UPDATE.md)**: Documentation about the token database schema changes, including the migration from VARCHAR to TEXT columns and the handling of unique constraints.

## 🐳 Deployment

### Running with JVM

```bash
# Build the application
mvn clean package

# Run the JAR
java -jar target/quarkus-app/quarkus-run.jar
```

### Building Native Image

```bash
# Build native executable
mvn package -Pnative

# Run native executable
./target/zekret-back-quarkus-1.0.0-SNAPSHOT-runner
```

### Docker Deployment

```bash
# Build native container image
mvn package -Pnative -Dquarkus.native.container-build=true

# Run with Docker
docker run -i --rm -p 8080:8080 \
  -e DATASOURCE_URL=jdbc:mysql://host.docker.internal:3306/zekretdb \
  -e MYSQL_USER=root \
  -e MYSQL_PASSWORD=root \
  -e JWT_PUBLIC_KEY_PATH=/keys/publicKey.pem \
  -e JWT_PRIVATE_KEY_PATH=/keys/privateKey.pem \
  zekret-back-quarkus:1.0.0-SNAPSHOT
```

## 🔒 Security Considerations

- **Password Storage**: Passwords are hashed using BCrypt before storage
- **JWT Tokens**: Access tokens expire after 1 hour (configurable)
- **Token Validation**: All protected endpoints validate JWT tokens
- **Input Validation**: All inputs are validated using Bean Validation
- **SQL Injection**: Protected through Panache parameterized queries
- **CORS**: Configured appropriately for each environment

## 🤝 Contributing

1. Fork the repository
2. Create your feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add some amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

## 📝 License

This project is licensed under the terms specified in the [LICENSE](../LICENSE) file.

## 👥 Authors

- **jcabrera9409** - *Initial work* - [GitHub](https://github.com/jcabrera9409)

## 🙏 Acknowledgments

- Built with [Quarkus](https://quarkus.io/) - Supersonic Subatomic Java Framework
- Inspired by modern secret management systems
- Community contributions and feedback

---

For more information, visit the [Zekret project repository](https://github.com/jcabrera9409/zekret).
