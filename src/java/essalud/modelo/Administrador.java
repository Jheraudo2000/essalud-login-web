package essalud.modelo;
public final class Administrador extends Usuario {
 public Administrador(int id,String cuenta,String nombre,boolean bloqueado,boolean activo){super(id,cuenta,nombre,bloqueado,activo);}
 @Override public int getRolId(){return 1;}
 @Override public String getRol(){return "Administrador";}
}
