SET ANSI_NULLS ON;
SET ANSI_PADDING ON;
SET ANSI_WARNINGS ON;
SET ARITHABORT ON;
SET CONCAT_NULL_YIELDS_NULL ON;
SET QUOTED_IDENTIFIER ON;
SET NUMERIC_ROUNDABORT OFF;
SET XACT_ABORT ON;
IF OBJECT_ID(N'dbo.Usuarios', N'U') IS NOT NULL
    THROW 50001, 'La estructura ya existe. Este script no elimina ni reemplaza datos.', 1;
BEGIN TRANSACTION;
CREATE TABLE rol (
 rol_id INT PRIMARY KEY, nombre_rol NVARCHAR(30) NOT NULL UNIQUE, descripcion NVARCHAR(150) NOT NULL,
 fechaCreacion DATETIME2 NOT NULL DEFAULT CONVERT(datetime2, SYSDATETIMEOFFSET() AT TIME ZONE 'SA Pacific Standard Time'), fechaActualizacion DATETIME2 NULL,
 usuarioRegistro NVARCHAR(50) NOT NULL DEFAULT N'sistema', estado VARCHAR(15) NOT NULL DEFAULT 'Activo');
CREATE TABLE permiso (
 permiso_id INT PRIMARY KEY, nombre_permiso VARCHAR(40) NOT NULL UNIQUE, descripcion NVARCHAR(150) NOT NULL,
 fechaCreacion DATETIME2 NOT NULL DEFAULT CONVERT(datetime2, SYSDATETIMEOFFSET() AT TIME ZONE 'SA Pacific Standard Time'), fechaActualizacion DATETIME2 NULL,
 usuarioRegistro NVARCHAR(50) NOT NULL DEFAULT N'sistema', estado VARCHAR(15) NOT NULL DEFAULT 'Activo');
CREATE TABLE Roles_Permisos (
 rol_id INT NOT NULL REFERENCES rol, permiso_id INT NOT NULL REFERENCES permiso,
 PRIMARY KEY (rol_id, permiso_id));
CREATE TABLE Usuarios (
 idUsuario INT IDENTITY PRIMARY KEY, nombreUsuario VARCHAR(50) NOT NULL UNIQUE,
 [contraseña] VARCHAR(255) NOT NULL, rol_id INT NOT NULL REFERENCES rol,
 permiso_id INT NOT NULL DEFAULT 1 REFERENCES permiso, ultimoAcceso DATETIME2 NULL,
 intentosFallidos INT NOT NULL DEFAULT 0 CHECK(intentosFallidos BETWEEN 0 AND 3),
 bloqueado BIT NOT NULL DEFAULT 0, tokenRecuperacion VARCHAR(255) NULL,
 fechaCreacion DATETIME2 NOT NULL DEFAULT CONVERT(datetime2, SYSDATETIMEOFFSET() AT TIME ZONE 'SA Pacific Standard Time'), fechaActualizacion DATETIME2 NULL,
 usuarioRegistro NVARCHAR(50) NOT NULL, estado VARCHAR(15) NOT NULL DEFAULT 'Activo' CHECK(estado IN ('Activo','Inactivo')));
CREATE TABLE Sexo (
 idSexo INT PRIMARY KEY, descripcion NVARCHAR(20) NOT NULL UNIQUE,
 fechaCreacion DATETIME2 NOT NULL DEFAULT CONVERT(datetime2, SYSDATETIMEOFFSET() AT TIME ZONE 'SA Pacific Standard Time'), fechaActualizacion DATETIME2 NULL,
 usuarioRegistro NVARCHAR(50) NOT NULL DEFAULT N'sistema', estado VARCHAR(15) NOT NULL DEFAULT 'Activo');
CREATE TABLE EstadoCivil (
 idEstadoCivil INT PRIMARY KEY, descripcion NVARCHAR(20) NOT NULL UNIQUE,
 fechaCreacion DATETIME2 NOT NULL DEFAULT CONVERT(datetime2, SYSDATETIMEOFFSET() AT TIME ZONE 'SA Pacific Standard Time'), fechaActualizacion DATETIME2 NULL,
 usuarioRegistro NVARCHAR(50) NOT NULL DEFAULT N'sistema', estado VARCHAR(15) NOT NULL DEFAULT 'Activo');
