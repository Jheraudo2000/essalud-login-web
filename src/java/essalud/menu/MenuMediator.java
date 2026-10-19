package essalud.menu;

import essalud.modelo.Usuario;
import essalud.seguridad.AccesoProxy;
import java.sql.SQLException;
import java.util.*;

/** Mediator: centraliza la selección y autorización de los comandos del menú. */
public final class MenuMediator {
    private final List<ComandoMenu> opciones=new ArrayList<>();
    public MenuMediator(Usuario u) throws SQLException {
        String[][] modulos={{"inicio","Inicio"},{"citas",u.getRolId()==3?"Mi agenda":"Citas médicas"},{"pacientes","Pacientes"},{"medicos","Médicos"},{"especialidades","Especialidades"},{"establecimientos","Establecimientos"},{"horarios","Horarios"},{"usuarios","Usuarios"},{"auditoria","Auditoría"},{"perfil","Mi perfil"}};
        AccesoProxy acceso=new AccesoProxy();
        for(String[] m:modulos)if(acceso.permite(u,m[0]))opciones.add(new AbrirModuloCommand(m[0],m[1]));
    }
    public List<ComandoMenu> getOpciones(){return Collections.unmodifiableList(opciones);}
    public ComandoMenu seleccionar(String codigo){return opciones.stream().filter(c->c.getCodigo().equals(codigo)).findFirst().orElseThrow(()->new SecurityException("No tienes acceso al módulo solicitado."));}
}
