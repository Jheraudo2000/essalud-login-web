package essalud.modelo;
public final class OperadorCitas extends Usuario {
 public OperadorCitas(int id,String cuenta,String nombre,boolean bloqueado,boolean activo){super(id,cuenta,nombre,bloqueado,activo);}
 @Override public int getRolId(){return 2;}
 @Override public String getRol(){return "Operador";}
}
