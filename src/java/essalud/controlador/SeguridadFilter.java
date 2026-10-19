package essalud.controlador;

import essalud.modelo.Usuario;
import essalud.repositorio.UsuarioRepositoryJdbc;
import essalud.servicio.SesionService;
import essalud.vista.WebView;
import jakarta.servlet.*;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.*;
import java.io.IOException;

@WebFilter("/*")
public final class SeguridadFilter implements Filter {
    @Override public void doFilter(ServletRequest peticion,ServletResponse respuesta,FilterChain cadena) throws IOException,ServletException {
        HttpServletRequest req=(HttpServletRequest)peticion;HttpServletResponse res=(HttpServletResponse)respuesta;
        req.setCharacterEncoding("UTF-8");res.setCharacterEncoding("UTF-8");
        res.setHeader("X-Content-Type-Options","nosniff");res.setHeader("X-Frame-Options","DENY");
        res.setHeader("Referrer-Policy","same-origin");
        res.setHeader("Content-Security-Policy","default-src 'self'; script-src 'self'; style-src 'self'; img-src 'self' data:; object-src 'none'; frame-ancestors 'none'; base-uri 'none'; form-action 'self'");
        res.setHeader("Cache-Control","no-store");
        String ruta=req.getRequestURI().substring(req.getContextPath().length());
        if(ruta.startsWith("/app/")) {
            HttpSession s=req.getSession(false);
            if(s==null||s.getAttribute("usuarioId")==null){WebView.redirigir(req,res,"/login");return;}
            try {
                Usuario usuario=new UsuarioRepositoryJdbc().buscarPorId((Integer)s.getAttribute("usuarioId"));
                if(usuario==null||!usuario.isActivo()||usuario.isBloqueado()){s.invalidate();WebView.redirigir(req,res,"/login");return;}
                req.setAttribute("usuario",usuario);
            } catch(Exception e){req.getServletContext().log("No se pudo validar la sesión",e);res.sendError(503);return;}
        }
        if("POST".equals(req.getMethod())&&!SesionService.validarCsrf(req)){res.sendError(403,"La sesión del formulario venció. Recarga la página.");return;}
        if(!ruta.startsWith("/assets/")&&!ruta.equals("/salud")) SesionService.csrf(req.getSession());
        cadena.doFilter(req,res);
    }
}
