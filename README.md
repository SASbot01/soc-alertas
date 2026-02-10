# BlackWolf SOC - Plataforma de Monitorizacion y Operaciones de Seguridad

Sistema completo de monitorizacion de ciberseguridad para empresas. Incluye deteccion de amenazas en tiempo real, gestion de auditorias, pentesting, remediacion de vulnerabilidades, certificaciones de seguridad y metricas SOC.

---

## Indice

1. [Arquitectura General](#arquitectura-general)
2. [Stack Tecnologico](#stack-tecnologico)
3. [Estructura del Proyecto](#estructura-del-proyecto)
4. [Como Funciona: Flujo Completo](#como-funciona-flujo-completo)
5. [Instalacion y Arranque Local](#instalacion-y-arranque-local)
6. [Despliegue con Docker](#despliegue-con-docker)
7. [API Reference](#api-reference)
8. [Base de Datos](#base-de-datos)
9. [Seguridad y Autenticacion](#seguridad-y-autenticacion)

---

## Arquitectura General

```
                                    +------------------+
                                    |   Navegador Web  |
                                    |  (React + Vite)  |
                                    +--------+---------+
                                             |
                                             | HTTP/HTTPS
                                             v
+------------------+           +----------------------------+
|  Sensor de Red   | -------> |     Spring Boot Backend     |
|  (en cliente)    |  POST    |         (Puerto 8080)       |
|                  | /upload  |                              |
+------------------+          |  - REST API                  |
                              |  - JWT Auth (HS512)          |
                              |  - Flyway Migrations         |
                              |  - JPA/Hibernate             |
                              +------+----------+------------+
                                     |          |
                              +------v--+  +----v-----+
                              | MySQL   |  |  MinIO   |
                              | 8.0     |  | (S3-like)|
                              | :3306   |  | :9000    |
                              +---------+  +----------+
```

**Modelo multi-tenant**: Cada empresa (Company) tiene su propio espacio aislado. Todos los datos (amenazas, sensores, auditorias, vulnerabilidades) estan ligados a un `company_id`. Un usuario solo puede ver datos de su empresa.

---

## Stack Tecnologico

### Backend
| Tecnologia | Version | Uso |
|---|---|---|
| Java | 21 | Lenguaje principal |
| Spring Boot | 3.2.3 | Framework web + DI |
| Spring Security | 6.x | Autenticacion/Autorizacion |
| Spring Data JPA | 3.2.x | Acceso a datos |
| Hibernate | 6.4.4 | ORM |
| Flyway | 9.22.3 | Migraciones de base de datos |
| MySQL | 8.0 | Base de datos relacional |
| JJWT | 0.11.5 | Tokens JWT (HS512) |
| MinIO | 8.5.7 | Almacenamiento de objetos |
| Lombok | - | Reduccion de boilerplate |

### Frontend
| Tecnologia | Version | Uso |
|---|---|---|
| React | 19.2.0 | UI framework |
| TypeScript | 5.9.3 | Tipado estatico |
| Vite | 7.2.4 | Build tool + dev server |
| Tailwind CSS | 4.1.18 | Estilos utility-first |
| React Router | 7.11.0 | Navegacion SPA |
| Axios | 1.13.2 | Cliente HTTP |
| Recharts | 3.6.0 | Graficos y charts |
| Framer Motion | 12.23.26 | Animaciones |
| Lucide React | 0.562.0 | Iconos |
| React Hook Form | 7.69.0 | Formularios |

---

## Estructura del Proyecto

```
balckwolf-monitorizacion-main/
|
|-- backend/
|   |-- src/main/java/com/blackwolf/backend/
|   |   |-- config/           # SecurityConfig, WebConfig, GlobalExceptionHandler
|   |   |-- controller/       # REST Controllers (8)
|   |   |-- dto/              # Data Transfer Objects (8)
|   |   |-- model/            # Entidades JPA (14)
|   |   |-- repository/       # Repositorios Spring Data (13)
|   |   |-- security/         # JWT Filter, Token Provider, UserDetailsService
|   |   |-- service/          # Logica de negocio (10)
|   |   |-- specification/    # JPA Specifications (filtrado dinamico)
|   |   |-- util/             # AuthUtils
|   |   +-- BlackWolfApplication.java
|   |-- src/main/resources/
|   |   |-- application.yml
|   |   +-- db/migration/
|   |       |-- V1__init.sql              # Tablas base
|   |       +-- V2__soc_nucleus_tables.sql # Tablas SOC
|   +-- pom.xml
|
|-- frontend/
|   |-- src/
|   |   |-- components/       # Componentes reutilizables (10)
|   |   |-- context/          # AuthContext (estado global auth)
|   |   |-- lib/              # api.ts (Axios), services.ts (capa API)
|   |   |-- pages/            # Paginas (12)
|   |   |-- types/            # Interfaces TypeScript
|   |   |-- App.tsx           # Routing principal
|   |   +-- main.tsx          # Entry point
|   +-- package.json
|
+-- deploy/
    |-- docker-compose.yml
    |-- Dockerfile.backend
    |-- Dockerfile.frontend
    +-- nginx.conf
```

---

## Como Funciona: Flujo Completo

Este es el proceso paso a paso para monitorizar a un nuevo cliente con BlackWolf.

### Paso 1: Registro de la Empresa

El administrador de BlackWolf registra la empresa cliente via API:

```bash
POST /api/v1/auth/register
{
  "companyName": "Acme Corp",
  "domain": "acme.com",
  "contactEmail": "security@acme.com",
  "contactPhone": "+34600000000",
  "plan": "enterprise"
}
```

**Que ocurre internamente:**
1. Se crea un registro en la tabla `companies` con un UUID como ID
2. Se genera automaticamente un **API Key** unico (UUID) - este se usara para autenticar los sensores
3. Se crea un usuario administrador con el email de contacto
4. La contraseña por defecto es `admin` (hasheada con BCrypt) - debe cambiarse en el primer acceso
5. La respuesta incluye el `apiKey` que se debe configurar en los sensores del cliente

### Paso 2: Login en el Panel

El equipo SOC accede al panel web:

```
URL: http://localhost:5174
Domain: acme.com
Email: security@acme.com
Password: admin
```

**Que ocurre internamente:**
1. `AuthService.login()` busca la empresa por dominio
2. Busca el usuario por email + companyId
3. Spring Security valida la contraseña con BCrypt
4. `JwtTokenProvider` genera un token JWT firmado con HS512 (expira en 24h)
5. El frontend guarda el token en `localStorage`
6. Todas las peticiones posteriores incluyen `Authorization: Bearer <token>`
7. `JwtAuthenticationFilter` intercepta cada request, valida el token y carga el `UserDetails`

### Paso 3: Despliegue de Sensores en el Cliente

Se instala un sensor (agente) en la red del cliente. El sensor envia datos periodicamente:

```bash
POST /api/v1/sensors/upload
{
  "company_id": "uuid-de-la-empresa",
  "sensor_id": "sensor-hq-01",
  "api_key": "api-key-de-la-empresa",
  "packets": [
    {"src": "192.168.1.5", "dst": "10.0.0.1", "port": 443, "protocol": "TCP"}
  ],
  "threats": [
    {
      "threat_type": "DDoS Attack",
      "severity": 7,
      "src_ip": "45.33.22.11",
      "dst_ip": "10.0.0.5",
      "dst_port": 80,
      "description": "SYN flood detected"
    }
  ]
}
```

**Que ocurre internamente (SensorService.processUpload):**

1. **Validacion**: Se verifica que el `api_key` exista en la BD y que el `company_id` coincida con la empresa duena de esa key
2. **Actualizacion del sensor**: Se actualiza el estado a "online", se registra `lastSeen`, se incrementan contadores de paquetes y amenazas
3. **Procesamiento de amenazas**: Por cada amenaza reportada:
   - Se crea un `ThreatEvent` en la BD con estado "detected"
   - Se asigna un UUID como ID
   - Se registra timestamp, IPs, puerto, tipo y severidad
4. **Auto-bloqueo**: Si la severidad es **>= 5**, se bloquea automaticamente la IP origen:
   - Se crea un registro en `blocked_ips` con expiracion de 24 horas
   - Razon: "Auto-block: [tipo de amenaza]"
5. **Respuesta al sensor**: El sensor recibe una lista de comandos `block_ip` con todas las IPs bloqueadas de su empresa, para que las aplique localmente en el firewall

```json
{
  "status": "success",
  "processed_packets": 1,
  "processed_threats": 1,
  "commands": [
    {"type": "block_ip", "ip": "45.33.22.11", "reason": "Auto-block: DDoS Attack"}
  ]
}
```

### Paso 4: Monitorizacion en Tiempo Real (Dashboard)

El panel web muestra:

- **Total Threats**: Numero total de amenazas detectadas para esta empresa
- **Threats Today**: Amenazas detectadas desde las 00:00 del dia actual (query real a BD)
- **Active Sensors**: Sensores con estado "online"
- **Blocked IPs**: IPs actualmente bloqueadas
- **Distribucion de ataques**: Grafico por tipo de amenaza
- **Amenazas recientes**: Ultimas 10 amenazas ordenadas por timestamp

El dashboard hace polling cada 30 segundos (`GET /api/v1/dashboard/overview`).

### Paso 5: Gestion de Amenazas

La pagina de Threats permite:

- **Filtrado dinamico**: Por tipo, estado, severidad minima, busqueda por IP
- **Paginacion server-side**: Usa JPA Specifications para construir queries dinamicas
- **Cambio de estado**: detected -> blocked -> resolved

**Como funciona el filtrado (ThreatSpecifications):**
Cada filtro activo se compone en un `Specification<ThreatEvent>` con operadores AND. Spring Data los traduce a SQL dinamico. Ejemplo:

```
companyId = 'xxx' AND severity >= 5 AND status = 'detected' AND srcIp LIKE '%192%'
```

### Paso 6: Auditorias de Seguridad

El equipo SOC puede ejecutar auditorias con un lifecycle completo:

```
SCOPING -> SCANNING -> TESTING -> REPORTING -> DELIVERED
```

**Flujo:**
1. **Crear auditoria**: Se define titulo, tipo (INFRASTRUCTURE, APPLICATION, NETWORK, COMPLIANCE, CLOUD), scope, metodologia, auditor lider
2. **Avanzar estados**: Cada transicion se valida (solo se puede avanzar al siguiente estado en orden)
3. **Agregar findings**: Durante la auditoria se registran hallazgos con:
   - Nivel de riesgo (CRITICAL, HIGH, MEDIUM, LOW, INFO)
   - CVSS Score (0.0 - 10.0)
   - Asset afectado
   - Recomendacion
4. **Resolver findings**: Cada finding se marca como RESOLVED cuando se aplica la remediacion
5. **Activity log**: Cada accion se registra automaticamente (quien hizo que, cuando)

### Paso 7: Penetration Testing

Similar a auditorias pero con fases de pentest:

```
PLANNING -> RECONNAISSANCE -> EXPLOITATION -> POST_EXPLOITATION -> REPORTING -> DELIVERED
```

- Tipos: BLACK_BOX, GREY_BOX, WHITE_BOX
- Cada finding incluye Proof of Concept (PoC) y evidencia
- Se definen Rules of Engagement antes de empezar

### Paso 8: Gestion de Vulnerabilidades

Lifecycle de vulnerabilidades:

```
DETECTED -> CONFIRMED -> IN_REMEDIATION -> FIXED -> VERIFIED
```

- Se pueden registrar con CVE ID
- Se asigna un plan de remediacion y deadline
- El dashboard muestra distribucion por estado y riesgo
- Al llegar a VERIFIED se registra la fecha de verificacion

### Paso 9: Certificaciones de Seguridad

Una vez completada una auditoria:

1. **Check eligibility**: La auditoria debe estar en estado DELIVERED y todos sus findings resueltos (0 findings OPEN)
2. **Emitir certificacion**: Se emite con validez de 12 meses
3. Tipos soportados: ISO 27001, SOC 2, PCI DSS, GDPR, HIPAA, NIST
4. Se puede **revocar** una certificacion activa si es necesario

### Paso 10: Metricas SOC

El dashboard de metricas agrega KPIs:

- Total y completados de auditorias y pentests
- Vulnerabilidades abiertas vs fijas
- Certificaciones activas (no expiradas)
- Tiempo medio de remediacion (dias entre deteccion y fix)
- Graficos de completitud y distribucion de vulnerabilidades

---

## Instalacion y Arranque Local

### Prerequisitos

- Java 21 (via SDKMAN: `sdk install java 21.0.2-tem`)
- Maven 3.9+ (via SDKMAN: `sdk install maven`)
- Node.js 20+ y npm
- Docker (para MySQL)

### 1. Levantar MySQL con Docker

```bash
docker run -d --name blackwolf-mysql \
  -e MYSQL_ROOT_PASSWORD=admin \
  -e MYSQL_DATABASE=blackwolf \
  -p 3306:3306 \
  mysql:8.0
```

### 2. Arrancar el Backend

```bash
cd backend
MYSQL_DATABASE=blackwolf MYSQL_PASSWORD=admin mvn spring-boot:run
```

El backend arranca en `http://localhost:8080`. Flyway ejecuta automaticamente las migraciones V1 y V2.

### 3. Arrancar el Frontend

```bash
cd frontend
npm install
npm run dev
```

El frontend arranca en `http://localhost:5173` con proxy configurado a `:8080`.

### 4. Primer Uso

```bash
# Registrar empresa
curl -X POST http://localhost:8080/api/v1/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "companyName": "Mi Empresa",
    "domain": "miempresa.com",
    "contactEmail": "admin@miempresa.com",
    "plan": "enterprise"
  }'

# Login en el navegador
# URL: http://localhost:5173
# Domain: miempresa.com
# Email: admin@miempresa.com
# Password: admin
```

---

## Despliegue con Docker

```bash
cd deploy
docker-compose up -d --build
```

Esto levanta 4 servicios:

| Servicio | Puerto | Descripcion |
|---|---|---|
| MySQL | 3306 | Base de datos |
| MinIO | 9000, 9001 | Almacenamiento objetos |
| Backend | 8081 | API Spring Boot |
| Frontend | 80 | React via Nginx |

---

## API Reference

### Autenticacion
| Metodo | Endpoint | Auth | Descripcion |
|---|---|---|---|
| POST | `/api/v1/auth/register` | No | Registrar empresa + admin |
| POST | `/api/v1/auth/login` | No | Login, devuelve JWT |

### Dashboard
| Metodo | Endpoint | Auth | Descripcion |
|---|---|---|---|
| GET | `/api/v1/dashboard/overview` | JWT | Estadisticas generales |

### Sensores
| Metodo | Endpoint | Auth | Descripcion |
|---|---|---|---|
| POST | `/api/v1/sensors/upload` | API Key | Envio de datos del sensor |
| GET | `/api/v1/sensors` | JWT | Listar sensores de la empresa |

### Amenazas
| Metodo | Endpoint | Auth | Descripcion |
|---|---|---|---|
| GET | `/api/v1/threats` | JWT | Listar con filtros y paginacion |
| PATCH | `/api/v1/threats/{id}/status` | JWT | Cambiar estado de amenaza |

### Auditorias
| Metodo | Endpoint | Auth | Descripcion |
|---|---|---|---|
| GET | `/api/v1/audits` | JWT | Listar auditorias |
| GET | `/api/v1/audits/{id}` | JWT | Detalle con findings |
| POST | `/api/v1/audits` | JWT | Crear auditoria |
| PATCH | `/api/v1/audits/{id}/status` | JWT | Avanzar estado |
| POST | `/api/v1/audits/{id}/findings` | JWT | Agregar finding |
| PATCH | `/api/v1/audits/findings/{id}/status` | JWT | Resolver finding |
| GET | `/api/v1/audits/{id}/activity` | JWT | Activity log |

### Pentests
| Metodo | Endpoint | Auth | Descripcion |
|---|---|---|---|
| GET | `/api/v1/pentests` | JWT | Listar pentests |
| GET | `/api/v1/pentests/{id}` | JWT | Detalle con findings |
| POST | `/api/v1/pentests` | JWT | Crear pentest |
| PATCH | `/api/v1/pentests/{id}/status` | JWT | Avanzar estado |
| POST | `/api/v1/pentests/{id}/findings` | JWT | Agregar finding |

### Vulnerabilidades
| Metodo | Endpoint | Auth | Descripcion |
|---|---|---|---|
| GET | `/api/v1/vulnerabilities` | JWT | Listar vulnerabilidades |
| GET | `/api/v1/vulnerabilities/{id}` | JWT | Detalle |
| POST | `/api/v1/vulnerabilities` | JWT | Reportar vulnerabilidad |
| PATCH | `/api/v1/vulnerabilities/{id}/status` | JWT | Avanzar estado |
| GET | `/api/v1/vulnerabilities/dashboard` | JWT | Resumen por estado/riesgo |

### Certificaciones y Metricas
| Metodo | Endpoint | Auth | Descripcion |
|---|---|---|---|
| GET | `/api/v1/certifications` | JWT | Listar certificaciones |
| GET | `/api/v1/certifications/{id}` | JWT | Detalle |
| GET | `/api/v1/certifications/eligibility/{auditId}` | JWT | Verificar elegibilidad |
| POST | `/api/v1/certifications` | JWT | Emitir certificacion |
| PATCH | `/api/v1/certifications/{id}/revoke` | JWT | Revocar |
| GET | `/api/v1/certifications/metrics` | JWT | KPIs SOC |

---

## Base de Datos

### Migracion V1: Tablas Base
- `companies` - Empresas registradas con API Key
- `users` - Usuarios del panel (FK company_id)
- `sensors` - Sensores de red desplegados (FK company_id)
- `threat_events` - Eventos de amenaza detectados (FK company_id)
- `blocked_ips` - IPs bloqueadas automaticamente (PK compuesta: ip + company_id)

### Migracion V2: Tablas SOC
- `security_audits` - Auditorias con lifecycle de 5 estados
- `audit_findings` - Hallazgos con CVSS scoring
- `penetration_tests` - Pentests con tipo BLACK/GREY/WHITE_BOX
- `pentest_findings` - Hallazgos con PoC y evidencia
- `vulnerabilities` - Lifecycle de 5 estados con plan de remediacion
- `security_certifications` - Certificaciones con validez de 12 meses
- `soc_metrics` - Tracking de KPIs personalizados
- `activity_log` - Audit trail de todas las operaciones

Todas las tablas SOC tienen indices en `company_id` y `status` para consultas eficientes.

---

## Seguridad y Autenticacion

### JWT (JSON Web Tokens)
- **Algoritmo**: HS512 (HMAC-SHA512)
- **Expiracion**: 24 horas
- **Almacenamiento**: `localStorage` en el frontend
- **Transmision**: Header `Authorization: Bearer <token>`
- **Subject**: User ID (UUID)

### Flujo de autenticacion
```
1. POST /auth/login -> AuthService valida credenciales -> JwtTokenProvider genera token
2. Frontend guarda token en localStorage
3. Axios interceptor anade "Bearer token" a cada request
4. JwtAuthenticationFilter extrae y valida el token en cada request
5. CustomUserDetailsService carga el usuario desde la BD
6. AuthUtils extrae companyId del usuario autenticado para aislar datos
7. Si el token expira o es invalido -> 401 -> frontend redirige a /login
```

### API Key para Sensores
- Cada empresa tiene un API Key unico generado al registrarse
- El endpoint `/sensors/upload` es publico (no requiere JWT) pero requiere API Key valida
- Se valida que el `company_id` del payload coincida con la empresa duena del API Key

### Aislamiento Multi-tenant
- Todos los controllers extraen el `companyId` del usuario autenticado via `AuthUtils`
- Las queries siempre filtran por `companyId`
- Si un usuario intenta acceder a datos de otra empresa, recibe HTTP 403

### CORS
- Origenes permitidos: configurable via variable de entorno `CORS_ALLOWED_ORIGINS`
- Default: `http://localhost:5173, http://localhost:80`
- Metodos: GET, POST, PUT, DELETE, OPTIONS
- Credentials habilitados

### Manejo de Errores (GlobalExceptionHandler)
| Condicion | HTTP Status |
|---|---|
| Entidad no encontrada | 404 Not Found |
| Acceso denegado | 403 Forbidden |
| Datos invalidos / duplicados | 400 Bad Request |
| Transicion de estado invalida | 422 Unprocessable Entity |
| Error interno | 500 Internal Server Error |
