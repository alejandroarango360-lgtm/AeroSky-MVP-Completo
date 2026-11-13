# ✈️ AeroSky - Sistema de Gestión Aerocomercial

Sistema integral de gestión aerocomercial desarrollado en **Java (Swing)** con base de datos **MySQL**. Automatiza los procesos de comercialización, reserva, emisión de tiquetes, posventa y operaciones administrativas, implementando altos estándares de **seguridad (2FA)** y **auditoría**.

Proyecto académico de la asignatura **Análisis y Diseño de Software** — Fundación Universitaria Konrad Lorenz.

---

## 🚀 Características Principales

### 🔐 Módulo de Seguridad y Usuarios (RBAC)
- **Autenticación de Doble Factor (2FA):** Inicio de sesión y registro de usuarios validados mediante códigos de 6 dígitos enviados al correo real (JavaMail).
- **Control de Acceso Basado en Roles:** Diferencia entre administradores y clientes. Los clientes no pueden acceder al panel administrativo.
- **Bloqueo de Cuentas:** Bloqueo automático de cuentas por 15 minutos tras 3 intentos fallidos (de contraseña o de código 2FA).
- **Gestión de Usuarios (Admin):** Creación, edición, eliminación, cambio de roles y desbloqueo de cuentas de usuario.

### 🎫 Módulo de Reservas y Ventas
- **Consulta de vuelos:** Búsqueda dinámica con filtros (escalas, horarios, precio máximo) y ordenamiento.
- **Ida y vuelta:** Validación de compatibilidad de tiempos entre vuelo de ida y regreso.
- **Generación de PNR:** Código alfanumérico único de 6 caracteres por reserva.
- **Bloqueo temporal de cupos:** Retención de asientos por 15 minutos mientras se completa el pago.

### 💳 Pagos y Posventa
- **Pasarela de Pagos Simulada:** Procesamiento de tarjetas (con validación algorítmica) y PSE.
- **Consulta de Tiquetes:** Módulo de consulta por PNR y apellido para ver el estado actual del vuelo (Confirmado, Cancelado, Expirado).
- **Cancelación Segura (2FA):** El reembolso y devolución de cupos requiere confirmación estricta mediante un código enviado al correo del titular.
- **Modificación de Vuelos:** Permite cambiar el itinerario calculando automáticamente diferencias de tarifa y penalidades.

### 📊 Módulo Administrativo y Analítica
- **CRUD de Vuelos:** Gestión total de rutas y frecuencias.
- **Notificaciones Operacionales:** Envío de correos masivos a pasajeros afectados por cancelaciones o reprogramaciones de vuelos.
- **Tablero Gerencial (KPIs):** Reportes en tiempo real de ingresos totales, tiquetes vendidos y cancelaciones, filtrables por fecha y ruta.
- **Auditoría Total:** Registro detallado (logs) de todos los eventos del sistema (pagos, bloqueos, accesos, modificaciones) con exportación a CSV.

---

## 🏗️ Arquitectura y Buenas Prácticas

El sistema sigue una estricta **arquitectura de 3 capas + Utils**:

```text
com.aircolombia.prototipo
├── dao/          → Capa de Persistencia (JDBC y consultas SQL)
├── model/        → Modelos de dominio (POJOs)
├── service/      → Capa de Negocio (lógica, validaciones y auditoría)
├── utils/        → Utilidades (Conexión BD, Envío de Correos)
└── view/         → Capa de Presentación (Interfaces Swing)
🗄️ Automatización en Base de Datos
Limpieza Automática: La base de datos cuenta con un Event Scheduler que limpia diariamente las reservas huérfanas, expiradas y canceladas (con más de 1 día de antigüedad) para evitar la sobrecarga de datos, preservando intacto el historial de auditoría.

Triggers y Relaciones: Uso de llaves foráneas complejas y borrado en cascada controlado.

🛠️ Tecnologías Utilizadas
Java 8+ (Programación Orientada a Objetos)

Java Swing (Interfaz Gráfica de Usuario)

JDBC con MySQL Connector/J

JavaMail API (Envío de correos SMTP reales)

MySQL / MariaDB (vía XAMPP)

NetBeans IDE

⚙️ Instalación y Ejecución
Requisitos previos:
Java JDK 8 o superior

MySQL / XAMPP

NetBeans IDE

Dependencias: mysql-connector-j, javax.mail, jakarta.activation

Pasos:
Clonar el repositorio:

bash
git clone https://github.com/tu-usuario/aerosky-sistema-aerocomercial.git
Crear la base de datos:

Abre phpMyAdmin y crea una base de datos (ej. aircolombia_mvp).

Importa el script database/aircolombia_mvp_completo.sql ubicado en el repositorio.

Opcional (Recomendado): Ejecuta el script de eventos para habilitar la limpieza automática de reservas viejas.

Configurar el correo (JavaMail):

Abre la clase EmailService.java en el paquete utils.

Ingresa un correo de Gmail y su respectiva "Contraseña de aplicación" en las constantes REMITENTE y PASSWORD.

Abrir el proyecto en NetBeans:

File → Open Project → selecciona la carpeta del proyecto.

Agregar las librerías:

Clic derecho en Libraries → Add JAR/Folder → Agrega los .jar de MySQL y JavaMail.

Ejecutar:

Corre el archivo FrmPrincipal.java (o FrmLogin.java si aplicaste la mejora de seguridad).

👥 Autores
Alejandro Arango

(Nota: Ajusta los nombres si es necesario).

📄 Licencia
Proyecto académico — Fundación Universitaria Konrad Lorenz. Uso educativo. Todos los derechos reservados a los autores.
