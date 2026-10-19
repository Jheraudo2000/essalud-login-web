package essalud.repositorio;

import essalud.configuracion.ConexionBD;
import essalud.fabrica.RecordatorioFactory;
import essalud.modelo.*;
import essalud.util.Sql;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.*;

public final class CitaRepositoryJdbc implements CitaRepository {
    private final AuditoriaRepository auditoria=new AuditoriaRepository();
    private static final String BASE="SELECT c.idCita,c.codigo,c.idPaciente,c.idHorario,c.estado,c.motivo,h.inicio,h.fin,CONCAT(p.nombres,N' ',p.apellidos) paciente,CONCAT(d.nombres,N' ',d.apellidos) medico,e.nombre especialidad,s.nombre establecimiento,(SELECT TOP 1 idSolicitud FROM DetalleSolicitud WHERE idCita=c.idCita ORDER BY idDetalle) idSolicitud FROM Citas c JOIN Pacientes a ON a.idPaciente=c.idPaciente JOIN DatosPersonales p ON p.idDatosPersonales=a.idDatosPersonales JOIN Horarios h ON h.idHorario=c.idHorario JOIN Medicos m ON m.idMedico=h.idMedico JOIN DatosPersonales d ON d.idDatosPersonales=m.idDatosPersonales JOIN Especialidades e ON e.idEspecialidad=m.idEspecialidad JOIN Establecimientos s ON s.idEstablecimiento=m.idEstablecimiento";
    public int pacientePropio(Usuario u) throws SQLException {
        return Sql.entero(Sql.primero(Sql.consultar("SELECT a.idPaciente FROM Pacientes a JOIN DatosPersonales p ON p.idDatosPersonales=a.idDatosPersonales WHERE p.idUsuario=? AND a.estado='Activo'",u.getIdUsuario())).get("idPaciente"));
    }
    private void propietario(Connection c,int paciente,Usuario u) throws SQLException {
        if(u.getRolId()==4) {
            if(Sql.consultar(c,"SELECT a.idPaciente FROM Pacientes a JOIN DatosPersonales p ON p.idDatosPersonales=a.idDatosPersonales WHERE a.idPaciente=? AND p.idUsuario=?",paciente,u.getIdUsuario()).isEmpty()) throw new SecurityException("Solo puedes gestionar tus propias citas.");
        } else if(u.getRolId()!=1&&u.getRolId()!=2) throw new SecurityException("Tu perfil no puede reservar o cambiar citas.");
    }
    private void bloquearPaciente(Connection c,int paciente) throws SQLException {
        if(Sql.consultar(c,"SELECT idPaciente FROM Pacientes WITH(UPDLOCK,HOLDLOCK) WHERE idPaciente=? AND estado='Activo'",paciente).isEmpty()) throw new IllegalArgumentException("El paciente no está activo.");
    }
    private Map<String,Object> cupo(Connection c,int horario) throws SQLException {
        Map<String,Object> h=Sql.primero(Sql.consultar(c,"SELECT h.inicio,h.fin,h.idMedico FROM Horarios h WITH(UPDLOCK,HOLDLOCK) JOIN Medicos m ON m.idMedico=h.idMedico JOIN DatosPersonales p ON p.idDatosPersonales=m.idDatosPersonales JOIN Usuarios u ON u.idUsuario=p.idUsuario JOIN Especialidades e ON e.idEspecialidad=m.idEspecialidad JOIN Establecimientos s ON s.idEstablecimiento=m.idEstablecimiento WHERE h.idHorario=? AND h.estado='Activo' AND m.estado='Activo' AND u.estado='Activo' AND e.estado='Activo' AND s.estado='Activo' AND h.inicio>CONVERT(datetime2, SYSDATETIMEOFFSET() AT TIME ZONE 'SA Pacific Standard Time')",horario));
        if(!Sql.consultar(c,"SELECT idCita FROM Citas WHERE idHorario=? AND estado IN ('RESERVADA','ATENDIDA')",horario).isEmpty()) throw new IllegalArgumentException("El horario seleccionado ya fue reservado. Elige otro cupo.");
        return h;
    }
    private void sinCruce(Connection c,int paciente,int excluir,Map<String,Object> h) throws SQLException {
        if(!Sql.consultar(c,"SELECT c.idCita FROM Citas c JOIN Horarios h ON h.idHorario=c.idHorario WHERE c.idPaciente=? AND c.idCita<>? AND c.estado='RESERVADA' AND h.inicio<? AND h.fin>?",paciente,excluir,h.get("fin"),h.get("inicio")).isEmpty()) throw new IllegalArgumentException("El paciente ya tiene una cita que se cruza con ese horario.");
    }
    private void recordar(Connection c,int cita,Map<String,Object> horario) throws SQLException {
        Sql.ejecutar(c,"UPDATE Recordatorios SET estado='REEMPLAZADO' WHERE idCita=? AND estado='SIMULADO'",cita);
        String texto=RecordatorioFactory.portal().crearRecordatorio("CIT-"+cita,Sql.texto(horario.get("inicio")));
        Sql.insertar(c,"INSERT Recordatorios(idCita,canal,mensaje) VALUES(?,'PORTAL',?)",cita,texto);
    }
    @Override public int reservar(int paciente,List<Integer> horarios,String motivo,Usuario autor) throws SQLException {
        SolicitudCita pedido=new SolicitudCita(paciente,horarios,motivo);
        try(Connection c=ConexionBD.getInstancia().getConexion()) {
            c.setTransactionIsolation(Connection.TRANSACTION_SERIALIZABLE);c.setAutoCommit(false);
            try {
                propietario(c,paciente,autor);bloquearPaciente(c,paciente);
                int solicitud=Sql.insertar(c,"INSERT SolicitudCita(idPaciente,usuarioRegistro) VALUES(?,?)",paciente,autor.getNombreUsuario());int numero=0;
                for(Cita detalle:pedido.getDetalle()) {
                    int id=detalle.getIdHorario();
                    Map<String,Object> horario=cupo(c,id);sinCruce(c,paciente,0,horario);
                    int cita=Sql.insertar(c,"INSERT Citas(idPaciente,idHorario,motivo,usuarioRegistro) VALUES(?,?,?,?)",detalle.getIdPaciente(),id,detalle.getMotivo(),autor.getNombreUsuario());
                    Sql.insertar(c,"INSERT DetalleSolicitud(idSolicitud,idCita,numero) VALUES(?,?,?)",solicitud,cita,++numero);
                    recordar(c,cita,horario);
                    auditoria.registrar(c,autor.getIdUsuario(),"citas","RESERVAR","CIT-"+cita+" / SOL-"+solicitud);
                }
                c.commit();return solicitud;
            }catch(SQLException|RuntimeException e){c.rollback();throw e;}
        }
    }
    @Override public void modificar(int cita,int horario,String accion,Usuario autor) throws SQLException {
        try(Connection c=ConexionBD.getInstancia().getConexion()) {
            c.setTransactionIsolation(Connection.TRANSACTION_SERIALIZABLE);c.setAutoCommit(false);
            try {
                Map<String,Object> inicial=Sql.primero(Sql.consultar(c,"SELECT idPaciente FROM Citas WHERE idCita=?",cita));
                int paciente=Sql.entero(inicial.get("idPaciente"));bloquearPaciente(c,paciente);
                Map<String,Object> actual=Sql.primero(Sql.consultar(c,"SELECT c.*,h.inicio,h.idMedico FROM Citas c WITH(UPDLOCK,HOLDLOCK) JOIN Horarios h ON h.idHorario=c.idHorario WHERE idCita=?",cita));
                EstadoCita.de(Sql.texto(actual.get("estado"))).validar(accion);
                if(accion.equals("atender")) {
                    if(autor.getRolId()!=1&&autor.getRolId()!=3) throw new SecurityException("Solo el médico o el administrador registra la atención.");
                    if(autor.getRolId()==3&&Sql.consultar(c,"SELECT m.idMedico FROM Medicos m JOIN DatosPersonales p ON p.idDatosPersonales=m.idDatosPersonales WHERE m.idMedico=? AND p.idUsuario=?",actual.get("idMedico"),autor.getIdUsuario()).isEmpty()) throw new SecurityException("La cita no pertenece a tu agenda.");
                    if(((Timestamp)actual.get("inicio")).toLocalDateTime().isAfter(LocalDateTime.now())) throw new IllegalArgumentException("La atención se registra a partir del inicio de la cita.");
                    Sql.ejecutar(c,"UPDATE Citas SET estado='ATENDIDA',fechaActualizacion=CONVERT(datetime2, SYSDATETIMEOFFSET() AT TIME ZONE 'SA Pacific Standard Time') WHERE idCita=?",cita);
                    Sql.ejecutar(c,"UPDATE Recordatorios SET estado='CONCLUIDO' WHERE idCita=? AND estado='SIMULADO'",cita);
                } else {
                    propietario(c,paciente,autor);
                    if(!((Timestamp)actual.get("inicio")).toLocalDateTime().isAfter(LocalDateTime.now())) throw new IllegalArgumentException("La cita ya comenzó y no admite cancelación ni reprogramación.");
                    if(accion.equals("cancelar")) {
                        Sql.ejecutar(c,"UPDATE Citas SET estado='CANCELADA',fechaActualizacion=CONVERT(datetime2, SYSDATETIMEOFFSET() AT TIME ZONE 'SA Pacific Standard Time') WHERE idCita=?",cita);
                        Sql.ejecutar(c,"UPDATE Recordatorios SET estado='CANCELADO' WHERE idCita=? AND estado='SIMULADO'",cita);
                    } else {
                        Map<String,Object> nuevo=cupo(c,horario);
                        sinCruce(c,paciente,cita,nuevo);
                        Sql.ejecutar(c,"UPDATE Citas SET idHorario=?,fechaActualizacion=CONVERT(datetime2, SYSDATETIMEOFFSET() AT TIME ZONE 'SA Pacific Standard Time') WHERE idCita=?",horario,cita);recordar(c,cita,nuevo);
                    }
                }
                auditoria.registrar(c,autor.getIdUsuario(),"citas",accion.toUpperCase(Locale.ROOT),"CIT-"+cita);c.commit();
            }catch(SQLException|RuntimeException e){c.rollback();throw e;}
        }
    }
    @Override public List<Map<String,Object>> listar(Usuario u) throws SQLException {
        if(u.getRolId()==4) return Sql.consultar(BASE+" WHERE p.idUsuario=? ORDER BY h.inicio DESC",u.getIdUsuario());
        if(u.getRolId()==3) return Sql.consultar(BASE+" WHERE d.idUsuario=? ORDER BY h.inicio DESC",u.getIdUsuario());
        return Sql.consultar(BASE+" ORDER BY h.inicio DESC");
    }
    public List<Map<String,Object>> recordatorios(Usuario u) throws SQLException {
        if(u.getRolId()!=4)return Collections.emptyList();
        return Sql.consultar("SELECT r.mensaje,r.estado FROM Recordatorios r JOIN Citas c ON c.idCita=r.idCita JOIN Pacientes p ON p.idPaciente=c.idPaciente JOIN DatosPersonales d ON d.idDatosPersonales=p.idDatosPersonales WHERE d.idUsuario=? AND r.estado='SIMULADO' ORDER BY r.idRecordatorio DESC",u.getIdUsuario());
    }
    @Override public Map<String,Object> constancia(int id,Usuario u) throws SQLException {
        Map<String,Object> cabecera=Sql.primero(Sql.consultar("SELECT s.idSolicitud,s.codigo,s.fechaSolicitud,CONCAT(p.nombres,N' ',p.apellidos) paciente,p.idUsuario FROM SolicitudCita s JOIN Pacientes a ON a.idPaciente=s.idPaciente JOIN DatosPersonales p ON p.idDatosPersonales=a.idDatosPersonales WHERE s.idSolicitud=?",id));
        if(u.getRolId()==4&&Sql.entero(cabecera.get("idUsuario"))!=u.getIdUsuario()) throw new SecurityException("La constancia no pertenece a tu cuenta.");
        String condicion=u.getRolId()==3?" AND d.idUsuario=?":"";
        List<Map<String,Object>> detalle=u.getRolId()==3?Sql.consultar(BASE+" WHERE c.idCita IN(SELECT idCita FROM DetalleSolicitud WHERE idSolicitud=?)"+condicion+" ORDER BY h.inicio",id,u.getIdUsuario()):Sql.consultar(BASE+" WHERE c.idCita IN(SELECT idCita FROM DetalleSolicitud WHERE idSolicitud=?) ORDER BY h.inicio",id);
        if(detalle.isEmpty()) throw new SecurityException("No tienes acceso a esta constancia.");
        cabecera.put("detalle",detalle);return cabecera;
    }
}