CREATE TABLE DatosPersonales (
 idDatosPersonales INT IDENTITY PRIMARY KEY, idUsuario INT NULL REFERENCES Usuarios,
 nombres NVARCHAR(80) NOT NULL, apellidos NVARCHAR(80) NOT NULL, dni VARCHAR(15) NOT NULL UNIQUE,
 idSexo INT NULL REFERENCES Sexo, idEstadoCivil INT NULL REFERENCES EstadoCivil, fechaNacimiento DATE NULL,
 fechaCreacion DATETIME2 NOT NULL DEFAULT CONVERT(datetime2, SYSDATETIMEOFFSET() AT TIME ZONE 'SA Pacific Standard Time'), fechaActualizacion DATETIME2 NULL,
 usuarioRegistro NVARCHAR(50) NOT NULL, estado VARCHAR(15) NOT NULL DEFAULT 'Activo');
CREATE UNIQUE INDEX UX_DatosPersonales_Usuario ON DatosPersonales(idUsuario) WHERE idUsuario IS NOT NULL;
CREATE TABLE CorreoElectronico (
 idCorreo INT IDENTITY PRIMARY KEY, idDatosPersonales INT NOT NULL REFERENCES DatosPersonales,
 correoElectronico VARCHAR(80) NOT NULL, tipoCorreo NVARCHAR(20) NOT NULL DEFAULT N'Personal',
 fechaCreacion DATETIME2 NOT NULL DEFAULT CONVERT(datetime2, SYSDATETIMEOFFSET() AT TIME ZONE 'SA Pacific Standard Time'), fechaActualizacion DATETIME2 NULL,
 usuarioRegistro NVARCHAR(50) NOT NULL, estado VARCHAR(15) NOT NULL DEFAULT 'Activo');
CREATE TABLE Telefono (
 idTelefono INT IDENTITY PRIMARY KEY, idDatosPersonales INT NOT NULL REFERENCES DatosPersonales,
 numeroTelefono VARCHAR(20) NOT NULL, tipoTelefono NVARCHAR(20) NOT NULL DEFAULT N'Móvil',
 fechaCreacion DATETIME2 NOT NULL DEFAULT CONVERT(datetime2, SYSDATETIMEOFFSET() AT TIME ZONE 'SA Pacific Standard Time'), fechaActualizacion DATETIME2 NULL,
 usuarioRegistro NVARCHAR(50) NOT NULL, estado VARCHAR(15) NOT NULL DEFAULT 'Activo');
CREATE TABLE Pais (
 idPais INT PRIMARY KEY, nombrePais NVARCHAR(100) NOT NULL UNIQUE,
 fechaCreacion DATETIME2 NOT NULL DEFAULT CONVERT(datetime2, SYSDATETIMEOFFSET() AT TIME ZONE 'SA Pacific Standard Time'), fechaActualizacion DATETIME2 NULL,
 usuarioRegistro NVARCHAR(50) NOT NULL DEFAULT N'sistema', estado VARCHAR(15) NOT NULL DEFAULT 'Activo');
CREATE TABLE Departamento (
 idDepartamento INT PRIMARY KEY, nombreDepartamento NVARCHAR(100) NOT NULL, idPais INT NOT NULL REFERENCES Pais,
 fechaCreacion DATETIME2 NOT NULL DEFAULT CONVERT(datetime2, SYSDATETIMEOFFSET() AT TIME ZONE 'SA Pacific Standard Time'), fechaActualizacion DATETIME2 NULL,
 usuarioRegistro NVARCHAR(50) NOT NULL DEFAULT N'sistema', estado VARCHAR(15) NOT NULL DEFAULT 'Activo');
