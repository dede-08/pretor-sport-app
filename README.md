# Pretor Sport App

Plataforma de e-commerce para artículos deportivos con Spring Boot 3.5.4 + Angular 19.2.18 + PostgreSQL

## Información del Proyecto

- **Backend**: Spring Boot 3.5.4 + Java 17
- **Frontend**: Angular 19.2.18 + TypeScript 5.5.0
- **Base de Datos**: PostgreSQL 17
- **Autenticación**: JWT (JSON Web Tokens)
- **Arquitectura**: REST API + SPA

## Estructura

```
pretor-sport-app/
├── com.pretor-sport.app/          # Backend Spring Boot
│   └── src/main/java/
│       └── com/pretor_sport/app/
│           ├── controller/          # REST Controllers
│           ├── service/            # Lógica de negocio
│           ├── model/              # Entidades JPA
│           ├── repository/         # Spring Data JPA
│           ├── dto/               # Data Transfer Objects
│           ├── security/          # JWT + Security
│           └── config/            # Configuración
├── pretor-sport-frontend/          # Frontend Angular
│   └── src/
│       ├── app/                # Componentes y páginas
│       ├── services/           # Servicios HTTP
│       ├── models/             # Interfaces TypeScript
│       └── assets/             # Recursos estáticos
└── README.md                   # Este archivo
```

## Instalación y Ejecución

### Prerrequisitos
- Java 17+
- Node.js 18+
- PostgreSQL 15+
- Maven 3.8+

### Backend (Spring Boot)

```bash
# Navegar al directorio del backend
cd com.pretor-sport.app/com.pretor-sport.app

# Compilar
./mvnw.cmd clean compile

# Ejecutar en desarrollo
./mvnw.cmd spring-boot:run

# Construir JAR para producción
./mvnw.cmd clean package -DskipTests

# Ejecutar JAR construido
java -jar target/com.pretor-sport.app-0.0.1-SNAPSHOT.jar
```

### Frontend (Angular)

```bash
# Navegar al directorio del frontend
cd pretor-sport-frontend

# Instalar dependencias
npm install

# Ejecutar en desarrollo
npm start

# Construir para producción
npm run build

# Ejecutar tests
npm test
```

### Base de Datos

```bash
# Crear base de datos PostgreSQL
CREATE DATABASE pretor_sport_db;

# El esquema se crea automáticamente con Hibernate DDL
# Ver archivo: pretor_sport_db.sql para datos iniciales
```

## Configuración

### Variables de Entorno (.env.properties)

```properties
# Base de Datos
DATABASE_URL=jdbc:postgresql://localhost:5432/pretor_sport_db
DATABASE_USERNAME=postgres
DATABASE_PASSWORD=tu_password_seguro

# JWT (IMPORTANTE: Usar secret seguro en producción)
JWT_SECRET=tu_jwt_secret_muy_largo_y_secreto
JWT_EXPIRATION=86400
JWT_REFRESH_EXPIRATION=604800

# Aplicación
SERVER_PORT=8080
FRONTEND_URL=http://localhost:4200
```

### Configuración Clave

- **Backend**: `application.yml` + variables de entorno
- **Frontend**: `config.service.ts` para URLs dinámicas
- **Imágenes**: Sirve desde `/uploads/images/`
- **CORS**: Configurado para `http://localhost:4200`

## Seguridad

### Autenticación JWT
- Access Token: 24 horas
- Refresh Token: 7 días
- Algoritmo: HS512
- Secret: Variable de entorno `JWT_SECRET`

### Roles de Usuario
- `ROLE_ADMIN`: Acceso completo
- `ROLE_EMPLEADO`: Gestión de productos y pedidos
- `ROLE_CLIENTE': Compras y perfil

### CORS Configurado
- Orígenes permitidos: Frontend URL
- Métodos: GET, POST, PUT, DELETE, PATCH, OPTIONS
- Headers: Authorization, Content-Type, etc.

## Endpoints Principales

### Autenticación
- `POST /auth/login` - Login de usuarios
- `POST /auth/register` - Registro de usuarios
- `POST /auth/refresh` - Refresh token
- `GET /auth/health` - Health check

### Productos
- `GET /productos` - Listar productos (público)
- `POST /productos` - Crear producto (requiere rol EMPLEADO/ADMIN)
- `GET /productos/{id}` - Detalle producto
- `PUT /productos/{id}` - Actualizar producto
- `DELETE /productos/{id}` - Eliminar producto

### Categorías
- `GET /categorias` - Listar categorías (público)
- `POST /categorias` - Crear categoría (requiere rol EMPLEADO/ADMIN)

### Imágenes
- `GET /images/{filename}` - Servir imágenes estáticas
- Upload: `POST /productos/{id}/image` - Subir imagen de producto

## Tests

### Backend Tests
```bash
cd com.pretor-sport.app/com.pretor-sport.app
./mvnw.cmd test
```

### Frontend Tests
```bash
cd pretor-sport-frontend
npm test
```

## 📊 Modelo de Datos

### Entidades Principales
- **Producto**: Artículos deportivos con especificaciones técnicas
- **Categoría**: Clasificación de productos
- **Usuario**: Clientes, empleados y administradores
- **Pedido**: Órdenes de compra
- **Venta**: Transacciones de venta

### Campos Clave (Producto)
- `nombre`, `descripcion`, `precio` (BigDecimal)
- `stock`, `marca`, `modelo`, `talla`, `color`
- `genero`: HOMBRE, MUJER, NIÑO, NIÑA, UNISEX
- `material`, `peso`, `características` (List<String>)


## Despliegue

### Variables de Entorno Producción
```bash
export DATABASE_URL=jdbc://postgresql://db-host:5432/pretor_sport_prod
export DATABASE_USERNAME=pretor_user
export DATABASE_PASSWORD=secure_password
export JWT_SECRET=production_jwt_secret_very_long_and_secure
export SERVER_PORT=8080
export FRONTEND_URL=https://tu-dominio.com
```

### Docker
```bash
# Construir backend
./mvnw.cmd clean package -DskipTests

# Construir frontend
cd ../pretor-sport-frontend
npm run build
```

---
**Versión**: 0.0.1-SNAPSHOT  
**Estado**: En desarrollo activo  
**Última Actualización**: 2026-01-26
