package essalud.modelo;
public final class Paciente extends Usuario {
 public Paciente(int id,String cuenta,String nombre,boolean bloqueado,boolean activo){super(id,cuenta,nombre,bloqueado,activo);}
 @Override public int getRolId(){return 4;}
 @Override public String getRol(){return "Paciente";}
}
