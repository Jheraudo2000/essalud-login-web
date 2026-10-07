package essalud.modelo;
public final class OperadorCitas extends Usuario {
    public OperadorCitas(String id, String nombre, String clave) { super(id, nombre, clave); }
    @Override public String getRol() { return "Operador de citas"; }
}
