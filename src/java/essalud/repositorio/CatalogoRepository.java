package essalud.repositorio;

import essalud.configuracion.ConexionBD;
import essalud.modelo.*;
import essalud.util.*;
import java.sql.*;
import java.util.*;

/** Repository de los mantenimientos clínicos. Los módulos y columnas admitidos están cerrados. */
public final class CatalogoRepository {
    public static final Set<String> MODULOS=Set.of("pacientes","medicos","especialidades","establecimientos","horarios");
    private final PersonaRepository personas=new PersonaRepository();
    private final AuditoriaRepository auditoria=new AuditoriaRepository();
    private String tabla(String modulo) {
        switch(modulo){case "pacientes":return "Pacientes";case "medicos":return "Medicos";case "especialidades":return "Especialidades";case "establecimientos":return "Establecimientos";case "horarios":return "Horarios";default:throw new IllegalArgumentException("Módulo desconocido.");}
    }
    private String llave(String modulo) {
        switch(modulo){case "pacientes":return "idPaciente";case "medicos":return "idMedico";case "especialidades":return "idEspecialidad";case "establecimientos":return "idEstablecimiento";case "horarios":return "idHorario";default:throw new IllegalArgumentException("Módulo desconocido.");}
    }
    public List<Map<String,Object>> listar(String modulo) throws SQLException {
        switch(modulo) {
            case "pacientes":return Sql.consultar("SELECT p.idPaciente id,CONCAT(d.nombres,N' ',d.apellidos) paciente,d.dni,p.numeroSeguro,p.estado FROM Pacientes p JOIN DatosPersonales d ON d.idDatosPersonales=p.idDatosPersonales ORDER BY p.idPaciente DESC");
            case "medicos":return Sql.consultar("SELECT m.idMedico id,CONCAT(d.nombres,N' ',d.apellidos) medico,m.cmp,e.nombre especialidad,s.nombre establecimiento,m.estado FROM Medicos m JOIN DatosPersonales d ON d.idDatosPersonales=m.idDatosPersonales JOIN Especialidades e ON e.idEspecialidad=m.idEspecialidad JOIN Establecimientos s ON s.idEstablecimiento=m.idEstablecimiento ORDER BY m.idMedico DESC");
            case "horarios":return Sql.consultar("SELECT h.idHorario id,CONCAT(d.nombres,N' ',d.apellidos) medico,h.inicio,h.fin,h.estado,CASE WHEN EXISTS(SELECT 1 FROM Citas c WHERE c.idHorario=h.idHorario AND c.estado='RESERVADA') THEN N'Reservado' ELSE N'Libre' END disponibilidad FROM Horarios h JOIN Medicos m ON m.idMedico=h.idMedico JOIN DatosPersonales d ON d.idDatosPersonales=m.idDatosPersonales ORDER BY h.inicio DESC");
            case "especialidades":return Sql.consultar("SELECT idEspecialidad id,nombre,descripcion,estado FROM Especialidades ORDER BY nombre");
            case "establecimientos":return Sql.consultar("SELECT idEstablecimiento id,nombre,direccion,estado FROM Establecimientos ORDER BY nombre");
            default:throw new IllegalArgumentException("Módulo desconocido.");
        }
    }
    public Map<String,Object> buscar(String modulo,int id) throws SQLException {
        try(Connection c=ConexionBD.getInstancia().getConexion()) {
            Map<String,Object> datos=Sql.primero(Sql.consultar(c,"SELECT * FROM "+tabla(modulo)+" WHERE "+llave(modulo)+"=?",id));
            if(modulo.equals("pacientes")){Object estado=datos.get("estado");datos.putAll(personas.buscar(c,Sql.entero(datos.get("idDatosPersonales"))));datos.put("estado",estado);}
            if(modulo.equals("medicos")) datos.put("idUsuario",Sql.primero(Sql.consultar(c,"SELECT idUsuario FROM DatosPersonales WHERE idDatosPersonales=?",datos.get("idDatosPersonales"))).get("idUsuario"));
            datos.put("id",id);return datos;
        }
    }
    public int guardar(String modulo,Map<String,String> f,Usuario autor) throws SQLException {
        int id=Validacion.id(f,"id");String estado=Validacion.estado(f);
        try(Connection c=ConexionBD.getInstancia().getConexion()) {
            c.setTransactionIsolation(Connection.TRANSACTION_SERIALIZABLE);c.setAutoCommit(false);
            try {
                if(id>0&&Sql.consultar(c,"SELECT "+llave(modulo)+" FROM "+tabla(modulo)+" WITH(UPDLOCK,HOLDLOCK) WHERE "+llave(modulo)+"=?",id).isEmpty())throw new IllegalArgumentException("El registro ya no existe.");
                if(id>0&&estado.equals("Inactivo"))validarBaja(c,modulo,id);
                if(modulo.equals("pacientes")) {
                    int persona=id==0?0:Sql.entero(Sql.primero(Sql.consultar(c,"SELECT idDatosPersonales FROM Pacientes WHERE idPaciente=?",id)).get("idDatosPersonales"));
                    persona=personas.guardar(c,persona,null,f,autor.getNombreUsuario());
                    String seguro=Validacion.requerido(f,"numeroSeguro",30);
                    if(id==0) id=Sql.insertar(c,"INSERT Pacientes(idDatosPersonales,numeroSeguro,estado,usuarioRegistro) VALUES(?,?,?,?)",persona,seguro,estado,autor.getNombreUsuario());
                    else Sql.ejecutar(c,"UPDATE Pacientes SET numeroSeguro=?,estado=?,fechaActualizacion=CONVERT(datetime2, SYSDATETIMEOFFSET() AT TIME ZONE 'SA Pacific Standard Time') WHERE idPaciente=?",seguro,estado,id);
                } else if(modulo.equals("medicos")) {
                    int cuenta=Validacion.id(f,"idUsuario");
                    Usuario medico=new UsuarioRepositoryJdbc().buscarPorId(cuenta);
                    if(!(medico instanceof Medico)||!medico.isActivo())throw new IllegalArgumentException("Selecciona una cuenta activa creada como Médico.");
                    int persona=Sql.entero(Sql.primero(Sql.consultar(c,"SELECT d.idDatosPersonales FROM DatosPersonales d JOIN Usuarios u ON u.idUsuario=d.idUsuario WHERE u.idUsuario=? AND u.rol_id=3 AND u.estado='Activo'",cuenta)).get("idDatosPersonales"));
                    int especialidad=Validacion.id(f,"idEspecialidad"),centro=Validacion.id(f,"idEstablecimiento");
                    activo(c,"Especialidades","idEspecialidad",especialidad);activo(c,"Establecimientos","idEstablecimiento",centro);
                    String cmp=Validacion.requerido(f,"cmp",15);
                    if(!cmp.matches("[0-9]{4,15}")) throw new IllegalArgumentException("El CMP debe contener entre 4 y 15 dígitos.");
                    if(id==0) id=Sql.insertar(c,"INSERT Medicos(idDatosPersonales,cmp,idEspecialidad,idEstablecimiento,estado,usuarioRegistro) VALUES(?,?,?,?,?,?)",persona,cmp,especialidad,centro,estado,autor.getNombreUsuario());
                    else Sql.ejecutar(c,"UPDATE Medicos SET idDatosPersonales=?,cmp=?,idEspecialidad=?,idEstablecimiento=?,estado=?,fechaActualizacion=CONVERT(datetime2, SYSDATETIMEOFFSET() AT TIME ZONE 'SA Pacific Standard Time') WHERE idMedico=?",persona,cmp,especialidad,centro,estado,id);
                } else if(modulo.equals("horarios")) {
                    Horario horario=new Horario.Builder().medico(Validacion.id(f,"idMedico")).inicio(Validacion.requerido(f,"inicio",30)).fin(Validacion.requerido(f,"fin",30)).construir();
                    activo(c,"Medicos","idMedico",horario.getIdMedico());
                    Sql.consultar(c,"SELECT idMedico FROM Medicos WITH(UPDLOCK,HOLDLOCK) WHERE idMedico=?",horario.getIdMedico());
                    if(id!=0&&!Sql.consultar(c,"SELECT idCita FROM Citas WHERE idHorario=?",id).isEmpty()) throw new IllegalArgumentException("Un horario con historial de citas no se modifica. Crea un horario nuevo.");
                    if(!Sql.consultar(c,"SELECT idHorario FROM Horarios WITH(UPDLOCK,HOLDLOCK) WHERE idMedico=? AND estado='Activo' AND idHorario<>? AND inicio<? AND fin>?",horario.getIdMedico(),id,Timestamp.valueOf(horario.getFin()),Timestamp.valueOf(horario.getInicio())).isEmpty()) throw new IllegalArgumentException("Ese médico ya tiene un horario que se cruza con el indicado.");
                    if(id==0) id=Sql.insertar(c,"INSERT Horarios(idMedico,inicio,fin,estado,usuarioRegistro) VALUES(?,?,?,?,?)",horario.getIdMedico(),Timestamp.valueOf(horario.getInicio()),Timestamp.valueOf(horario.getFin()),estado,autor.getNombreUsuario());
                    else Sql.ejecutar(c,"UPDATE Horarios SET idMedico=?,inicio=?,fin=?,estado=?,fechaActualizacion=CONVERT(datetime2, SYSDATETIMEOFFSET() AT TIME ZONE 'SA Pacific Standard Time') WHERE idHorario=?",horario.getIdMedico(),Timestamp.valueOf(horario.getInicio()),Timestamp.valueOf(horario.getFin()),estado,id);
                } else if(modulo.equals("especialidades")||modulo.equals("establecimientos")) {
                    String columna=modulo.equals("especialidades")?"descripcion":"direccion";
                    Catalogo catalogo=new Catalogo.Builder().nombre(f.get("nombre")).detalle(f.get(columna)).requiereDireccion(modulo.equals("establecimientos")).construir();
                    String nombre=catalogo.getNombre(),detalle=catalogo.getDetalle();
                    if(id==0) id=Sql.insertar(c,"INSERT "+tabla(modulo)+"(nombre,"+columna+",estado,usuarioRegistro) VALUES(?,?,?,?)",nombre,detalle,estado,autor.getNombreUsuario());
                    else Sql.ejecutar(c,"UPDATE "+tabla(modulo)+" SET nombre=?,"+columna+"=?,estado=?,fechaActualizacion=CONVERT(datetime2, SYSDATETIMEOFFSET() AT TIME ZONE 'SA Pacific Standard Time') WHERE "+llave(modulo)+"=?",nombre,detalle,estado,id);
                } else throw new IllegalArgumentException("Módulo desconocido.");
                auditoria.registrar(c,autor.getIdUsuario(),modulo,"GUARDAR","Registro "+id);c.commit();return id;
            }catch(SQLException|RuntimeException e){c.rollback();throw e;}
        }
    }
    private void activo(Connection c,String tabla,String llave,int id) throws SQLException {
        if(Sql.consultar(c,"SELECT "+llave+" FROM "+tabla+" WHERE "+llave+"=? AND estado='Activo'",id).isEmpty()) throw new IllegalArgumentException("Selecciona un registro activo de "+tabla+".");
    }
    public void eliminar(String modulo,int id,Usuario autor) throws SQLException {
        try(Connection c=ConexionBD.getInstancia().getConexion()) {
            c.setTransactionIsolation(Connection.TRANSACTION_SERIALIZABLE);c.setAutoCommit(false);
            try {
                validarBaja(c,modulo,id);
                if(Sql.ejecutar(c,"UPDATE "+tabla(modulo)+" SET estado='Inactivo',fechaActualizacion=CONVERT(datetime2, SYSDATETIMEOFFSET() AT TIME ZONE 'SA Pacific Standard Time') WHERE "+llave(modulo)+"=?",id)!=1) throw new IllegalArgumentException("El registro no existe.");
                auditoria.registrar(c,autor.getIdUsuario(),modulo,"BAJA","Registro "+id);c.commit();
            }catch(SQLException|RuntimeException e){c.rollback();throw e;}
        }
    }
    private void validarBaja(Connection c,String modulo,int id) throws SQLException {
        String vinculados;
        switch(modulo) {
            case "pacientes":vinculados="SELECT idCita FROM Citas WHERE idPaciente=? AND estado='RESERVADA'";break;
            case "horarios":vinculados="SELECT idCita FROM Citas WHERE idHorario=? AND estado='RESERVADA'";break;
            case "medicos":vinculados="SELECT c.idCita FROM Citas c JOIN Horarios h ON h.idHorario=c.idHorario WHERE h.idMedico=? AND c.estado='RESERVADA'";break;
            case "especialidades":vinculados="SELECT idMedico FROM Medicos WHERE idEspecialidad=? AND estado='Activo'";break;
            case "establecimientos":vinculados="SELECT idMedico FROM Medicos WHERE idEstablecimiento=? AND estado='Activo'";break;
            default:throw new IllegalArgumentException("Módulo desconocido.");
        }
        if(!Sql.consultar(c,vinculados,id).isEmpty())throw new IllegalArgumentException("El registro tiene citas o registros activos relacionados. Resuélvelos antes de darlo de baja.");
    }
    public Map<String,List<Map<String,Object>>> opciones() throws SQLException {
        Map<String,List<Map<String,Object>>> opciones=new LinkedHashMap<>();
        opciones.put("roles",Sql.consultar("SELECT rol_id id,nombre_rol nombre FROM rol WHERE estado='Activo' ORDER BY rol_id"));
        opciones.put("sexos",Sql.consultar("SELECT idSexo id,descripcion nombre FROM Sexo WHERE estado='Activo'"));
        opciones.put("civiles",Sql.consultar("SELECT idEstadoCivil id,descripcion nombre FROM EstadoCivil WHERE estado='Activo'"));
        opciones.put("distritos",Sql.consultar("SELECT d.idDistrito id,CONCAT(d.nombreDistrito,N' — ',p.nombreProvincia,N' — ',r.nombreDepartamento,N' — ',a.nombrePais) nombre FROM Distrito d JOIN Provincia p ON p.idProvincia=d.idProvincia JOIN Departamento r ON r.idDepartamento=p.idDepartamento JOIN Pais a ON a.idPais=r.idPais WHERE d.estado='Activo'"));
        opciones.put("especialidades",Sql.consultar("SELECT idEspecialidad id,nombre FROM Especialidades WHERE estado='Activo'"));
        opciones.put("establecimientos",Sql.consultar("SELECT idEstablecimiento id,nombre FROM Establecimientos WHERE estado='Activo'"));
        opciones.put("cuentasMedicas",Sql.consultar("SELECT u.idUsuario id,CONCAT(p.nombres,N' ',p.apellidos,N' (',u.nombreUsuario,N')') nombre FROM Usuarios u JOIN DatosPersonales p ON p.idUsuario=u.idUsuario WHERE u.rol_id=3 AND u.estado='Activo'"));
        opciones.put("medicos",Sql.consultar("SELECT m.idMedico id,CONCAT(p.nombres,N' ',p.apellidos,N' — ',e.nombre) nombre FROM Medicos m JOIN DatosPersonales p ON p.idDatosPersonales=m.idDatosPersonales JOIN Especialidades e ON e.idEspecialidad=m.idEspecialidad WHERE m.estado='Activo'"));
        opciones.put("pacientes",Sql.consultar("SELECT a.idPaciente id,CONCAT(p.nombres,N' ',p.apellidos,N' — ',p.dni) nombre FROM Pacientes a JOIN DatosPersonales p ON p.idDatosPersonales=a.idDatosPersonales WHERE a.estado='Activo'"));
        return opciones;
    }
}
