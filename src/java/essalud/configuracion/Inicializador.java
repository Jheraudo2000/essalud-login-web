package essalud.configuracion;

import essalud.modelo.Administrador;
import essalud.repositorio.*;
import essalud.util.Sql;
import jakarta.servlet.*;
import jakarta.servlet.annotation.WebListener;
import java.sql.*;
import java.util.*;

@WebListener
public final class Inicializador implements ServletContextListener {
    @Override public void contextInitialized(ServletContextEvent evento) {
        evento.getServletContext().getSessionCookieConfig().setName("ESSALUD_SESSION");
        evento.getServletContext().getSessionCookieConfig().setHttpOnly(true);
        if("true".equalsIgnoreCase(System.getenv("ESSALUD_HTTPS")))evento.getServletContext().getSessionCookieConfig().setSecure(true);
        try {
            if(Sql.entero(Sql.consultar("SELECT COUNT(*) n FROM Usuarios").get(0).get("n"))==0) {
                boolean demo="true".equalsIgnoreCase(System.getenv("ESSALUD_DEMO"));
                String clave=System.getenv("ESSALUD_ADMIN_CLAVE");
                if(!demo&&(clave==null||clave.isEmpty()))throw new IllegalStateException("Define ESSALUD_ADMIN_CLAVE para crear el administrador inicial.");
                // La auditoría del primer administrador admite el autor nulo, sin inventar un ID existente.
                Map<String,String> f=datos("admin","Andrea","Ejemplo","90000001","1",demo?"Admin2026!":clave);
                int admin=crearInicial(f);
                if(demo) {
                    Administrador autor=new Administrador(admin,"admin","Andrea Ejemplo",false,true);
                    UsuarioRepositoryJdbc repo=new UsuarioRepositoryJdbc();
                    repo.guardar(datos("operador","Camila","Ejemplo","90000002","2","Operador2026!"),autor);
                    int medico=repo.guardar(datos("medico","Daniel","Ejemplo","90000003","3","Medico2026!"),autor);
                    repo.guardar(datos("paciente","Alex","Ejemplo","90000004","4","Paciente2026!"),autor);
                    Map<String,String> m=new LinkedHashMap<>();m.put("idUsuario",String.valueOf(medico));m.put("cmp","90001");m.put("idEspecialidad","1");m.put("idEstablecimiento","1");
                    new CatalogoRepository().guardar("medicos",m,autor);
                }
            }
            evento.getServletContext().setAttribute("bdLista",true);
        } catch(Exception e){evento.getServletContext().log("No se pudo preparar la base del portal",e);evento.getServletContext().setAttribute("bdLista",false);}
    }
    private int crearInicial(Map<String,String> f) throws SQLException {
        try(Connection c=ConexionBD.getInstancia().getConexion()) {
            c.setAutoCommit(false);
            try {
                int id=Sql.insertar(c,"INSERT Usuarios(nombreUsuario,[contraseña],rol_id,permiso_id,usuarioRegistro) VALUES(?,?,1,1,'sistema')",f.get("nombreUsuario"),essalud.seguridad.ClaveSegura.generar(f.get("clave")));
                new PersonaRepository().guardar(c,0,id,f,"sistema");
                new AuditoriaRepository().registrar(c,id,"usuarios","CREAR_INICIAL","Administrador inicial");c.commit();return id;
            }catch(SQLException|RuntimeException e){c.rollback();throw e;}
        }
    }
    private Map<String,String> datos(String cuenta,String nombre,String apellido,String dni,String rol,String clave) {
        Map<String,String> f=new LinkedHashMap<>();f.put("nombreUsuario",cuenta);f.put("nombres",nombre);f.put("apellidos",apellido);f.put("dni",dni);f.put("rol_id",rol);f.put("clave",clave);return f;
    }
}
