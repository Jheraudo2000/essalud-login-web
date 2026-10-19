package essalud.seguridad;

import essalud.modelo.Usuario;
import essalud.util.Sql;
import java.sql.SQLException;

/** Proxy de protección. La autorización se comprueba en el servidor para cada operación. */
public final class AccesoProxy {
    public boolean permite(Usuario usuario,String modulo) throws SQLException {
        if(usuario==null||!usuario.isActivo()||usuario.isBloqueado()) return false;
        return !Sql.consultar("SELECT p.permiso_id FROM permiso p JOIN Roles_Permisos rp ON rp.permiso_id=p.permiso_id JOIN rol r ON r.rol_id=rp.rol_id WHERE rp.rol_id=? AND p.nombre_permiso=? AND p.estado='Activo' AND r.estado='Activo'",usuario.getRolId(),modulo).isEmpty();
    }
    public void exigir(Usuario usuario,String modulo) throws SQLException {
        if(!permite(usuario,modulo)) throw new SecurityException("Tu perfil no tiene permiso para esta operación.");
    }
}
