# 🔐 GUÍA DE SEGURIDAD PARA DESARROLLO

## 🚨 **IMPORTANTE: LEER ANTES DE COMPROMETER**

Este documento explica cómo manejar credenciales de forma segura en desarrollo y producción.

## 📋 **Problemática Resuelta**

### ✅ **Antes (INSEGURO)**
- `application.yml` con credenciales visibles
- Secrets hardcoded en código Java
- Archivos sensibles subidos a GitHub

### ✅ **Ahora (SEGURO)**
- Variables de entorno en `.env.properties`
- `.gitignore` actualizado
- Fallbacks seguros en application.yml

## 🔧 **Configuración Segura Actual**

### **Archivos de Configuración**

#### **1. .env.properties** 👈 **SECRETO**
```properties
# DESARROLLO - valores de ejemplo
DATABASE_URL=jdbc:postgresql://localhost:5432/pretor_sport_db
DATABASE_USERNAME=postgres
DATABASE_PASSWORD=cambiar_esta_password_en_produccion

JWT_SECRET=cambiar_est JWT_secret_por_uno_muy_largo_y_secreto_para_produccion_minimo_512_caracteres
```

#### **2. application.yml** 👁 **PÚBLICO PERO SEGURO**
```yaml
spring:
  datasource:
    url: ${DATABASE_URL:jdbc:postgresql://localhost:5432/pretor_sport_db}
    username: ${DATABASE_USERNAME:postgres}
    password: ${DATABASE_PASSWORD}
  # Solo fallbacks si no hay variables de entorno
```

#### **3. .gitignore** 🛡️ **PROTECCIÓN**
```
# Variables de entorno (NUNCA subirlas)
.env
.env.properties
.env.local
.env.production

# Configuración sensible (excepto desarrollo)
application*.yml
application-production.yml
```

## 🚀 **Flujo de Trabajo Seguro**

### **Para Desarrollo Local**
1. ✅ Usar `.env.properties` con credenciales locales
2. ✅ Nunca commitear `.env.properties`
3. ✅ Usar credenciales diferentes a producción

### **Para Producción**
1. ✅ Configurar variables de entorno en servidor
2. ✅ **NO** incluir `.env.properties` en despliegue
3. ✅ Usar secrets del cloud provider (AWS, Azure, etc.)

## 🔑 **Requisitos de Seguridad para Variables de Entorno**

### **JWT_SECRET**
- ✅ **MÍNIMO**: 512 caracteres
- ✅ **CONTENIDO**: Aleatorio, sin palabras del diccionario
- ✅ **EJEMPLO**: `GeneratedSecureKey_` + 48 caracteres aleatorios

### **DATABASE_PASSWORD**
- ✅ **REQUISITO**: Mínimo 12 caracteres
- ✅ **CONTENIDO**: Letras, números, símbolos
- ✅ **NO USAR**: "123456", "password", "admin123"

## 🛡️ **Validación de Configuración**

### **Comprobar antes de iniciar:**
```bash
# 1. Verificar que .env.properties NO esté en Git
git status | grep ".env" # No debe mostrar nada

# 2. Verificar variables cargadas
./mvnw.cmd spring-boot:run | grep "DATABASE_URL" # Debe mostrar el valor

# 3. Verificar secreto JWT
grep "JWT_SECRET" .env.properties # Debe existir y ser largo
```

## 🚨 **Qué NO HACER**

### **❌ MALAS PRÁCTICAS**
- ❌ Commitear `.env.properties`
- ❌ Usar contraseñas como "123456"
- ❌ Exponer secrets en código
- ❌ Subir `application-production.yml`
- ❌ Usar los mismos credentials en dev/prod

### **✅ BUENAS PRÁCTICAS**
- ✅ Variables de entorno para todo
- ✅ Diferentes credenciales por ambiente
- ✅ Validar configuración antes de iniciar
- ✅ Rotar secrets periódicamente
- ✅ Usar secret managers en producción

## 🔄 **Para Cambiar Credenciales Actuales**

### **1. Base de Datos**
```properties
DATABASE_PASSWORD=tu_nuevo_password_seguro_123
```

### **2. JWT Secret**
```bash
# Generar nuevo secret (512+ caracteres)
openssl rand -base64 64

# O usar generador online
# https://randomkeygen.com/
```

## 🌐 **Configuración por Ambiente**

### **Desarrollo**
```bash
export DATABASE_URL=jdbc://postgresql://localhost:5432/pretor_sport_dev
export DATABASE_USERNAME=dev_user
export DATABASE_PASSWORD=dev_secure_password
export JWT_SECRET=dev_jwt_secret_very_long_and_secure_random_key
```

### **Producción**
```bash
# Usar variables del cloud provider
# AWS Secrets Manager, Azure Key Vault, etc.
# NUNCA exportar como variables de shell en scripts
```

## ✅ **Verificación Final**

Después de estos cambios, el proyecto está:
- ✅ **Seguro**: No hay secrets visibles
- ✅ **Flexible**: Funciona en múltiples ambientes  
- ✅ **Maintenible**: Fácil cambio de credenciales
- ✅ **Git-friendly**: `.gitignore` protege archivos sensibles

---
**Última actualización**: 2026-01-26  
**Estado**: Seguridad implementada y verificada