package essalud.controlador;

import essalud.menu.*;
import essalud.modelo.*;
import essalud.repositorio.*;
import essalud.seguridad.AccesoProxy;
import essalud.servicio.GestorCitas;
import essalud.util.*;
import essalud.vista.WebView;
import jakarta.servlet.*;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.util.*;

@WebServlet("/app/portal")
public final class PortalController extends HttpServlet {
    @Override protected void doGet(HttpServletRequest req,HttpServletResponse res) throws IOException,ServletException {
        Usuario usuario=(Usuario)req.getAttribute("usuario");
        try {
            MenuMediator menu=new MenuMediator(usuario);req.setAttribute("menu",menu.getOpciones());
            String modulo=req.getParameter("modulo");if(modulo==null||modulo.isBlank())modulo="inicio";
            req.setAttribute("modulo",modulo);
            if(modulo.equals("constancia")){new AccesoProxy().exigir(usuario,modulo);mostrarModulo(modulo,req,res);}
            else menu.seleccionar(modulo).ejecutar(this,req,res);
        }catch(Exception e) {
            if(!(e instanceof IllegalArgumentException||e instanceof SecurityException))getServletContext().log("Error al mostrar el módulo",e);
            res.setStatus(e instanceof SecurityException?403:e instanceof IllegalArgumentException?400:503);
            req.setAttribute("error",WebView.mensaje(e));req.getRequestDispatcher("/WEB-INF/vistas/ErrorView.jsp").forward(req,res);
        }
    }
    public void mostrarModulo(String modulo,HttpServletRequest req,HttpServletResponse res) throws Exception {
        Usuario u=(Usuario)req.getAttribute("usuario");CatalogoRepository catalogos=new CatalogoRepository();
        String vista;
        if(modulo.equals("inicio")) {
            List<Map<String,Object>> citas=new CitaRepositoryJdbc().listar(u);req.setAttribute("citas",citas);
            req.setAttribute("reservadas",citas.stream().filter(c->c.get("estado").equals("RESERVADA")).count());
            vista="InicioView.jsp";
        } else if(modulo.equals("usuarios")||CatalogoRepository.MODULOS.contains(modulo)||modulo.equals("perfil")) {
            int id=Validacion.id(WebView.formulario(req),"id");Map<String,Object> ficha=new LinkedHashMap<>();
            if(modulo.equals("usuarios")) {
                req.setAttribute("filas",new UsuarioRepositoryJdbc().listar(req.getParameter("q")==null?"":req.getParameter("q")));
                if(id>0)ficha=new UsuarioRepositoryJdbc().buscarFicha(id);
            } else if(modulo.equals("perfil")){id=u.getIdUsuario();ficha=new UsuarioRepositoryJdbc().buscarFicha(id);}
            else {
                req.setAttribute("filas",catalogos.listar(modulo));
                if(id>0)ficha=catalogos.buscar(modulo,id);
                if(modulo.equals("pacientes")&&"duplicar".equals(req.getParameter("accion"))){ficha=new LinkedHashMap<>(new FichaPaciente(ficha).copiarDatosBase().getDatos());id=0;}
            }
            ficha.put("id",id);req.setAttribute("ficha",ficha);req.setAttribute("opciones",catalogos.opciones());vista="CrudView.jsp";
        } else if(modulo.equals("citas")) {
            req.setAttribute("citas",new CitaRepositoryJdbc().listar(u));
            // Los catálogos de pacientes solo se entregan a quienes gestionan pacientes ajenos.
            Map<String,List<Map<String,Object>>> opciones=catalogos.opciones();
            if(u.getRolId()==3||u.getRolId()==4)opciones.remove("pacientes");
            req.setAttribute("opciones",opciones);
            if(u.getRolId()!=3)req.setAttribute("cupos",new GestorCitas().buscarCupos(WebView.formulario(req)));
            else req.setAttribute("cupos",Collections.emptyList());
            req.setAttribute("recordatorios",new CitaRepositoryJdbc().recordatorios(u));
            vista="CitasView.jsp";
        } else if(modulo.equals("constancia")) {
            req.setAttribute("constancia",new CitaRepositoryJdbc().constancia(Validacion.id(WebView.formulario(req),"id"),u));vista="ConstanciaView.jsp";
        } else if(modulo.equals("auditoria")) {
            req.setAttribute("filas",new AuditoriaRepository().listar());req.setAttribute("accesos",new AuditoriaRepository().accesos());vista="AuditoriaView.jsp";
        } else throw new IllegalArgumentException("Módulo desconocido.");
        req.getRequestDispatcher("/WEB-INF/vistas/"+vista).forward(req,res);
    }
}
