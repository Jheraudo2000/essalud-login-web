package essalud.modelo;
public final class Medico extends Usuario {
 public Medico(int id,String cuenta,String nombre,boolean bloqueado,boolean activo){super(id,cuenta,nombre,bloqueado,activo);}
 @Override public int getRolId(){return 3;}
 @Override public String getRol(){return "Médico";}
}
