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

# El esquema lo gestiona Flyway (migraciones en src/main/resources/db/migration)
# Hibernate está en modo validate (no crea ni modifica tablas)
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
- **Frontend**: `config.service.ts` para URLs dinámicas (usa `environment.ts`/`environment.prod.ts`)
- **Imágenes**: Se sirven en `/api/images` (context-path `/api`; directorio configurable con `APP_IMAGES_UPLOAD_DIR`)
- **CORS**: Origen configurable con `FRONTEND_URL`

## Seguridad

### Autenticación JWT
- Access Token: 24 horas
- Refresh Token: 7 días
- Algoritmo: HS512
- Secret: Variable de entorno `JWT_SECRET`

### Roles de Usuario
- `ROLE_ADMIN`: Acceso completo
- `ROLE_EMPLEADO`: Gestión de productos y pedidos
- `ROLE_CLIENTE`: Compras y perfil

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
- `POST /productos/add` - Crear producto (requiere rol EMPLEADO/ADMIN)
- `GET /productos/{id}` - Detalle producto
- `PUT /productos/{id}` - Actualizar producto
- `DELETE /productos/{id}` - Eliminar producto

### Categorías
- `GET /categorias` - Listar categorías (público)
- `POST /categorias` - Crear categoría (requiere rol EMPLEADO/ADMIN)

### Imágenes
- `GET /api/images/{filename}` - Servir imágenes estáticas
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

## Modelo de Datos

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

El despliegue se hace con Docker Compose: un contenedor PostgreSQL, el backend (JAR en JRE Alpine)
y el frontend (SPA Angular servida por nginx, que además hace de proxy de `/api` y `/api/images`
hacia el backend).

### 1. Configurar variables de entorno

```bash
cp .env.production.example .env
# Editar .env con los valores reales (BD, JWT_SECRET, correo, FRONTEND_URL, admin, etc.)
```

Notas:
- `JWT_SECRET` es obligatorio y debe ser largo/aleatorio (p. ej. `openssl rand -base64 64`).
- `EXPOSE_VERIFICATION_URL` debe quedar en `false` en producción.
- `SPRINGDOC_ENABLED=false` desactiva Swagger en producción.
- `APP_IMAGES_BASE_URL` es la URL pública con la que se guardan/leen las imágenes (p. ej. `https://tu-dominio.com/api/images`).
- `ADMIN_EMAIL`/`ADMIN_PASSWORD` crean un administrador inicial la primera vez (si `ADMIN_EMAIL` está vacío, no se crea nada).

### 2. Levantar el stack

```bash
docker compose up --build -d

# Ver estado y logs
docker compose ps
docker compose logs -f backend
```

La aplicación queda disponible en el puerto definido por `HTTP_PORT` (por defecto el 80).
El backend **no** publica su puerto: solo se accede a través del proxy de nginx.

### 3. Actualizar una nueva versión

```bash
git pull
docker compose up --build -d
```

### Persistencia
- `db_data`: datos de PostgreSQL.
- `uploads`: imágenes subidas (`/app/uploads` dentro del backend).

### Endpoints de monitoreo
- `GET /api/auth/health`
- `GET /api/actuator/health` (health/liveness/readiness)

### Sin Docker (alternativa)

```bash
# Backend
cd com.pretor-sport.app/com.pretor-sport.app
./mvnw.cmd clean package -DskipTests
java -jar target/com.pretor-sport.app-0.0.1-SNAPSHOT.jar

# Frontend
cd pretor-sport-frontend
npm run build -- --configuration production
# Servir dist/pretor-sport-frontend/browser con un servidor web que haga fallback a index.html
```

## Soporte

### Logs Importantes
- **Backend**: `logs/pretor-sport.log`
- **Consola**: Configurado con DEBUG level para desarrollo

---
**Versión**: 0.0.1-SNAPSHOT  
**Estado**: En desarrollo activo  
**Última Actualización**: 2026-01-26
