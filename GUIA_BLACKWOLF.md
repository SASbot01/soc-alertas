# BlackWolf Defense - Guia Completa del Sistema de Monitorizacion

---

## Credenciales de Acceso

### Superadmin (vision global de todas las empresas)
| Campo    | Valor                      |
|----------|----------------------------|
| Domain   | *(dejar vacio)*            |
| Email    | superadmin@blackwolf.io    |
| Password | superadmin                 |

### Usuario de empresa (BlackWolf Sec)
| Campo    | Valor                          |
|----------|--------------------------------|
| Domain   | blackwolfsec.io                |
| Email    | alejandro.cto@blackwolfsec.io  |
| Password | admin                          |

---

## Roles del Sistema

| Rol        | Que puede hacer                                                  |
|------------|------------------------------------------------------------------|
| user       | Ver dashboard, amenazas, sensores, auditorias, pentests, vulnerabilidades, certificaciones, metricas SOC, cambiar su contrasena |
| admin      | Todo lo de user + gestionar usuarios de su empresa (crear, editar, desactivar) |
| superadmin | Vision global de TODAS las empresas, dashboard agregado, drill-down por empresa |

---

## Seccion 1: Login

**Ruta:** `/login`

### Como funciona
1. **Domain:** Introduce el dominio de tu empresa (ej: `blackwolfsec.io`). Si eres superadmin, dejalo vacio.
2. **Email:** Tu correo electronico registrado.
3. **Password:** Tu contrasena.
4. Pulsa **"Access Secure Portal"**.

### Notas
- Si dejas el dominio vacio, el sistema busca un usuario superadmin con ese email.
- Si introduces un dominio, busca la empresa y luego el usuario dentro de esa empresa.

---

## Seccion 2: Overview (Dashboard)

**Ruta:** `/`
**Menu lateral:** Overview
**Roles:** user, admin

### Que muestra
Este es el panel principal de monitorizacion en tiempo real de TU empresa. Se refresca automaticamente cada 30 segundos.

**Tarjetas de estadisticas:**
- **Total Threats:** Numero total de amenazas detectadas historicamente.
- **Active Sensors:** Cuantos sensores de red estan online.
- **Threats Today:** Amenazas detectadas hoy.
- **Blocked IPs:** Direcciones IP bloqueadas actualmente.

**Grafico de distribucion de ataques:**
- Muestra los tipos de ataque detectados (DDoS, brute force, malware, etc.) y su frecuencia.

**Amenazas recientes:**
- Lista de las ultimas 10 amenazas con tipo, IPs origen/destino, y hora.
- Las amenazas con severidad >= 4 aparecen en rojo.

### Como usar
- Simplemente observa. Los datos se actualizan solos.
- Haz clic en "View All" para ir a la seccion de Threats con mas detalle.

---

## Seccion 3: Threats (Amenazas)

**Ruta:** `/threats`
**Menu lateral:** Threats
**Roles:** user, admin

### Que muestra
Listado detallado de todas las amenazas detectadas por los sensores de tu empresa.

**Columnas de la tabla:**
| Columna     | Descripcion                                       |
|-------------|---------------------------------------------------|
| Threat Type | Tipo de ataque (DDoS, SQL Injection, Brute Force) |
| Severity    | Nivel 1-10 con codigo de colores                  |
| Source IP   | IP de donde viene el ataque                       |
| Target IP   | IP destino del ataque                              |
| Status      | Detected, Blocked, o Resolved                     |
| Timestamp   | Fecha y hora de la deteccion                       |

### Como usar
1. **Filtrar por estado:** Usa el filtro de Status para ver solo amenazas "Detected" (pendientes), "Blocked" (bloqueadas), o "Resolved" (resueltas).
2. **Filtrar por severidad:** Filtra por severidad minima (3+, 5+, 7+) para enfocarte en las mas criticas.
3. **Buscar por IP:** Escribe una IP en el buscador para encontrar amenazas de/hacia esa direccion.
4. **Paginacion:** Navega entre paginas (20 amenazas por pagina).

### Caso de uso tipico
> "Quiero ver todas las amenazas criticas (severidad 7+) que aun no se han resuelto"
> - Filtro Status: Detected
> - Filtro Severity: 7+

---

## Seccion 4: Sensors (Sensores)

**Ruta:** `/sensors`
**Menu lateral:** Sensors
**Roles:** user, admin

### Que muestra
Tarjetas con el estado de cada sensor de red desplegado en tu infraestructura.

**Por cada sensor:**
- **Nombre e ID** del sensor.
- **Status:** Online (verde) u Offline (rojo).
- **Threats Found:** Amenazas detectadas por ese sensor.
- **Packets Processed:** Paquetes de red analizados (en miles).
- **Last Seen:** Ultima vez que el sensor reporto actividad.

