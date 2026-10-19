package essalud.repositorio;

import essalud.configuracion.ConexionBD;
import essalud.fabrica.UsuarioFactory;
import essalud.modelo.Usuario;
import essalud.seguridad.ClaveSegura;
import essalud.util.*;
import java.sql.*;
import java.util.*;

/** Repository: JDBC queda fuera de los controladores y de las páginas JSP. */
public final class UsuarioRepositoryJdbc implements UsuarioRepository {
    private final PersonaRepository personas=new PersonaRepository();
    private final AuditoriaRepository auditoria=new AuditoriaRepository();
    public Usuario convertir(Map<String,Object> f) {
        return UsuarioFactory.paraRol(Sql.entero(f.get("rol_id"))).crearUsuario(Sql.entero(f.get("idUsuario")),Sql.texto(f.get("nombreUsuario")),Sql.texto(f.get("nombre")),Boolean.TRUE.equals(f.get("bloqueado")),"Activo".equals(f.get("estado")));
    }
    @Override public Usuario buscarPorId(int id) throws SQLException {
        List<Map<String,Object>> filas=Sql.consultar("SELECT u.idUsuario,u.nombreUsuario,u.rol_id,u.bloqueado,u.estado,CONCAT(p.nombres,N' ',p.apellidos) nombre FROM Usuarios u JOIN DatosPersonales p ON p.idUsuario=u.idUsuario WHERE u.idUsuario=?",id);
        return filas.isEmpty()?null:convertir(filas.get(0));
    }
    @Override public List<Map<String,Object>> listar(String filtro) throws SQLException {
        return Sql.consultar("SELECT u.idUsuario,u.nombreUsuario,CONCAT(p.nombres,N' ',p.apellidos) nombre,p.dni,r.nombre_rol rol,u.estado,u.bloqueado,u.intentosFallidos,u.ultimoAcceso FROM Usuarios u JOIN rol r ON r.rol_id=u.rol_id JOIN DatosPersonales p ON p.idUsuario=u.idUsuario WHERE u.nombreUsuario LIKE ? OR p.nombres LIKE ? OR p.dni LIKE ? ORDER BY u.idUsuario DESC","%"+filtro+"%","%"+filtro+"%","%"+filtro+"%");
    }
    @Override public Map<String,Object> buscarFicha(int id) throws SQLException {
        try(Connection c=ConexionBD.getInstancia().getConexion()) {
            Map<String,Object> u=Sql.primero(Sql.consultar(c,"SELECT u.idUsuario,u.nombreUsuario,u.rol_id,u.permiso_id,u.estado,p.idDatosPersonales FROM Usuarios u JOIN DatosPersonales p ON p.idUsuario=u.idUsuario WHERE u.idUsuario=?",id));
            Object estado=u.get("estado");u.putAll(personas.buscar(c,Sql.entero(u.get("idDatosPersonales"))));u.put("estado",estado); return u;
        }
    }
    @Override public int guardar(Map<String,String> f,Usuario autor) throws SQLException {
        int id=Validacion.id(f,"id"),rol=Validacion.id(f,"rol_id");
        if(rol<1||rol>4) throw new IllegalArgumentException("Selecciona un rol válido.");
        String cuenta=Validacion.requerido(f,"nombreUsuario",50);
        if(!cuenta.matches("[a-zA-Z0-9._-]{3,50}")) throw new IllegalArgumentException("El usuario admite letras, números, punto, guion y guion bajo (3–50 caracteres).");
        String clave=f.getOrDefault("clave","");
        String hash=clave.isEmpty()?null:ClaveSegura.generar(clave);
        String estado=Validacion.estado(f);
        try(Connection c=ConexionBD.getInstancia().getConexion()) {
            c.setAutoCommit(false);
            try {
                int persona=0;
                if(id==0) {
                    if(hash==null) throw new IllegalArgumentException("Indica una contraseña para el usuario nuevo.");
                    id=Sql.insertar(c,"INSERT Usuarios(nombreUsuario,[contraseña],rol_id,permiso_id,usuarioRegistro,estado) VALUES(?,?,?,?,?,?)",cuenta,hash,rol,1,autor.getNombreUsuario(),estado);
                    List<Map<String,Object>> identidad=Sql.consultar(c,"SELECT idDatosPersonales,idUsuario FROM DatosPersonales WITH(UPDLOCK,HOLDLOCK) WHERE dni=?",Validacion.requerido(f,"dni",15));
                    if(!identidad.isEmpty()) {
                        if(identidad.get(0).get("idUsuario")!=null)throw new IllegalArgumentException("Ese DNI ya está vinculado a otra cuenta.");
                        persona=Sql.entero(identidad.get(0).get("idDatosPersonales"));
                        Sql.ejecutar(c,"UPDATE DatosPersonales SET idUsuario=? WHERE idDatosPersonales=?",id,persona);
                    }
                } else {
                    Map<String,Object> anterior=Sql.primero(Sql.consultar(c,"SELECT u.rol_id,p.idDatosPersonales FROM Usuarios u WITH(UPDLOCK,HOLDLOCK) JOIN DatosPersonales p ON p.idUsuario=u.idUsuario WHERE u.idUsuario=?",id));
                    if(id==autor.getIdUsuario()&&(rol!=1||!estado.equals("Activo"))) throw new IllegalArgumentException("No puedes desactivar tu propia cuenta ni quitarte el rol administrador.");
                    persona=Sql.entero(anterior.get("idDatosPersonales"));
                    Sql.ejecutar(c,"UPDATE Usuarios SET nombreUsuario=?,rol_id=?,estado=?,fechaActualizacion=CONVERT(datetime2, SYSDATETIMEOFFSET() AT TIME ZONE 'SA Pacific Standard Time') WHERE idUsuario=?",cuenta,rol,estado,id);
                    if(hash!=null) Sql.ejecutar(c,"UPDATE Usuarios SET [contraseña]=? WHERE idUsuario=?",hash,id);
                }
                persona=personas.guardar(c,persona,id,f,autor.getNombreUsuario());
                if(rol==4&&Sql.consultar(c,"SELECT idPaciente FROM Pacientes WHERE idDatosPersonales=?",persona).isEmpty())
                    Sql.insertar(c,"INSERT Pacientes(idDatosPersonales,numeroSeguro,usuarioRegistro) VALUES(?,?,?)",persona,"SEG-"+Validacion.requerido(f,"dni",15),autor.getNombreUsuario());
                auditoria.registrar(c,autor.getIdUsuario()==0?null:autor.getIdUsuario(),"usuarios","GUARDAR","Cuenta "+id);
                c.commit(); return id;
            } catch(SQLException|RuntimeException e) { c.rollback();throw e; }
        }
    }
    @Override public void eliminar(int id,Usuario autor) throws SQLException {
        if(id==autor.getIdUsuario()) throw new IllegalArgumentException("No puedes dar de baja tu propia cuenta.");
        modificar(id,autor,"BAJA","UPDATE Usuarios SET estado='Inactivo',fechaActualizacion=CONVERT(datetime2, SYSDATETIMEOFFSET() AT TIME ZONE 'SA Pacific Standard Time') WHERE idUsuario=?");
    }
    @Override public void desbloquear(int id,Usuario autor) throws SQLException {
        modificar(id,autor,"DESBLOQUEAR","UPDATE Usuarios SET bloqueado=0,intentosFallidos=0,fechaActualizacion=CONVERT(datetime2, SYSDATETIMEOFFSET() AT TIME ZONE 'SA Pacific Standard Time') WHERE idUsuario=?");
    }
    private void modificar(int id,Usuario autor,String accion,String sql) throws SQLException {
        try(Connection c=ConexionBD.getInstancia().getConexion()) {
            c.setAutoCommit(false);
            try {if(Sql.ejecutar(c,sql,id)!=1) throw new IllegalArgumentException("La cuenta no existe."); auditoria.registrar(c,autor.getIdUsuario(),"usuarios",accion,"Cuenta "+id);c.commit();}
            catch(SQLException|RuntimeException e){c.rollback();throw e;}
        }
    }
    public void guardarPerfil(Map<String,String> f,Usuario autor) throws SQLException {
        try(Connection c=ConexionBD.getInstancia().getConexion()) {
            c.setAutoCommit(false);
            try {
                Map<String,Object> u=Sql.primero(Sql.consultar(c,"SELECT u.[contraseña] hash,p.idDatosPersonales FROM Usuarios u WITH(UPDLOCK,HOLDLOCK) JOIN DatosPersonales p ON p.idUsuario=u.idUsuario WHERE u.idUsuario=?",autor.getIdUsuario()));
                personas.guardar(c,Sql.entero(u.get("idDatosPersonales")),autor.getIdUsuario(),f,autor.getNombreUsuario());
                String nueva=f.getOrDefault("clave","");
                if(!nueva.isEmpty()) {
                    if(!ClaveSegura.verificar(f.get("claveActual"),Sql.texto(u.get("hash")))) throw new IllegalArgumentException("La contraseña actual no coincide.");
                    Sql.ejecutar(c,"UPDATE Usuarios SET [contraseña]=?,fechaActualizacion=CONVERT(datetime2, SYSDATETIMEOFFSET() AT TIME ZONE 'SA Pacific Standard Time') WHERE idUsuario=?",ClaveSegura.generar(nueva),autor.getIdUsuario());
                }
                auditoria.registrar(c,autor.getIdUsuario(),"perfil","ACTUALIZAR","Datos de contacto actualizados"); c.commit();
            } catch(SQLException|RuntimeException e){c.rollback();throw e;}
        }
    }
    public boolean hayAdministradorActivo() throws SQLException {
        return Sql.entero(Sql.consultar("SELECT COUNT(*) n FROM Usuarios WHERE estado='Activo' AND rol_id=1").get(0).get("n"))>0;
    }
}