CREATE TABLE Provincia (
 idProvincia INT PRIMARY KEY, nombreProvincia NVARCHAR(100) NOT NULL, idDepartamento INT NOT NULL REFERENCES Departamento,
 fechaCreacion DATETIME2 NOT NULL DEFAULT CONVERT(datetime2, SYSDATETIMEOFFSET() AT TIME ZONE 'SA Pacific Standard Time'), fechaActualizacion DATETIME2 NULL,
 usuarioRegistro NVARCHAR(50) NOT NULL DEFAULT N'sistema', estado VARCHAR(15) NOT NULL DEFAULT 'Activo');
CREATE TABLE Distrito (
 idDistrito INT PRIMARY KEY, nombreDistrito NVARCHAR(100) NOT NULL, idProvincia INT NOT NULL REFERENCES Provincia,
 fechaCreacion DATETIME2 NOT NULL DEFAULT CONVERT(datetime2, SYSDATETIMEOFFSET() AT TIME ZONE 'SA Pacific Standard Time'), fechaActualizacion DATETIME2 NULL,
 usuarioRegistro NVARCHAR(50) NOT NULL DEFAULT N'sistema', estado VARCHAR(15) NOT NULL DEFAULT 'Activo');
CREATE TABLE Direccion (
 idDireccion INT IDENTITY PRIMARY KEY, idDatosPersonales INT NOT NULL REFERENCES DatosPersonales,
 idDistrito INT NOT NULL REFERENCES Distrito, calleAvenida NVARCHAR(150) NOT NULL,
 tipoDireccion NVARCHAR(20) NOT NULL DEFAULT N'Domicilio', piso NVARCHAR(10) NULL, numero NVARCHAR(10) NULL, referencia NVARCHAR(255) NULL,
 fechaCreacion DATETIME2 NOT NULL DEFAULT CONVERT(datetime2, SYSDATETIMEOFFSET() AT TIME ZONE 'SA Pacific Standard Time'), fechaActualizacion DATETIME2 NULL,
 usuarioRegistro NVARCHAR(50) NOT NULL, estado VARCHAR(15) NOT NULL DEFAULT 'Activo');
CREATE TABLE Pacientes (
 idPaciente INT IDENTITY PRIMARY KEY, idDatosPersonales INT NOT NULL UNIQUE REFERENCES DatosPersonales,
 numeroSeguro VARCHAR(30) NOT NULL UNIQUE, estado VARCHAR(15) NOT NULL DEFAULT 'Activo',
 fechaCreacion DATETIME2 NOT NULL DEFAULT CONVERT(datetime2, SYSDATETIMEOFFSET() AT TIME ZONE 'SA Pacific Standard Time'), fechaActualizacion DATETIME2 NULL, usuarioRegistro NVARCHAR(50) NOT NULL);
CREATE TABLE Especialidades (
 idEspecialidad INT IDENTITY PRIMARY KEY, nombre NVARCHAR(100) NOT NULL UNIQUE, descripcion NVARCHAR(255) NULL,
 estado VARCHAR(15) NOT NULL DEFAULT 'Activo', fechaCreacion DATETIME2 NOT NULL DEFAULT CONVERT(datetime2, SYSDATETIMEOFFSET() AT TIME ZONE 'SA Pacific Standard Time'),
 fechaActualizacion DATETIME2 NULL, usuarioRegistro NVARCHAR(50) NOT NULL);
CREATE TABLE Establecimientos (
 idEstablecimiento INT IDENTITY PRIMARY KEY, nombre NVARCHAR(100) NOT NULL UNIQUE, direccion NVARCHAR(180) NOT NULL,
 estado VARCHAR(15) NOT NULL DEFAULT 'Activo', fechaCreacion DATETIME2 NOT NULL DEFAULT CONVERT(datetime2, SYSDATETIMEOFFSET() AT TIME ZONE 'SA Pacific Standard Time'),
 fechaActualizacion DATETIME2 NULL, usuarioRegistro NVARCHAR(50) NOT NULL);
