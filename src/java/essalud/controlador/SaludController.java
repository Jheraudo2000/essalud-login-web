package essalud.controlador;
import essalud.repositorio.UsuarioRepositoryJdbc;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;
@WebServlet("/salud")
public final class SaludController extends HttpServlet {
 @Override protected void doGet(HttpServletRequest req,HttpServletResponse res) throws IOException {
  res.setContentType("application/json;charset=UTF-8");
  try{if(!new UsuarioRepositoryJdbc().hayAdministradorActivo())throw new IllegalStateException("Falta el administrador inicial.");res.getWriter().write("{\"estado\":\"disponible\"}");}
  catch(Exception e){res.setStatus(503);res.getWriter().write("{\"estado\":\"no disponible\"}");}
 }
}
