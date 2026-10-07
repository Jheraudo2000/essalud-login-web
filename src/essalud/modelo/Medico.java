package essalud.modelo;
public final class Medico extends Usuario {
    public Medico(String id, String nombre, String clave) { super(id, nombre, clave); }
    @Override public String getRol() { return "Médico"; }
}
