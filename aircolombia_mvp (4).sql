-- phpMyAdmin SQL Dump
-- version 5.2.1
-- https://www.phpmyadmin.net/
--
-- Servidor: 127.0.0.1:3306
-- Tiempo de generación: 10-11-2026 a las 07:12:20
-- Versión del servidor: 10.4.32-MariaDB
-- Versión de PHP: 8.2.12

SET SQL_MODE = "NO_AUTO_VALUE_ON_ZERO";
START TRANSACTION;
SET time_zone = "+00:00";


/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8mb4 */;

--
-- Base de datos: `aircolombia_mvp`
--

-- --------------------------------------------------------

--
-- Estructura de tabla para la tabla `aeronave`
--

CREATE TABLE `aeronave` (
  `id_aeronave` int(11) NOT NULL,
  `matricula` varchar(10) NOT NULL,
  `capacidad_pasajeros` int(11) NOT NULL,
  `estado_operativo` varchar(20) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--
-- Volcado de datos para la tabla `aeronave`
--

INSERT INTO `aeronave` (`id_aeronave`, `matricula`, `capacidad_pasajeros`, `estado_operativo`) VALUES
(1, 'HK-5321', 180, 'Operativo'),
(2, 'HK-4522', 220, 'Operativo'),
(3, 'HK-1234', 300, 'Operativo');

-- --------------------------------------------------------

--
-- Estructura de tabla para la tabla `aeropuerto`
--

CREATE TABLE `aeropuerto` (
  `codigo_iata` varchar(3) NOT NULL,
  `nombre` varchar(100) NOT NULL,
  `ciudad` varchar(50) DEFAULT NULL,
  `pais` varchar(50) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--
-- Volcado de datos para la tabla `aeropuerto`
--

INSERT INTO `aeropuerto` (`codigo_iata`, `nombre`, `ciudad`, `pais`) VALUES
('BOG', 'El Dorado', 'Bogotá', 'Colombia'),
('CLO', 'Alfonso Bonilla Aragón', 'Cali', 'Colombia'),
('CTG', 'Rafael Núñez', 'Cartagena', 'Colombia'),
('MDE', 'José María Córdova', 'Medellín', 'Colombia');

-- --------------------------------------------------------

--
-- Estructura de tabla para la tabla `auditoria_eventos`
--

CREATE TABLE `auditoria_eventos` (
  `id_auditoria` bigint(20) NOT NULL,
  `codigo_pnr` varchar(10) NOT NULL,
  `evento` varchar(50) NOT NULL,
  `resultado` varchar(20) NOT NULL,
  `detalle` varchar(500) DEFAULT NULL,
  `usuario` varchar(100) DEFAULT NULL,
  `ip` varchar(45) DEFAULT NULL,
  `fecha_evento` datetime NOT NULL DEFAULT current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Volcado de datos para la tabla `auditoria_eventos`
--

INSERT INTO `auditoria_eventos` (`id_auditoria`, `codigo_pnr`, `evento`, `resultado`, `detalle`, `usuario`, `ip`, `fecha_evento`) VALUES
(1, 'AVB2NW', 'RESERVA_EXPIRADA_MANUAL', 'OK', 'Reserva expirada manualmente (cancelación del usuario).', NULL, NULL, '2026-11-07 18:19:53'),
(2, 'AVGBLH', 'RESERVA_INICIADA', 'OK', 'Reserva creada. Total: 380000.0 | Asientos: 1', NULL, NULL, '2026-11-07 18:20:30'),
(3, 'AVGBLH', 'CODIGO_ENVIADO', 'OK', 'Correo enviado a arangoalejandro360@gmail.com', NULL, NULL, '2026-11-07 18:21:03'),
(4, 'AVGBLH', 'TOKEN_EXPIRADO', 'BLOQUEADO', 'Código expirado tras 18021s. Reserva bloqueada.', NULL, NULL, '2026-11-07 18:21:22'),
(5, 'AVGBLH', 'ERROR_ENVIO_CORREO_BLOQUEO', 'ERROR', 'Correo del comprador no disponible.', NULL, NULL, '2026-11-07 18:21:22'),
(6, 'AVGBLH', 'RESERVA_EXPIRADA_MANUAL', 'OK', 'Reserva expirada manualmente (cancelación del usuario).', NULL, NULL, '2026-11-07 18:21:37'),
(7, 'AVB3YK', 'RESERVA_INICIADA', 'OK', 'Reserva creada. Total: 380000.0 | Asientos: 1', NULL, NULL, '2026-11-07 18:22:04'),
(8, 'AVB3YK', 'CODIGO_ENVIADO', 'OK', 'Correo enviado a arangoalejandro360@gmail.com', NULL, NULL, '2026-11-07 18:22:57'),
(9, 'AVB3YK', 'TOKEN_EXPIRADO', 'BLOQUEADO', 'Código expirado tras 18014s. Reserva bloqueada.', NULL, NULL, '2026-11-07 18:23:09'),
(10, 'AVB3YK', 'ERROR_ENVIO_CORREO_BLOQUEO', 'ERROR', 'Correo del comprador no disponible.', NULL, NULL, '2026-11-07 18:23:09'),
(11, 'AVB3YK', 'RESERVA_EXPIRADA_MANUAL', 'OK', 'Reserva expirada manualmente (cancelación del usuario).', NULL, NULL, '2026-11-07 18:23:37'),
(12, 'AVW725', 'RESERVA_INICIADA', 'OK', 'Reserva creada. Total: 320000.0 | Asientos: 1', NULL, NULL, '2026-11-07 18:28:57'),
(13, 'AVW725', 'CODIGO_ENVIADO', 'OK', 'Correo enviado a arangoalejandro360@gmail.com', NULL, NULL, '2026-11-07 18:29:30'),
(14, 'AVW725', 'VERIFICACION_EXITOSA', 'OK', 'Código verificado correctamente.', NULL, NULL, '2026-11-07 18:29:41'),
(15, 'AVW725', 'PAGO_APROBADO', 'OK', 'Total: 320000.0 | Método: Tarjeta de Crédito', NULL, NULL, '2026-11-07 18:29:41'),
(16, 'AVW725', 'TOKEN_CONSUMIDO', 'OK', 'Token invalidado tras pago exitoso.', NULL, NULL, '2026-11-07 18:29:41'),
(17, 'AVW725', 'RESERVA_CANCELADA', 'OK', 'Cancelación con penalidad. Total: 320000.0 | Penalidad: 96000.0 | Reembolso: 224000.0', NULL, NULL, '2026-11-07 18:30:17'),
(18, 'AV8DRM', 'RESERVA_INICIADA', 'OK', 'Reserva creada. Total: 320000.0 | Asientos: 1', NULL, NULL, '2026-11-07 18:43:51'),
(19, 'AV8DRM', 'CODIGO_ENVIADO', 'OK', 'Correo enviado a arangoalejandro360@gmail.com', NULL, NULL, '2026-11-07 18:44:26'),
(20, 'AV8DRM', 'VERIFICACION_FALLIDA', 'RECHAZADO', 'Intento fallido 1 de 3', NULL, NULL, '2026-11-07 18:44:38'),
(21, 'AV8DRM', 'VERIFICACION_FALLIDA', 'RECHAZADO', 'Intento fallido 2 de 3', NULL, NULL, '2026-11-07 18:44:40'),
(22, 'AV8DRM', 'VERIFICACION_FALLIDA', 'RECHAZADO', 'Intento fallido 3 de 3', NULL, NULL, '2026-11-07 18:44:42'),
(23, 'AV8DRM', 'RESERVA_BLOQUEADA', 'BLOQUEADO', 'Bloqueada por 3 intentos fallidos.', NULL, NULL, '2026-11-07 18:44:42'),
(24, 'AV8DRM', 'BLOQUEO_NOTIFICADO', 'OK', 'Correo de bloqueo enviado a arangoalejandro360@gmail.com', NULL, NULL, '2026-11-07 18:44:44'),
(25, 'AV8DRM', 'RESERVA_EXPIRADA_MANUAL', 'OK', 'Reserva expirada manualmente (cancelación del usuario).', NULL, NULL, '2026-11-07 18:47:59'),
(26, 'AVCAKY', 'RESERVA_INICIADA', 'OK', 'Reserva creada. Total: 352000.0 | Asientos: 1', NULL, NULL, '2026-11-07 18:54:53'),
(27, 'AVCAKY', 'RESERVA_EXPIRADA_MANUAL', 'OK', 'Reserva expirada manualmente (cancelación del usuario).', NULL, NULL, '2026-11-07 18:55:33'),
(28, 'AVPXTD', 'RESERVA_INICIADA', 'OK', 'Reserva creada. Total: 352000.0 | Asientos: 1', NULL, NULL, '2026-11-07 18:55:58'),
(29, 'AVPXTD', 'CODIGO_ENVIADO', 'OK', 'Correo enviado a arangoalejandro360@gmail.com', NULL, NULL, '2026-11-07 18:57:17'),
(30, 'AVPXTD', 'VERIFICACION_EXITOSA', 'OK', 'Código verificado correctamente.', NULL, NULL, '2026-11-07 18:57:44'),
(31, 'AVPXTD', 'PAGO_APROBADO', 'OK', 'Total: 352000.0 | Método: Tarjeta de Crédito', NULL, NULL, '2026-11-07 18:57:44'),
(32, 'AVPXTD', 'TOKEN_CONSUMIDO', 'OK', 'Token invalidado tras pago exitoso.', NULL, NULL, '2026-11-07 18:57:44'),
(33, 'AVPXTD', 'RESERVA_CANCELADA', 'OK', 'Cancelación con penalidad. Total: 352000.0 | Penalidad: 105600.0 | Reembolso: 246400.0', NULL, NULL, '2026-11-07 18:58:37'),
(34, 'AV5CFY', 'RESERVA_INICIADA', 'OK', 'Reserva creada. Total: 320000.0 | Asientos: 1', NULL, NULL, '2026-11-07 18:59:01'),
(35, 'AV5CFY', 'CODIGO_ENVIADO', 'OK', 'Correo enviado a arangoalejandro360@gmail.com', NULL, NULL, '2026-11-07 18:59:45'),
(36, 'AV5CFY', 'VERIFICACION_FALLIDA', 'RECHAZADO', 'Intento fallido 1 de 3', NULL, NULL, '2026-11-07 18:59:51'),
(37, 'AV5CFY', 'VERIFICACION_FALLIDA', 'RECHAZADO', 'Intento fallido 2 de 3', NULL, NULL, '2026-11-07 18:59:53'),
(38, 'AV5CFY', 'VERIFICACION_FALLIDA', 'RECHAZADO', 'Intento fallido 3 de 3', NULL, NULL, '2026-11-07 18:59:55'),
(39, 'AV5CFY', 'RESERVA_BLOQUEADA', 'BLOQUEADO', 'Bloqueada por 3 intentos fallidos.', NULL, NULL, '2026-11-07 18:59:55'),
(40, 'AV5CFY', 'BLOQUEO_NOTIFICADO', 'OK', 'Correo de bloqueo enviado a arangoalejandro360@gmail.com', NULL, NULL, '2026-11-07 18:59:58'),
(41, 'AV5CFY', 'RESERVA_EXPIRADA_MANUAL', 'OK', 'Reserva expirada manualmente (cancelación del usuario).', NULL, NULL, '2026-11-07 19:00:28'),
(42, 'AVWT8S', 'RESERVA_INICIADA', 'OK', 'Reserva creada. Total: 320000.0 | Asientos: 1', NULL, NULL, '2026-11-07 20:31:58'),
(43, 'AVWT8S', 'CODIGO_ENVIADO', 'OK', 'Correo enviado a arangoalejandro360@gmail.com', NULL, NULL, '2026-11-07 20:32:29'),
(44, 'AVWT8S', 'VERIFICACION_FALLIDA', 'RECHAZADO', 'Intento fallido 1 de 3', NULL, NULL, '2026-11-07 20:32:48'),
(45, 'AVWT8S', 'VERIFICACION_FALLIDA', 'RECHAZADO', 'Intento fallido 2 de 3', NULL, NULL, '2026-11-07 20:32:51'),
(46, 'AVWT8S', 'VERIFICACION_FALLIDA', 'RECHAZADO', 'Intento fallido 3 de 3', NULL, NULL, '2026-11-07 20:32:54'),
(47, 'AVWT8S', 'RESERVA_BLOQUEADA', 'BLOQUEADO', 'Bloqueada por 3 intentos fallidos.', NULL, NULL, '2026-11-07 20:32:54'),
(48, 'AVWT8S', 'BLOQUEO_NOTIFICADO', 'OK', 'Correo de bloqueo enviado a arangoalejandro360@gmail.com', NULL, NULL, '2026-11-07 20:32:56'),
(49, 'AVWT8S', 'RESERVA_EXPIRADA_MANUAL', 'OK', 'Reserva expirada manualmente (cancelación del usuario).', NULL, NULL, '2026-11-07 20:34:26'),
(50, 'SISTEMA', 'LOGIN_PASO1_OK', 'OK', 'User: admin esperando 2FA.', NULL, NULL, '2026-11-10 00:50:45'),
(51, 'SISTEMA', 'LOGIN_EXITOSO', 'OK', 'User: admin accedió al sistema.', NULL, NULL, '2026-11-10 00:51:04'),
(52, 'SISTEMA', 'LOGIN_PASO1_OK', 'OK', 'User: admin esperando 2FA.', NULL, NULL, '2026-11-10 00:51:54'),
(53, 'SISTEMA', 'LOGIN_EXITOSO', 'OK', 'User: admin accedió al sistema.', NULL, NULL, '2026-11-10 00:52:15'),
(54, 'SISTEMA', 'LOGIN_PASO1_OK', 'OK', 'User: admin esperando 2FA.', NULL, NULL, '2026-11-10 01:07:12'),
(55, 'SISTEMA', 'LOGIN_EXITOSO', 'OK', 'User: admin accedió al sistema.', NULL, NULL, '2026-11-10 01:07:22');

-- --------------------------------------------------------

--
-- Estructura de tabla para la tabla `intentos_pago`
--

CREATE TABLE `intentos_pago` (
  `id_intento` int(11) NOT NULL,
  `codigo_pnr` varchar(6) NOT NULL,
  `fecha_intento` timestamp NOT NULL DEFAULT current_timestamp(),
  `monto` decimal(10,2) NOT NULL,
  `metodo_pago` varchar(50) NOT NULL,
  `numero_tarjeta` varchar(20) DEFAULT NULL,
  `resultado` varchar(20) NOT NULL,
  `motivo` varchar(200) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--
-- Volcado de datos para la tabla `intentos_pago`
--

INSERT INTO `intentos_pago` (`id_intento`, `codigo_pnr`, `fecha_intento`, `monto`, `metodo_pago`, `numero_tarjeta`, `resultado`, `motivo`) VALUES
(1, '3FD7A0', '2026-10-14 22:33:02', 380000.00, 'Tarjeta de Crédito', '1234567890123456', 'Aprobado', 'Transacción aprobada por la pasarela'),
(2, 'BDC675', '2026-10-16 18:54:44', 470000.00, 'Tarjeta de Crédito', '1023654789987456', 'Aprobado', 'Transacción aprobada por la pasarela'),
(3, 'F8EDF9', '2026-10-16 18:58:12', 470000.00, 'Tarjeta de Crédito', '1023654789987456', 'Aprobado', 'Transacción aprobada por la pasarela'),
(4, '7ED07E', '2026-10-16 23:59:09', 800000.00, 'Tarjeta de Crédito', '5558222577844456', 'Aprobado', 'Transacción aprobada por la pasarela'),
(5, '856FA1', '2026-10-21 22:35:11', 1045000.00, 'Tarjeta de Crédito', '1236547899874566', 'Aprobado', 'Transacción aprobada por la pasarela'),
(6, 'AVCX3R', '2026-10-22 00:24:31', 380000.00, 'Tarjeta de Crédito', '1236547899874556', 'Aprobado', 'Transacción aprobada por la pasarela'),
(7, 'AVW725', '2026-11-07 23:29:41', 320000.00, 'Tarjeta de Crédito', '1023654789874666', 'Aprobado', 'Transacción aprobada por la pasarela'),
(8, 'AVPXTD', '2026-11-07 23:57:44', 352000.00, 'Tarjeta de Crédito', '1023654878987456', 'Aprobado', 'Transacción aprobada por la pasarela');

-- --------------------------------------------------------

--
-- Estructura de tabla para la tabla `pasajero`
--

CREATE TABLE `pasajero` (
  `id_pasajero` int(11) NOT NULL,
  `tipo_documento` varchar(10) NOT NULL,
  `numero_documento` varchar(20) NOT NULL,
  `nombre_completo` varchar(100) NOT NULL,
  `email` varchar(80) DEFAULT NULL,
  `telefono` varchar(20) DEFAULT NULL,
  `direccion` varchar(150) DEFAULT NULL,
  `ciudad_residencia` varchar(50) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--
-- Volcado de datos para la tabla `pasajero`
--

INSERT INTO `pasajero` (`id_pasajero`, `tipo_documento`, `numero_documento`, `nombre_completo`, `email`, `telefono`, `direccion`, `ciudad_residencia`) VALUES
(1, 'cc', '1025530359', 'Juan Pérez', 'juan@test.com', '3001234567', NULL, NULL),
(2, 'CC', '1025565443', 'Juan Camilo Mierdalo', 'MIERDALOGRIMALDO@test.com', '3215896967', NULL, NULL),
(3, 'CC', '145582269', 'Alejandro A', 'Alejandro@test.com', '3215252487', NULL, NULL),
(4, 'CC', '9554616123', 'alejandro', 'alej@test.com', NULL, NULL, NULL),
(5, 'CC', '10245698', 'david', 'david@test.com', NULL, NULL, NULL),
(6, 'CC', '1024578', 'Luis', 'luis@test.com', NULL, NULL, NULL),
(7, 'CC', '1025456852', 'Alejandro', 'alejandro@test.com', NULL, NULL, NULL),
(8, 'CC', '10255545789', 'alejandro', 'alejandro@test.com', NULL, NULL, NULL),
(9, 'CC', '1454787656', 'sofia', 'sofia@test.com', NULL, NULL, NULL),
(10, 'CC', '61321365132', 'Emily', 'emily@test.com', NULL, NULL, NULL),
(11, 'CC', '102556235', 'Alejandro', 'arangoalejandro360@gmail.com', '3215474741', 'calle 5', 'Bogota'),
(12, 'CC', '6432132131', 'alejandro', 'arangoalejandro360@gmail.com', '32165546161', 'nose', 'bogota'),
(13, 'CC', '16131', 'alejandro', 'arangoalejandro360@gmail.com', '161616', 'nose', 'nose'),
(14, 'CC', '2131651', 'usagi', 'nose', '516516954', 'nose', 'nose');

-- --------------------------------------------------------

--
-- Estructura de tabla para la tabla `reserva`
--

CREATE TABLE `reserva` (
  `codigo_pnr` varchar(6) NOT NULL,
  `id_vuelo` int(11) DEFAULT NULL,
  `id_vuelo_regreso` int(11) DEFAULT NULL,
  `fecha_creacion` datetime NOT NULL DEFAULT current_timestamp(),
  `estado_reserva` varchar(20) NOT NULL DEFAULT 'Pendiente_Pago',
  `clase_viaje` varchar(20) NOT NULL,
  `tipo_viaje` varchar(20) DEFAULT 'Solo_Ida',
  `asientos` int(11) NOT NULL DEFAULT 1,
  `total` decimal(10,2) NOT NULL,
  `id_comprador` int(11) DEFAULT NULL,
  `token_confirmacion` varchar(6) DEFAULT NULL,
  `intentos_verificacion` int(11) NOT NULL DEFAULT 0,
  `codigo_enviado_en` datetime DEFAULT NULL,
  `bloqueado_hasta` datetime DEFAULT NULL,
  `motivo_bloqueo` varchar(150) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--
-- Volcado de datos para la tabla `reserva`
--

INSERT INTO `reserva` (`codigo_pnr`, `id_vuelo`, `id_vuelo_regreso`, `fecha_creacion`, `estado_reserva`, `clase_viaje`, `tipo_viaje`, `asientos`, `total`, `id_comprador`, `token_confirmacion`, `intentos_verificacion`, `codigo_enviado_en`, `bloqueado_hasta`, `motivo_bloqueo`) VALUES
('0SV8R2', 7, NULL, '2026-10-12 19:02:19', 'Expirada', 'Economica', 'Solo_Ida', 1, 620500.00, NULL, NULL, 0, NULL, NULL, NULL),
('175F0D', 7, NULL, '2026-10-14 17:57:11', 'Expirada', 'Economica', 'Solo_Ida', 1, 450000.00, NULL, NULL, 0, NULL, NULL, NULL),
('233437', 7, NULL, '2026-10-14 17:40:40', 'Expirada', 'Economica', 'Solo_Ida', 1, 450000.00, NULL, NULL, 0, NULL, NULL, NULL),
('280B59', 7, NULL, '2026-10-14 21:30:26', 'Expirada', 'Economica', 'Solo_Ida', 1, 450000.00, NULL, NULL, 0, NULL, NULL, NULL),
('2A9470', 8, NULL, '2026-10-16 13:39:38', 'Expirada', 'Economica', 'Solo_Ida', 1, 380000.00, NULL, NULL, 0, NULL, NULL, NULL),
('2EWZ5I', 7, NULL, '2026-10-12 19:13:33', 'Expirada', 'Económica', 'Solo_Ida', 1, 620500.00, NULL, NULL, 0, NULL, NULL, NULL),
('3FD7A0', 8, NULL, '2026-10-14 17:32:34', 'Confirmada', 'Economica', 'Solo_Ida', 1, 380000.00, NULL, NULL, 0, NULL, NULL, NULL),
('7ED07E', 11, NULL, '2026-10-16 18:57:37', 'Confirmada', 'Primera Clase', 'Solo_Ida', 1, 800000.00, NULL, NULL, 0, NULL, NULL, NULL),
('856FA1', 8, NULL, '2026-10-21 17:33:07', 'Confirmada', 'Economica', 'Solo_Ida', 3, 1045000.00, NULL, NULL, 0, NULL, NULL, NULL),
('AV5CFY', 11, NULL, '2026-11-07 18:59:01', 'Expirada', 'Economica', 'Solo_Ida', 1, 320000.00, NULL, '859360', 3, '2026-11-07 23:59:41', '2026-11-08 00:14:55', 'MAX_INTENTOS'),
('AV8DRM', 11, NULL, '2026-11-07 18:43:51', 'Expirada', 'Economica', 'Solo_Ida', 1, 320000.00, NULL, '698694', 3, '2026-11-07 23:44:23', '2026-11-07 23:59:42', 'MAX_INTENTOS'),
('AV9FQC', 8, NULL, '2026-10-21 19:01:06', 'Expirada', 'Economica', 'Solo_Ida', 1, 380000.00, NULL, '919073', 0, NULL, NULL, NULL),
('AVB2NW', 8, NULL, '2026-11-07 18:11:58', 'Expirada', 'Economica', 'Solo_Ida', 1, 380000.00, NULL, NULL, 0, NULL, NULL, NULL),
('AVB3YK', 8, NULL, '2026-11-07 18:22:04', 'Expirada', 'Economica', 'Solo_Ida', 1, 380000.00, NULL, '586139', 0, '2026-11-07 18:22:55', NULL, NULL),
('AVCAKY', 11, NULL, '2026-11-07 18:54:53', 'Expirada', 'Economica', 'Solo_Ida', 1, 352000.00, NULL, NULL, 0, NULL, NULL, NULL),
('AVCX3R', 8, NULL, '2026-10-21 19:23:02', 'Confirmada', 'Economica', 'Solo_Ida', 1, 380000.00, 11, '047253', 0, NULL, NULL, NULL),
('AVGBLH', 8, NULL, '2026-11-07 18:20:30', 'Expirada', 'Economica', 'Solo_Ida', 1, 380000.00, NULL, '685184', 0, '2026-11-07 18:21:01', NULL, NULL),
('AVPT6V', 8, NULL, '2026-11-07 17:56:52', 'Expirada', 'Economica', 'Solo_Ida', 1, 380000.00, NULL, NULL, 0, NULL, NULL, NULL),
('AVPXTD', 11, NULL, '2026-11-07 18:55:58', 'Cancelada', 'Economica', 'Solo_Ida', 1, 352000.00, 13, NULL, 0, NULL, NULL, NULL),
('AVSSA6', 8, NULL, '2026-10-21 19:17:28', 'Expirada', 'Economica', 'Solo_Ida', 1, 380000.00, NULL, NULL, 0, NULL, NULL, NULL),
('AVVS49', 8, NULL, '2026-10-21 19:08:17', 'Expirada', 'Economica', 'Solo_Ida', 1, 380000.00, NULL, NULL, 0, NULL, NULL, NULL),
('AVW725', 11, NULL, '2026-11-07 18:28:57', 'Cancelada', 'Economica', 'Solo_Ida', 1, 320000.00, 12, NULL, 0, NULL, NULL, NULL),
('AVWT8S', 11, NULL, '2026-11-07 20:31:58', 'Expirada', 'Economica', 'Solo_Ida', 1, 320000.00, NULL, '119766', 3, '2026-11-08 01:32:25', '2026-11-08 01:47:54', 'MAX_INTENTOS'),
('BDC675', 10, NULL, '2026-10-16 13:54:17', 'Confirmada', 'Economica', 'Solo_Ida', 1, 470000.00, NULL, NULL, 0, NULL, NULL, NULL),
('D8691B', 7, NULL, '2026-10-14 17:41:27', 'Expirada', 'Economica', 'Solo_Ida', 1, 450000.00, NULL, NULL, 0, NULL, NULL, NULL),
('F8EDF9', 10, NULL, '2026-10-16 13:57:42', 'Confirmada', 'Economica', 'Solo_Ida', 1, 470000.00, NULL, NULL, 0, NULL, NULL, NULL),
('ME09NL', 7, NULL, '2026-10-12 19:30:26', 'Expirada', 'Economica', 'Solo_Ida', 1, 537200.00, NULL, NULL, 0, NULL, NULL, NULL);

-- --------------------------------------------------------

--
-- Estructura de tabla para la tabla `reserva_pasajero`
--

CREATE TABLE `reserva_pasajero` (
  `codigo_pnr` varchar(6) NOT NULL,
  `id_pasajero` int(11) NOT NULL,
  `es_titular` tinyint(1) DEFAULT 0
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--
-- Volcado de datos para la tabla `reserva_pasajero`
--

INSERT INTO `reserva_pasajero` (`codigo_pnr`, `id_pasajero`, `es_titular`) VALUES
('0SV8R2', 1, 1),
('2EWZ5I', 2, 1),
('3FD7A0', 4, 1),
('7ED07E', 7, 1),
('856FA1', 8, 1),
('856FA1', 9, 0),
('856FA1', 10, 0),
('AVCX3R', 11, 1),
('AVPXTD', 13, 1),
('AVPXTD', 14, 0),
('AVW725', 12, 1),
('BDC675', 5, 1),
('F8EDF9', 6, 1),
('ME09NL', 3, 1);

-- --------------------------------------------------------

--
-- Estructura de tabla para la tabla `usuario`
--

CREATE TABLE `usuario` (
  `id_usuario` int(11) NOT NULL,
  `username` varchar(50) NOT NULL,
  `password` varchar(100) NOT NULL,
  `rol` varchar(20) NOT NULL,
  `email` varchar(100) NOT NULL,
  `intentos_fallidos` int(11) DEFAULT 0,
  `bloqueado_hasta` datetime DEFAULT NULL,
  `token_2fa` varchar(6) DEFAULT NULL,
  `token_enviado_en` datetime DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Volcado de datos para la tabla `usuario`
--

INSERT INTO `usuario` (`id_usuario`, `username`, `password`, `rol`, `email`, `intentos_fallidos`, `bloqueado_hasta`, `token_2fa`, `token_enviado_en`) VALUES
(1, 'admin', 'admin123', 'ADMIN', 'arangoalejandro360@gmail.com', 0, NULL, '873802', '2026-11-10 01:07:08');

-- --------------------------------------------------------

--
-- Estructura de tabla para la tabla `vuelo`
--

CREATE TABLE `vuelo` (
  `id_vuelo` int(11) NOT NULL,
  `numero_vuelo` varchar(10) NOT NULL,
  `origen_iata` varchar(3) NOT NULL,
  `destino_iata` varchar(3) NOT NULL,
  `id_aeronave` int(11) DEFAULT NULL,
  `fecha_salida` datetime NOT NULL,
  `fecha_llegada` datetime NOT NULL,
  `estado_vuelo` varchar(20) DEFAULT 'Programado',
  `escalas` int(11) NOT NULL DEFAULT 0,
  `tarifa_base` decimal(10,2) NOT NULL,
  `cupos_disponibles` int(11) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--
-- Volcado de datos para la tabla `vuelo`
--

INSERT INTO `vuelo` (`id_vuelo`, `numero_vuelo`, `origen_iata`, `destino_iata`, `id_aeronave`, `fecha_salida`, `fecha_llegada`, `estado_vuelo`, `escalas`, `tarifa_base`, `cupos_disponibles`) VALUES
(7, 'AV102', 'BOG', 'MDE', 1, '2026-10-15 06:00:00', '2026-10-15 07:15:00', 'Programado', 0, 450000.00, 13),
(8, 'AV204', 'BOG', 'MDE', 2, '2026-10-15 08:30:00', '2026-10-15 10:00:00', 'Programado', 0, 380000.00, 2),
(9, 'AV306', 'BOG', 'MDE', 3, '2026-10-15 12:00:00', '2026-10-15 13:20:00', 'Programado', 0, 520000.00, 4),
(10, 'AV205', 'MDE', 'BOG', 1, '2026-10-22 14:00:00', '2026-10-22 15:15:00', 'Programado', 0, 470000.00, 8),
(11, 'AV450', 'BOG', 'CLO', 2, '2026-10-16 09:00:00', '2026-10-16 10:00:00', 'Programado', 1, 320000.00, 14),
(12, 'AV670', 'BOG', 'CTG', 3, '2026-10-17 11:00:00', '2026-10-17 12:30:00', 'Programado', 2, 410000.00, 9),
(13, 'AV600', 'MDE', 'BOG', 1, '2026-10-22 05:00:00', '2026-10-22 06:15:00', 'Programado', 0, 380000.00, 15),
(14, 'AV601', 'MDE', 'BOG', 2, '2026-10-22 09:00:00', '2026-10-22 10:15:00', 'Programado', 0, 420000.00, 20),
(15, 'AV602', 'MDE', 'BOG', 3, '2026-10-22 14:30:00', '2026-10-22 15:45:00', 'Programado', 1, 350000.00, 25),
(16, 'AV603', 'MDE', 'BOG', 1, '2026-10-22 20:00:00', '2026-10-22 21:15:00', 'Programado', 0, 500000.00, 10);

--
-- Índices para tablas volcadas
--

--
-- Indices de la tabla `aeronave`
--
ALTER TABLE `aeronave`
  ADD PRIMARY KEY (`id_aeronave`),
  ADD UNIQUE KEY `matricula` (`matricula`);

--
-- Indices de la tabla `aeropuerto`
--
ALTER TABLE `aeropuerto`
  ADD PRIMARY KEY (`codigo_iata`);

--
-- Indices de la tabla `auditoria_eventos`
--
ALTER TABLE `auditoria_eventos`
  ADD PRIMARY KEY (`id_auditoria`),
  ADD KEY `idx_aud_pnr_fecha` (`codigo_pnr`,`fecha_evento`),
  ADD KEY `idx_aud_evento` (`evento`);

--
-- Indices de la tabla `intentos_pago`
--
ALTER TABLE `intentos_pago`
  ADD PRIMARY KEY (`id_intento`),
  ADD KEY `codigo_pnr` (`codigo_pnr`);

--
-- Indices de la tabla `pasajero`
--
ALTER TABLE `pasajero`
  ADD PRIMARY KEY (`id_pasajero`),
  ADD UNIQUE KEY `tipo_documento` (`tipo_documento`,`numero_documento`);

--
-- Indices de la tabla `reserva`
--
ALTER TABLE `reserva`
  ADD PRIMARY KEY (`codigo_pnr`),
  ADD KEY `fk_reserva_vuelo` (`id_vuelo`),
  ADD KEY `fk_reserva_vuelo_regreso` (`id_vuelo_regreso`),
  ADD KEY `fk_reserva_comprador` (`id_comprador`);

--
-- Indices de la tabla `reserva_pasajero`
--
ALTER TABLE `reserva_pasajero`
  ADD PRIMARY KEY (`codigo_pnr`,`id_pasajero`),
  ADD KEY `id_pasajero` (`id_pasajero`);

--
-- Indices de la tabla `usuario`
--
ALTER TABLE `usuario`
  ADD PRIMARY KEY (`id_usuario`),
  ADD UNIQUE KEY `username` (`username`);

--
-- Indices de la tabla `vuelo`
--
ALTER TABLE `vuelo`
  ADD PRIMARY KEY (`id_vuelo`),
  ADD KEY `origen_iata` (`origen_iata`),
  ADD KEY `destino_iata` (`destino_iata`),
  ADD KEY `id_aeronave` (`id_aeronave`);

--
-- AUTO_INCREMENT de las tablas volcadas
--

--
-- AUTO_INCREMENT de la tabla `aeronave`
--
ALTER TABLE `aeronave`
  MODIFY `id_aeronave` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=4;

--
-- AUTO_INCREMENT de la tabla `auditoria_eventos`
--
ALTER TABLE `auditoria_eventos`
  MODIFY `id_auditoria` bigint(20) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=56;

--
-- AUTO_INCREMENT de la tabla `intentos_pago`
--
ALTER TABLE `intentos_pago`
  MODIFY `id_intento` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=9;

--
-- AUTO_INCREMENT de la tabla `pasajero`
--
ALTER TABLE `pasajero`
  MODIFY `id_pasajero` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=15;

--
-- AUTO_INCREMENT de la tabla `usuario`
--
ALTER TABLE `usuario`
  MODIFY `id_usuario` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=2;

--
-- AUTO_INCREMENT de la tabla `vuelo`
--
ALTER TABLE `vuelo`
  MODIFY `id_vuelo` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=17;

--
-- Restricciones para tablas volcadas
--

--
-- Filtros para la tabla `intentos_pago`
--
ALTER TABLE `intentos_pago`
  ADD CONSTRAINT `intentos_pago_ibfk_1` FOREIGN KEY (`codigo_pnr`) REFERENCES `reserva` (`codigo_pnr`);

--
-- Filtros para la tabla `reserva`
--
ALTER TABLE `reserva`
  ADD CONSTRAINT `fk_reserva_comprador` FOREIGN KEY (`id_comprador`) REFERENCES `pasajero` (`id_pasajero`),
  ADD CONSTRAINT `fk_reserva_vuelo` FOREIGN KEY (`id_vuelo`) REFERENCES `vuelo` (`id_vuelo`),
  ADD CONSTRAINT `fk_reserva_vuelo_regreso` FOREIGN KEY (`id_vuelo_regreso`) REFERENCES `vuelo` (`id_vuelo`);

--
-- Filtros para la tabla `reserva_pasajero`
--
ALTER TABLE `reserva_pasajero`
  ADD CONSTRAINT `reserva_pasajero_ibfk_1` FOREIGN KEY (`codigo_pnr`) REFERENCES `reserva` (`codigo_pnr`),
  ADD CONSTRAINT `reserva_pasajero_ibfk_2` FOREIGN KEY (`id_pasajero`) REFERENCES `pasajero` (`id_pasajero`);

--
-- Filtros para la tabla `vuelo`
--
ALTER TABLE `vuelo`
  ADD CONSTRAINT `vuelo_ibfk_1` FOREIGN KEY (`origen_iata`) REFERENCES `aeropuerto` (`codigo_iata`),
  ADD CONSTRAINT `vuelo_ibfk_2` FOREIGN KEY (`destino_iata`) REFERENCES `aeropuerto` (`codigo_iata`),
  ADD CONSTRAINT `vuelo_ibfk_3` FOREIGN KEY (`id_aeronave`) REFERENCES `aeronave` (`id_aeronave`);
COMMIT;

/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