CREATE TABLE Medicos (
 idMedico INT IDENTITY PRIMARY KEY, idDatosPersonales INT NOT NULL UNIQUE REFERENCES DatosPersonales,
 cmp VARCHAR(15) NOT NULL UNIQUE, idEspecialidad INT NOT NULL REFERENCES Especialidades,
 idEstablecimiento INT NOT NULL REFERENCES Establecimientos, estado VARCHAR(15) NOT NULL DEFAULT 'Activo',
 fechaCreacion DATETIME2 NOT NULL DEFAULT CONVERT(datetime2, SYSDATETIMEOFFSET() AT TIME ZONE 'SA Pacific Standard Time'), fechaActualizacion DATETIME2 NULL, usuarioRegistro NVARCHAR(50) NOT NULL);
CREATE TABLE Horarios (
 idHorario INT IDENTITY PRIMARY KEY, idMedico INT NOT NULL REFERENCES Medicos,
 inicio DATETIME2 NOT NULL, fin DATETIME2 NOT NULL, estado VARCHAR(15) NOT NULL DEFAULT 'Activo',
 fechaCreacion DATETIME2 NOT NULL DEFAULT CONVERT(datetime2, SYSDATETIMEOFFSET() AT TIME ZONE 'SA Pacific Standard Time'), fechaActualizacion DATETIME2 NULL, usuarioRegistro NVARCHAR(50) NOT NULL,
 CHECK(fin>inicio), UNIQUE(idMedico,inicio));
CREATE TABLE Citas (
 idCita INT IDENTITY PRIMARY KEY, codigo AS ('CIT-'+CONVERT(VARCHAR(12),idCita)) PERSISTED,
 idPaciente INT NOT NULL REFERENCES Pacientes, idHorario INT NOT NULL REFERENCES Horarios,
 estado VARCHAR(15) NOT NULL DEFAULT 'RESERVADA' CHECK(estado IN ('RESERVADA','CANCELADA','ATENDIDA')),
 motivo NVARCHAR(200) NOT NULL, fechaCreacion DATETIME2 NOT NULL DEFAULT CONVERT(datetime2, SYSDATETIMEOFFSET() AT TIME ZONE 'SA Pacific Standard Time'),
 fechaActualizacion DATETIME2 NULL, usuarioRegistro NVARCHAR(50) NOT NULL);
CREATE UNIQUE INDEX UX_Citas_Cupo ON Citas(idHorario) WHERE estado='RESERVADA';
CREATE INDEX IX_Citas_Paciente ON Citas(idPaciente,estado);
CREATE TABLE SolicitudCita (
 idSolicitud INT IDENTITY PRIMARY KEY, codigo AS ('SOL-'+CONVERT(VARCHAR(12),idSolicitud)) PERSISTED,
 idPaciente INT NOT NULL REFERENCES Pacientes, fechaSolicitud DATETIME2 NOT NULL DEFAULT CONVERT(datetime2, SYSDATETIMEOFFSET() AT TIME ZONE 'SA Pacific Standard Time'),
 usuarioRegistro NVARCHAR(50) NOT NULL);
CREATE TABLE DetalleSolicitud (
 idDetalle INT IDENTITY PRIMARY KEY, idSolicitud INT NOT NULL REFERENCES SolicitudCita,
 idCita INT NOT NULL REFERENCES Citas, numero INT NOT NULL,
 UNIQUE(idSolicitud,idCita), UNIQUE(idSolicitud,numero));
CREATE TABLE Recordatorios (
 idRecordatorio INT IDENTITY PRIMARY KEY, idCita INT NOT NULL REFERENCES Citas,
 canal VARCHAR(15) NOT NULL, mensaje NVARCHAR(400) NOT NULL, estado VARCHAR(20) NOT NULL DEFAULT 'SIMULADO',
 fechaCreacion DATETIME2 NOT NULL DEFAULT CONVERT(datetime2, SYSDATETIMEOFFSET() AT TIME ZONE 'SA Pacific Standard Time'));
CREATE TABLE Historial_Login (
 idHistorial BIGINT IDENTITY PRIMARY KEY, idUsuario INT NULL REFERENCES Usuarios,
 nombreUsuario NVARCHAR(50) NOT NULL, resultado VARCHAR(25) NOT NULL,
 fecha DATETIME2 NOT NULL DEFAULT CONVERT(datetime2, SYSDATETIMEOFFSET() AT TIME ZONE 'SA Pacific Standard Time'));
