# ✈️ AeroSky - Sistema de Gestión Aerocomercial

Sistema de gestión aerocomercial desarrollado en **Java (Swing)** con base de datos **MySQL**, que automatiza los procesos de comercialización, reserva, emisión de tiquetes y gestiones operativas de vuelos.

Proyecto académico de la asignatura **Análisis y Diseño de Software** — Fundación Universitaria Konrad Lorenz.

---

## 🚀 Características Principales

- 🔍 **Consulta de vuelos** en tiempo real por origen y destino
- 🎫 **Compra de tiquetes** con bloqueo temporal de cupos (15 minutos)
- 💳 **Procesamiento de pagos** con simulación de pasarela y rechazo
- 📧 **Generación automática de PNR** único de 6 caracteres
- ❌ **Cancelación de tiquetes** con devolución de cupos
- 🛠️ **Módulo de administración** para gestionar vuelos (CRUD)
- 📊 **Auditoría de pagos** (cada intento aprobado/rechazado queda registrado)

---

## 🏗️ Arquitectura

El sistema sigue una **arquitectura de 3 capas**:

```
com.aircolombia.prototipo
├── dao/          → Capa de Persistencia (acceso a datos)
├── model/        → Modelos de dominio (POJOs)
├── service/      → Capa de Negocio (lógica y orquestación)
└── view/         → Capa de Presentación (interfaces Swing)
```

- **Presentación** → depende de Negocio
- **Negocio** → depende de Persistencia
- **Persistencia** → independiente (solo conoce el modelo de datos)

---

## 🗄️ Base de Datos

Nombre: **`aircolombia_mvp`** (MySQL/MariaDB)

### Tablas principales:

| Tabla | Descripción |
|---|---|
| `aeropuerto` | Catálogo de aeropuertos (IATA, ciudad, país) |
| `aeronave` | Flota de aeronaves |
| `vuelo` | Vuelos programados (origen, destino, fecha, tarifa) |
| `pasajero` | Datos de los pasajeros |
| `reserva` | Reservas con código PNR |
| `reserva_pasajero` | Relación reserva ↔ pasajero |
| `intentos_pago` | Auditoría de transacciones de pago |

---

## 🛠️ Tecnologías Utilizadas

- **Java 8+**
- **Swing** (interfaz gráfica)
- **JDBC** con MySQL Connector/J 9.4.0
- **MySQL / MariaDB** (vía XAMPP)
- **NetBeans IDE**

---

## ⚙️ Instalación y Ejecución

### Requisitos previos:
- Java JDK 8 o superior
- MySQL / XAMPP
- NetBeans IDE
- Driver `mysql-connector-j-9.4.0.jar`

### Pasos:

1. **Clonar el repositorio:**
   ```bash
   git clone https://github.com/tu-usuario/aerosky-sistema-aerocomercial.git
   ```

2. **Crear la base de datos:**
   - Abre phpMyAdmin
   - Ejecuta el script `database/aircolombia_mvp.sql`

3. **Abrir el proyecto en NetBeans:**
   - File → Open Project → selecciona la carpeta del proyecto

4. **Agregar el driver de MySQL:**
   - Clic derecho en Libraries → Add JAR/Folder → `mysql-connector-j-9.4.0.jar`

5. **Ejecutar:**
   - Run File sobre `FrmPrincipal.java`

---

## 📖 Flujo del Sistema

### Caso de uso principal: Compra de tiquetes

1. El usuario busca vuelos por origen, destino.
2. El sistema muestra vuelos disponibles con precios.
3. El usuario selecciona un vuelo y completa datos del pasajero.
4. El sistema crea una **reserva pendiente** y **bloquea el cupo** por 15 minutos.
5. El usuario ingresa método de pago (Tarjeta/PSE).
6. El sistema procesa el pago a través de una **pasarela simulada**.
7. Si el pago es aprobado → se confirma la reserva y se genera el **PNR**.
8. Si el pago es rechazado → se permite reintentar dentro del tiempo límite.

### Flujos alternativos:

- **FA1:** Sin disponibilidad → sugiere fechas cercanas
- **FA2:** Rechazo de pago → notifica motivo y permite reintentar
- **FA3:** Timeout de 15 min → libera cupo y expira la reserva

---

## 👥 Autores

- **Brian Steven Guevara Carvajal**
- **Kevin Valvuena**

---

## 📄 Licencia

Proyecto académico — Fundación Universitaria Konrad Lorenz. Uso educativo.