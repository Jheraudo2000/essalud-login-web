package essalud.servicio;

import essalud.configuracion.ConexionBD;
import essalud.modelo.Usuario;
import essalud.repositorio.UsuarioRepositoryJdbc;
import essalud.seguridad.ClaveSegura;
import essalud.util.Sql;
import java.sql.*;
import java.util.*;

/** Facade de autenticación: coordina hash, contador persistente, bloqueo e historial. */
public final class AuthService {
    public static final class Resultado {
        private final Usuario usuario; private final boolean bloqueado; private final String mensaje;
        Resultado(Usuario u,boolean b,String m){usuario=u;bloqueado=b;mensaje=m;}
        public Usuario getUsuario(){return usuario;} public boolean isBloqueado(){return bloqueado;} public String getMensaje(){return mensaje;}
    }
    public Resultado autenticar(String cuenta,String clave) throws SQLException {
        if(cuenta==null||cuenta.length()>50||clave==null||clave.length()>128) return new Resultado(null,false,"Usuario o contraseña incorrectos.");
        try(Connection c=ConexionBD.getInstancia().getConexion()) {
            c.setAutoCommit(false);
            try {
                List<Map<String,Object>> lista=Sql.consultar(c,"SELECT u.*,CONCAT(p.nombres,N' ',p.apellidos) nombre FROM Usuarios u WITH(UPDLOCK,HOLDLOCK) JOIN DatosPersonales p ON p.idUsuario=u.idUsuario WHERE u.nombreUsuario=?",cuenta.trim());
                Usuario usuario=null; boolean bloqueado=false; String resultado="ERROR";
                Integer id=null; String mensaje="Usuario o contraseña incorrectos.";
                if(!lista.isEmpty()) {
                    Map<String,Object> datos=lista.get(0);id=Sql.entero(datos.get("idUsuario"));
                    bloqueado=Boolean.TRUE.equals(datos.get("bloqueado"));
                    if(bloqueado) {resultado="BLOQUEADO";mensaje="La cuenta está bloqueada. Contacta al administrador.";}
                    else if(!"Activo".equals(datos.get("estado"))) {resultado="INACTIVO";mensaje="La cuenta no está activa.";}
                    else if(ClaveSegura.verificar(clave,Sql.texto(datos.get("contraseña")))) {
                        Sql.ejecutar(c,"UPDATE Usuarios SET intentosFallidos=0,ultimoAcceso=CONVERT(datetime2, SYSDATETIMEOFFSET() AT TIME ZONE 'SA Pacific Standard Time') WHERE idUsuario=?",id);
                        usuario=new UsuarioRepositoryJdbc().convertir(datos);resultado="CORRECTO";
                    } else {
                        int intentos=Math.min(3,Sql.entero(datos.get("intentosFallidos"))+1);bloqueado=intentos==3;
                        Sql.ejecutar(c,"UPDATE Usuarios SET intentosFallidos=?,bloqueado=?,fechaActualizacion=CONVERT(datetime2, SYSDATETIMEOFFSET() AT TIME ZONE 'SA Pacific Standard Time') WHERE idUsuario=?",intentos,bloqueado,id);
                        resultado=bloqueado?"BLOQUEADO":"ERROR";
                        mensaje=bloqueado?"Tres intentos incorrectos. La cuenta ha sido bloqueada.":"Usuario o contraseña incorrectos. Intentos restantes: "+(3-intentos)+".";
                    }
                }
                Sql.insertar(c,"INSERT Historial_Login(idUsuario,nombreUsuario,resultado) VALUES(?,?,?)",id,cuenta.trim(),resultado);
                c.commit();return new Resultado(usuario,bloqueado,mensaje);
            } catch(SQLException|RuntimeException e){c.rollback();throw e;}
        }
    }
}
