package essalud.servicio;

import jakarta.servlet.http.*;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

public final class SesionService {
    private SesionService(){ }
    public static String csrf(HttpSession s) {
        String token=(String)s.getAttribute("csrf");
        if(token==null){byte[] b=new byte[32];new SecureRandom().nextBytes(b);token=Base64.getUrlEncoder().withoutPadding().encodeToString(b);s.setAttribute("csrf",token);}
        return token;
    }
    public static boolean validarCsrf(HttpServletRequest req) {
        HttpSession s=req.getSession(false);String enviado=req.getParameter("csrf");
        return s!=null&&enviado!=null&&MessageDigest.isEqual(csrf(s).getBytes(StandardCharsets.UTF_8),enviado.getBytes(StandardCharsets.UTF_8));
    }
    public static void iniciar(HttpServletRequest req,int usuario) {
        HttpSession s=req.getSession();req.changeSessionId();s.setAttribute("usuarioId",usuario);s.removeAttribute("csrf");csrf(s);s.setMaxInactiveInterval(1800);
    }
}
