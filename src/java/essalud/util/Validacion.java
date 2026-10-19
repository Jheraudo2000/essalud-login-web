package essalud.util;

import java.util.Map;

public final class Validacion {
    private Validacion() { }
    public static String campo(Map<String,String> f,String nombre,int max) {
        String v=f.getOrDefault(nombre,"").trim();
        if(v.length()>max) throw new IllegalArgumentException("El campo "+nombre+" supera "+max+" caracteres.");
        return v;
    }
    public static String requerido(Map<String,String> f,String nombre,int max) {
        String v=campo(f,nombre,max);
        if(v.isEmpty()) throw new IllegalArgumentException("Completa el campo "+nombre+".");
        return v;
    }
    public static int id(Map<String,String> f,String nombre) {
        try { String texto=f.getOrDefault(nombre,"0").trim();int v=texto.isEmpty()?0:Integer.parseInt(texto); if(v<0) throw new NumberFormatException(); return v; }
        catch(NumberFormatException e) { throw new IllegalArgumentException("Identificador inválido: "+nombre+"."); }
    }
    public static void persona(Map<String,String> f) {
        requerido(f,"nombres",80); requerido(f,"apellidos",80);
        if(!requerido(f,"dni",15).matches("[0-9]{8}")) throw new IllegalArgumentException("El DNI debe tener ocho dígitos.");
        String correo=campo(f,"correo",80), telefono=campo(f,"telefono",20);
        if(!correo.isEmpty()&&!correo.matches("[^\\s@]+@[^\\s@]+\\.[^\\s@]+")) throw new IllegalArgumentException("Revisa el correo electrónico.");
        if(!telefono.isEmpty()&&!telefono.matches("[+0-9 ()-]{6,20}")) throw new IllegalArgumentException("Revisa el teléfono.");
    }
    public static String estado(Map<String,String> f) {
        String e=f.getOrDefault("estado","Activo");
        if(!e.equals("Activo")&&!e.equals("Inactivo")) throw new IllegalArgumentException("Estado inválido.");
        return e;
    }
}
