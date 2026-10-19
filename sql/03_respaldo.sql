-- Ejecutar con una cuenta autorizada para respaldar esta base en SQL Server.
USE EsSaludCitasSemana10;
DECLARE @carpeta NVARCHAR(200)=CONVERT(NVARCHAR(200),SERVERPROPERTY('InstanceDefaultBackupPath'));
IF @carpeta IS NULL THROW 50002,'Configura una carpeta de respaldo accesible al servicio SQL Server.',1;
IF RIGHT(@carpeta,1) NOT IN(N'\',N'/') SET @carpeta+=CASE WHEN CHARINDEX(N'/',@carpeta)>0 THEN N'/' ELSE N'\' END;
DECLARE @destino NVARCHAR(255)=@carpeta+N'EsSaludCitasSemana10_'+REPLACE(REPLACE(REPLACE(CONVERT(NVARCHAR(30),SYSDATETIME(),126),':',''),'-',''),'.','')+N'.bak';
BEGIN TRY
 BACKUP DATABASE EsSaludCitasSemana10 TO DISK=@destino WITH COPY_ONLY, CHECKSUM;
 RESTORE VERIFYONLY FROM DISK=@destino WITH CHECKSUM;
 INSERT Historial_Backup(archivo,estado,usuarioRegistro) VALUES(@destino,'VERIFICADO',SUSER_SNAME());
 SELECT @destino archivoRespaldo;
END TRY
BEGIN CATCH
 INSERT Historial_Backup(archivo,estado,usuarioRegistro) VALUES(@destino,'FALLIDO',SUSER_SNAME());
 THROW;
END CATCH;
-- Recuperación: usar Restaurar base de datos en SSMS y elegir un nombre NUEVO.
-- El respaldo se conserva intacto y no se usa WITH REPLACE sobre una base existente.
