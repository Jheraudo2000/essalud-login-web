package essalud.modelo;
public final class Paciente extends Usuario {
    public Paciente(String id, String nombre, String clave) { super(id, nombre, clave); }
    @Override public String getRol() { return "Paciente"; }
}
