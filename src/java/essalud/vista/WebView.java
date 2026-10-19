package essalud.vista;

import jakarta.servlet.http.*;
import java.io.IOException;
import java.sql.SQLException;
import java.util.*;

/** Solo utilidades de presentación y navegación; no contiene consultas SQL. */
public final class WebView {
    private WebView(){ }
    public static String e(Object valor) {
        if(valor==null)return "";
        String texto;
        if(valor instanceof java.sql.Timestamp)texto=((java.sql.Timestamp)valor).toLocalDateTime().format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
        else if(valor instanceof java.sql.Date)texto=((java.sql.Date)valor).toLocalDate().format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy"));
        else texto=valor.toString();
        return texto.replace("&","&amp;").replace("<","&lt;").replace(">","&gt;").replace("\"","&quot;").replace("'","&#39;");
    }
    public static Map<String,String> formulario(HttpServletRequest req) {
        Map<String,String> datos=new LinkedHashMap<>();req.getParameterMap().forEach((k,v)->datos.put(k,v.length==0?"":v[0]));return datos;
    }
    public static void redirigir(HttpServletRequest req,HttpServletResponse res,String ruta) throws IOException {
        res.setStatus(303);res.setHeader("Location",req.getContextPath()+ruta);
    }
    public static String mensaje(Exception e) {
        if(e instanceof IllegalArgumentException||e instanceof SecurityException) return e.getMessage();
        if(e instanceof SQLException) {
            int codigo=((SQLException)e).getErrorCode();
            if(codigo==2601||codigo==2627) return "Ya existe un registro con esos datos o el horario acaba de ser reservado.";
            if(codigo==547) return "Revisa los datos seleccionados y sus registros relacionados.";
        }
        return "No se pudo completar la operación. Inténtalo de nuevo o contacta al administrador.";
    }
    public static String etiqueta(String campo) {
        Map<String,String> nombres=Map.ofEntries(
            Map.entry("idUsuario","ID"),Map.entry("id","ID"),Map.entry("nombreUsuario","Usuario"),Map.entry("nombre","Nombre"),Map.entry("dni","DNI"),Map.entry("rol","Rol"),Map.entry("estado","Estado"),Map.entry("bloqueado","Bloqueado"),Map.entry("intentosFallidos","Intentos fallidos"),Map.entry("ultimoAcceso","Último acceso"),Map.entry("numeroSeguro","N.º de seguro"),Map.entry("idCita","ID"),Map.entry("codigo","Código"),Map.entry("inicio","Inicio"),Map.entry("fin","Fin"),Map.entry("cmp","CMP"),Map.entry("descripcion","Descripción"),Map.entry("direccion","Dirección"),Map.entry("idAuditoria","ID"),Map.entry("accion","Acción"),Map.entry("modulo","Módulo"),Map.entry("idHistorial","ID"));
        String nombre=nombres.getOrDefault(campo,campo);return nombre.isEmpty()?nombre:Character.toUpperCase(nombre.charAt(0))+nombre.substring(1);
    }
    public static String fechaInput(Object valor) {
        if(valor==null)return "";String s=valor.toString().replace(' ','T');return s.length()>16?s.substring(0,16):s;
    }
}
