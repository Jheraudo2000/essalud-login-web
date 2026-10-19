package essalud.controlador;

import essalud.modelo.Usuario;
import essalud.servicio.*;
import essalud.vista.WebView;
import jakarta.servlet.*;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;

@WebServlet(urlPatterns={"/login","/salir"})
public final class AuthController extends HttpServlet {
    private final AuthService auth=new AuthService();
    @Override protected void doGet(HttpServletRequest req,HttpServletResponse res) throws IOException,ServletException {
        if(req.getServletPath().equals("/salir")){res.sendError(405);return;}
        if(req.getSession().getAttribute("usuarioId")!=null){WebView.redirigir(req,res,"/app/portal");return;}
        req.getRequestDispatcher("/WEB-INF/vistas/LoginView.jsp").forward(req,res);
    }
    @Override protected void doPost(HttpServletRequest req,HttpServletResponse res) throws IOException,ServletException {
        if(req.getServletPath().equals("/salir")){req.getSession().invalidate();WebView.redirigir(req,res,"/login");return;}
        try {
            AuthService.Resultado resultado=auth.autenticar(req.getParameter("usuario"),req.getParameter("clave"));
            Usuario usuario=resultado.getUsuario();
            if(usuario!=null){SesionService.iniciar(req,usuario.getIdUsuario());WebView.redirigir(req,res,"/app/portal");}
            else if(resultado.isBloqueado()) {
                req.getSession().invalidate();req.setAttribute("error",resultado.getMensaje());
                res.setStatus(423);req.getRequestDispatcher("/WEB-INF/vistas/BloqueadoView.jsp").forward(req,res);
            } else {req.setAttribute("error",resultado.getMensaje());res.setStatus(401);doGet(req,res);}
        }catch(Exception e){getServletContext().log("Error de autenticación",e);req.setAttribute("error",WebView.mensaje(e));res.setStatus(503);doGet(req,res);}
    }
}
