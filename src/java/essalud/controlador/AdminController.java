package essalud.controlador;

import essalud.modelo.Usuario;
import essalud.repositorio.*;
import essalud.seguridad.AccesoProxy;
import essalud.util.Validacion;
import essalud.vista.WebView;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.util.*;

@WebServlet("/app/guardar")
public final class AdminController extends HttpServlet {
    @Override protected void doPost(HttpServletRequest req,HttpServletResponse res) throws IOException {
        Usuario u=(Usuario)req.getAttribute("usuario");Map<String,String> f=WebView.formulario(req);
        String modulo=f.getOrDefault("modulo",""),accion=f.getOrDefault("accion","guardar");
        try {
            new AccesoProxy().exigir(u,modulo);int id=Validacion.id(f,"id");
            if(modulo.equals("perfil")) {
                if(!accion.equals("guardar"))throw new SecurityException("Acción de perfil inválida.");
                new UsuarioRepositoryJdbc().guardarPerfil(f,u);
            } else if(modulo.equals("usuarios")) {
                UsuarioRepository repo=new UsuarioRepositoryJdbc();
                switch(accion){case "guardar":repo.guardar(f,u);break;case "eliminar":repo.eliminar(id,u);break;case "desbloquear":repo.desbloquear(id,u);break;default:throw new IllegalArgumentException("Acción desconocida.");}
            } else if(CatalogoRepository.MODULOS.contains(modulo)) {
                CatalogoRepository repo=new CatalogoRepository();
                if(accion.equals("guardar"))repo.guardar(modulo,f,u);
                else if(accion.equals("eliminar"))repo.eliminar(modulo,id,u);
                else throw new IllegalArgumentException("Acción desconocida.");
            } else throw new SecurityException("Módulo no permitido.");
            req.getSession().setAttribute("exito","La operación se completó correctamente.");
        } catch(Exception e){
            if(e instanceof SecurityException){req.setAttribute("error",WebView.mensaje(e));res.sendError(403);return;}
            req.getSession().setAttribute("error",WebView.mensaje(e));if(!(e instanceof IllegalArgumentException))getServletContext().log("Error en mantenimiento",e);
        }
        String destino=Set.of("usuarios","perfil","pacientes","medicos","especialidades","establecimientos","horarios").contains(modulo)?modulo:"inicio";
        WebView.redirigir(req,res,"/app/portal?modulo="+destino);
    }
}
