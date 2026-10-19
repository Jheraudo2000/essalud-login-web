package essalud.menu;

import essalud.controlador.PortalController;
import jakarta.servlet.http.*;

public final class AbrirModuloCommand implements ComandoMenu {
    private final String codigo,titulo;
    public AbrirModuloCommand(String codigo,String titulo){this.codigo=codigo;this.titulo=titulo;}
    @Override public String getCodigo(){return codigo;}
    @Override public String getTitulo(){return titulo;}
    @Override public void ejecutar(PortalController portal,HttpServletRequest req,HttpServletResponse res) throws Exception {portal.mostrarModulo(codigo,req,res);}
}