### Como usar
- Revisa que todos los sensores esten **Online**.
- Si un sensor aparece **Offline**, investiga si hay un problema de red o si el sensor se ha caido.
- Pulsa **Refresh** para actualizar el estado manualmente.

### Caso de uso tipico
> "Un sensor no reporta desde hace 2 horas"
> - Revisa la tarjeta del sensor, mira "Last Seen".
> - Si esta Offline, contacta al equipo de infraestructura.

---

## Seccion 5: Audits (Auditorias de Seguridad)

**Ruta:** `/audits`
**Menu lateral:** SOC Operations > Audits
**Roles:** user, admin

### Que muestra
Listado de proyectos de auditoria de seguridad de la empresa.

**Columnas:**
| Columna      | Descripcion                                              |
|--------------|----------------------------------------------------------|
| Title        | Nombre de la auditoria                                    |
| Type         | Infrastructure, Application, Network, Compliance, Cloud  |
| Status       | Scoping > Scanning > Testing > Reporting > Delivered     |
| Lead Auditor | Auditor responsable                                       |
| Start Date   | Fecha de inicio                                           |

### Como usar

**Crear una auditoria:**
1. Pulsa **"New Audit"**.
2. Rellena: titulo, tipo, scope (alcance), auditor principal, metodologia.
3. Pulsa **Create**.

**Ver detalle de una auditoria:**
1. Haz clic en cualquier fila de la tabla.
2. Veras:
   - **Timeline de estado:** Progreso visual por las 5 etapas.
   - **Hallazgos (Findings):** Lista de problemas encontrados con nivel de riesgo y CVSS.
   - **Resumen ejecutivo.**
   - **Log de actividad:** Historial de cambios con timestamps.

**Avanzar el estado:**
- En la vista detalle, pulsa el boton para avanzar al siguiente estado (ej: de Scanning a Testing).

**Agregar hallazgos:**
1. En la vista detalle, pulsa **"Add Finding"**.
2. Rellena: titulo, descripcion, nivel de riesgo (CRITICAL/HIGH/MEDIUM/LOW/INFO), puntuacion CVSS, activo afectado, recomendacion.

---

## Seccion 6: Pentests (Pruebas de Penetracion)

**Ruta:** `/pentests`
**Menu lateral:** SOC Operations > Pentests
**Roles:** user, admin

### Que muestra
Gestion de pruebas de penetracion (hacking etico) realizadas a la empresa.

**Tipos de pentest:**
| Tipo      | Descripcion                                           |
|-----------|-------------------------------------------------------|
| Black Box | El tester no tiene informacion previa del sistema     |
| Grey Box  | El tester tiene informacion parcial                   |
| White Box | El tester tiene acceso completo al codigo y documentacion |

**Estados del pentest:**
Planning > Reconnaissance > Exploitation > Post-Exploitation > Reporting > Delivered

### Como usar

**Crear un pentest:**
1. Pulsa **"New Pentest"**.
2. Rellena: titulo, tipo, scope, reglas de engagement, sistemas objetivo, tester.

**Ver detalle:**
1. Clic en la fila del pentest.
2. Veras la timeline de progreso, hallazgos con severidad, y detalles del engagement.

**Agregar hallazgos de pentest:**
1. En la vista detalle, pulsa **"Add Finding"**.
2. Rellena: titulo, componente afectado, severidad, CVSS, proof of concept, evidencia, recomendacion.

---

## Seccion 7: Vulnerabilities (Vulnerabilidades)

**Ruta:** `/vulnerabilities`
**Menu lateral:** SOC Operations > Vulnerabilities
**Roles:** user, admin

### Que muestra
Registro y seguimiento de vulnerabilidades encontradas en la infraestructura.

**Columnas:**
| Columna        | Descripcion                                           |
|----------------|-------------------------------------------------------|
| Title          | Nombre descriptivo de la vulnerabilidad               |
| CVE ID         | Identificador publico (ej: CVE-2024-1234)            |
| Risk Level     | CRITICAL, HIGH, MEDIUM, LOW, INFO                    |
| CVSS Score     | Puntuacion de severidad (0-10)                       |
| Status         | Detected > Confirmed > In Remediation > Fixed > Verified |
| Affected Asset | Sistema o componente afectado                         |

### Como usar

**Reportar una vulnerabilidad:**
1. Pulsa **"Report Vulnerability"**.
2. Rellena: titulo, CVE ID, nivel de riesgo, CVSS, activo afectado, descripcion, plan de remediacion.

**Filtrar:**
- Por estado: ver solo las que estan "In Remediation" para hacer seguimiento.
- Por riesgo: ver solo las CRITICAL para priorizar.