CREATE TABLE Log_Auditoria (
 idAuditoria BIGINT IDENTITY PRIMARY KEY, idUsuario INT NULL REFERENCES Usuarios,
 modulo VARCHAR(30) NOT NULL, accion VARCHAR(40) NOT NULL, detalle NVARCHAR(400) NOT NULL,
 fecha DATETIME2 NOT NULL DEFAULT CONVERT(datetime2, SYSDATETIMEOFFSET() AT TIME ZONE 'SA Pacific Standard Time'));
CREATE TABLE Historial_Backup (
 idBackup INT IDENTITY PRIMARY KEY, fecha DATETIME2 NOT NULL DEFAULT CONVERT(datetime2, SYSDATETIMEOFFSET() AT TIME ZONE 'SA Pacific Standard Time'),
 archivo NVARCHAR(255) NOT NULL, estado VARCHAR(20) NOT NULL, usuarioRegistro NVARCHAR(50) NOT NULL);
INSERT rol(rol_id,nombre_rol,descripcion) VALUES (1,N'Administrador',N'Administra cuentas y catálogos'),(2,N'Operador',N'Gestiona pacientes y citas'),(3,N'Médico',N'Consulta su agenda'),(4,N'Paciente',N'Gestiona sus propias citas');
INSERT permiso(permiso_id,nombre_permiso,descripcion) VALUES
 (1,'inicio',N'Ver el inicio'),(2,'usuarios',N'Gestionar usuarios'),(3,'pacientes',N'Gestionar pacientes'),
 (4,'medicos',N'Gestionar médicos'),(5,'especialidades',N'Gestionar especialidades'),(6,'establecimientos',N'Gestionar establecimientos'),
 (7,'horarios',N'Gestionar horarios'),(8,'citas',N'Gestionar citas'),(9,'auditoria',N'Consultar auditoría'),(10,'perfil',N'Actualizar datos propios'),(11,'constancia',N'Consultar constancias');
INSERT Roles_Permisos SELECT 1,permiso_id FROM permiso;
INSERT Roles_Permisos VALUES (2,1),(2,3),(2,8),(2,10),(2,11),(3,1),(3,8),(3,10),(3,11),(4,1),(4,8),(4,10),(4,11);
INSERT Sexo VALUES(1,N'Masculino',CONVERT(datetime2, SYSDATETIMEOFFSET() AT TIME ZONE 'SA Pacific Standard Time'),NULL,N'sistema','Activo'),(2,N'Femenino',CONVERT(datetime2, SYSDATETIMEOFFSET() AT TIME ZONE 'SA Pacific Standard Time'),NULL,N'sistema','Activo'),(3,N'Sin indicar',CONVERT(datetime2, SYSDATETIMEOFFSET() AT TIME ZONE 'SA Pacific Standard Time'),NULL,N'sistema','Activo');
INSERT EstadoCivil(idEstadoCivil,descripcion) VALUES(1,N'Soltero/a'),(2,N'Casado/a'),(3,N'Otro');
INSERT Pais(idPais,nombrePais) VALUES(1,N'Perú');
INSERT Departamento(idDepartamento,nombreDepartamento,idPais) VALUES(1,N'Lima',1);
INSERT Provincia(idProvincia,nombreProvincia,idDepartamento) VALUES(1,N'Lima',1);
INSERT Distrito(idDistrito,nombreDistrito,idProvincia) VALUES(1,N'Jesús María',1),(2,N'Miraflores',1),(3,N'San Isidro',1);
INSERT Especialidades(nombre,descripcion,usuarioRegistro) VALUES(N'Medicina general',N'Consulta médica general',N'sistema'),(N'Cardiología',N'Consulta de cardiología',N'sistema'),(N'Pediatría',N'Consulta pediátrica',N'sistema');
INSERT Establecimientos(nombre,direccion,usuarioRegistro) VALUES(N'Centro Médico de Atención',N'Av. de la Salud 100, Lima',N'sistema');
COMMIT;
