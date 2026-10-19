package essalud.menu;

import essalud.controlador.PortalController;
import jakarta.servlet.http.*;

public interface ComandoMenu {
    String getCodigo();
    String getTitulo();
    void ejecutar(PortalController portal,HttpServletRequest req,HttpServletResponse res) throws Exception;
}