**Ciclo de vida:**
1. Se **detecta** la vulnerabilidad.
2. Se **confirma** que es real (no falso positivo).
3. Se pone **en remediacion** (el equipo esta trabajando en el fix).
4. Se marca como **fixed** (corregida).
5. Se **verifica** que la correccion es efectiva.

**Vista detalle:**
- Clic en la fila para ver descripcion completa, plan de remediacion, deadlines, y avanzar el estado.

---

## Seccion 8: Certifications (Certificaciones)

**Ruta:** `/certifications`
**Menu lateral:** SOC Operations > Certifications
**Roles:** user, admin

### Que muestra
Certificaciones de seguridad y compliance de la empresa.

**Tipos soportados:**
- ISO 27001
- SOC 2
- PCI DSS
- GDPR
- HIPAA
- NIST

**Estados:**
| Estado  | Significado                                |
|---------|--------------------------------------------|
| Active  | Certificacion vigente                      |
| Expired | Certificacion vencida                      |
| Revoked | Certificacion revocada                     |
| Pending | Pendiente de emision                       |

### Como usar

**Emitir una certificacion:**
1. Pulsa **"Issue Certification"**.
2. Rellena: titulo, tipo, auditoria base, emitido por, notas.

**Revocar una certificacion:**
- En la tarjeta de la certificacion activa, pulsa **"Revoke"** y confirma.

**Alertas de vencimiento:**
- Las certificaciones que vencen en menos de 30 dias se resaltan con una alerta.

---

## Seccion 9: SOC Metrics (Metricas del SOC)

**Ruta:** `/soc-metrics`
**Menu lateral:** SOC Operations > SOC Metrics
**Roles:** user, admin

### Que muestra
Panel de KPIs (indicadores clave de rendimiento) del centro de operaciones de seguridad.

**Metricas principales (tarjetas):**
| Metrica                  | Que mide                                           |
|--------------------------|-----------------------------------------------------|
| Total Audits             | Auditorias totales (y cuantas completadas)          |
| Total Pentests           | Pentests totales (y cuantos completados)            |
| Open Vulnerabilities     | Vulnerabilidades abiertas (y cuantas corregidas)    |
| Active Certifications    | Certificaciones activas                              |
| Avg Remediation Days     | Dias promedio para corregir una vulnerabilidad      |
| Audit Delivery Days      | Dias promedio para completar una auditoria          |
| Fixed Vulnerabilities    | Vulnerabilidades corregidas con barra de progreso   |
| Completion Rate          | Porcentaje de completitud general                    |

**Graficos:**
- **Barras:** Completados vs Total (auditorias y pentests).
- **Pie:** Vulnerabilidades abiertas vs corregidas.

**Tabla de metricas custom:**
- Nombre de la metrica, valor, unidad, objetivo, periodo, fecha de registro.

### Como usar
- Es un panel de solo lectura. Usalo para evaluar el rendimiento de tu equipo de seguridad.
- Identifica cuellos de botella: si "Avg Remediation Days" es muy alto, tu equipo tarda demasiado en corregir vulnerabilidades.

---

## Seccion 10: Users (Gestion de Usuarios)

**Ruta:** `/users`
**Menu lateral:** Administration > Users
**Roles:** admin solamente

### Que muestra
Tabla con todos los usuarios de tu empresa.

**Columnas:**
| Columna  | Descripcion                     |
|----------|---------------------------------|
| Name     | Nombre completo con avatar      |
| Email    | Correo electronico              |
| Role     | user o admin                    |
| Status   | Active o Inactive               |
| Created  | Fecha de creacion               |
| Actions  | Botones de editar y eliminar    |

### Como usar

**Crear un usuario:**
1. Pulsa **"Add User"**.
2. Rellena: email, nombre completo, rol (user/admin), contrasena.
3. Pulsa **"Create User"**.
4. El nuevo usuario ya puede loguearse con el dominio de tu empresa.

**Editar un usuario:**
1. Pulsa el icono de lapiz en la fila del usuario.
2. Cambia: nombre, rol, o activa/desactiva el toggle de "Active".
3. Pulsa **"Save Changes"**.

**Desactivar un usuario:**
1. Pulsa el icono de papelera.
2. Confirma la desactivacion.
3. El usuario ya no podra loguearse, pero su registro se mantiene (soft delete).

---

## Seccion 11: Settings (Configuracion)

**Ruta:** `/settings`
**Menu lateral:** Settings
**Roles:** user, admin, superadmin

### Que muestra
Formulario para cambiar tu contrasena.

### Como usar
1. Introduce tu **contrasena actual**.
2. Introduce la **nueva contrasena** (minimo 4 caracteres).
3. **Confirma** la nueva contrasena.
4. Pulsa **"Change Password"**.
5. Veras un mensaje verde de exito o rojo de error.

