package essalud.modelo;
public final class Administrador extends Usuario {
    public Administrador(String id, String nombre, String clave) { super(id, nombre, clave); }
    @Override public String getRol() { return "Administrador"; }
}
