package essalud.controlador;

import essalud.modelo.Usuario;
import essalud.seguridad.AccesoProxy;
import essalud.servicio.GestorCitas;
import essalud.vista.WebView;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.util.*;

@WebServlet("/app/citas")
public final class CitaController extends HttpServlet {
    @Override protected void doPost(HttpServletRequest req,HttpServletResponse res) throws IOException {
        Usuario u=(Usuario)req.getAttribute("usuario");Map<String,String> f=WebView.formulario(req);
        try {
            new AccesoProxy().exigir(u,"citas");GestorCitas gestor=new GestorCitas();
            if("reservar".equals(f.get("accion"))) {
                int id=gestor.reservar(f,req.getParameterValues("horarios"),u);
                req.getSession().setAttribute("exito","Solicitud registrada. Tu constancia está lista.");
                WebView.redirigir(req,res,"/app/portal?modulo=constancia&id="+id);return;
            }
            gestor.modificar(f,u);req.getSession().setAttribute("exito","La cita se actualizó correctamente.");
        }catch(Exception e){req.getSession().setAttribute("error",WebView.mensaje(e));if(!(e instanceof IllegalArgumentException||e instanceof SecurityException))getServletContext().log("Error en citas",e);}
        WebView.redirigir(req,res,"/app/portal?modulo=citas");
    }
}