---

## Seccion 12: SuperAdmin - Global Overview

**Ruta:** `/` (cuando el usuario es superadmin)
**Menu lateral:** Global Overview
**Roles:** superadmin solamente

### Que muestra
Dashboard agregado de TODAS las empresas de la plataforma.

**Tarjetas globales:**
| Metrica         | Descripcion                                |
|-----------------|--------------------------------------------|
| Companies       | Total de empresas registradas              |
| Total Threats   | Amenazas de todas las empresas             |
| Threats Today   | Amenazas de hoy en todas las empresas      |
| Active Sensors  | Sensores activos / Total sensores          |
| Blocked IPs     | IPs bloqueadas en toda la plataforma       |

**Grafico:**
- Distribucion global de tipos de ataque (todas las empresas combinadas).

**Lista de empresas:**
- Cada empresa muestra: nombre, dominio, numero de amenazas, numero de sensores.
- Haz clic en una empresa para ver su dashboard individual.

### Como usar
- Monitoriza la salud global de la plataforma.
- Identifica que empresa tiene mas amenazas.
- Haz clic en "View All" para ir a la tabla completa de empresas.

---

## Seccion 13: SuperAdmin - Companies

**Ruta:** `/superadmin/companies`
**Menu lateral:** Companies
**Roles:** superadmin solamente

### Que muestra
Tabla completa de todas las empresas registradas.

**Columnas:**
| Columna  | Descripcion                           |
|----------|---------------------------------------|
| Company  | Nombre de la empresa                  |
| Domain   | Dominio                               |
| Plan     | Plan contratado (starter, pro, etc.)  |
| Status   | Activa o inactiva                     |
| Threats  | Numero total de amenazas              |
| Sensors  | Numero de sensores desplegados        |
| Actions  | Boton "View Dashboard"                |

### Como usar
1. Revisa la tabla para tener una vision general.
2. Pulsa **"View Dashboard"** en cualquier empresa para ver su dashboard de seguridad completo (el mismo que veria un admin de esa empresa).

---

## Seccion 14: SuperAdmin - Company Detail (Drill-down)

**Ruta:** `/superadmin/company/:id`
**Menu lateral:** Companies (detalle)
**Roles:** superadmin solamente

### Que muestra
El dashboard de seguridad de UNA empresa especifica, identico al que ve un admin de esa empresa:
- Tarjetas de estadisticas (threats, sensors, blocked IPs).
- Grafico de distribucion de ataques.
- Lista de amenazas recientes.

### Como usar
- Llega aqui desde la tabla de Companies o desde el dashboard global.
- Pulsa la flecha **atras** para volver a la lista de empresas.
- Usa esta vista para investigar problemas en una empresa concreta.

---

## Flujos de Trabajo Comunes

### "Quiero ver todas las alertas/amenazas de mi empresa"
1. Ve a **Threats** en el menu lateral.
2. Sin filtros, veras todas las amenazas historicas.
3. Filtra por "Detected" para ver las pendientes de accion.

### "Quiero ver alertas de TODAS las empresas" (solo superadmin)
1. Logueate como superadmin (dominio vacio).
2. El **Global Overview** muestra amenazas agregadas de todas las empresas.
3. Haz clic en una empresa especifica para ver sus amenazas.
4. En el drill-down veras las amenazas recientes de esa empresa.

### "Quiero hacer seguimiento a una vulnerabilidad critica"
1. Ve a **Vulnerabilities**.
2. Filtra por Risk Level: CRITICAL.
3. Clic en la vulnerabilidad para ver el detalle.
4. Avanza el estado a medida que se trabaja en la correccion:
   Detected > Confirmed > In Remediation > Fixed > Verified.

### "Quiero saber si mis certificaciones estan al dia"
1. Ve a **Certifications**.
2. Revisa que todas esten en estado **Active**.
3. Las que tienen alerta de vencimiento (< 30 dias) necesitan renovacion.

### "Quiero evaluar el rendimiento de mi equipo SOC"
1. Ve a **SOC Metrics**.
2. Revisa "Avg Remediation Days" (cuanto tardan en corregir vulnerabilidades).
3. Revisa "Completion Rate" (porcentaje de trabajo completado).
4. Compara auditorias y pentests completados vs totales.

### "Quiero crear un nuevo usuario para mi equipo"
1. Logueate como **admin**.
2. Ve a **Users** en el menu lateral (seccion Administration).
3. Pulsa **"Add User"**.
4. El nuevo usuario podra loguearse con el mismo dominio de empresa.

### "Quiero cambiar mi contrasena"
1. Ve a **Settings** en el menu lateral.
2. Introduce tu contrasena actual, la nueva, y confirma.
3. La proxima vez que hagas login, usa la nueva contrasena.
