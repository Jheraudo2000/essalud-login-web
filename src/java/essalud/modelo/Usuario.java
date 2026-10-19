package essalud.modelo;

/** Entidad base: las especializaciones se construyen mediante Factory Method. */
public abstract class Usuario {
    private final int idUsuario;
    private final String nombreUsuario;
    private final String nombre;
    private final boolean bloqueado;
    private final boolean activo;
    protected Usuario(int id,String cuenta,String nombre,boolean bloqueado,boolean activo) {
        this.idUsuario=id;this.nombreUsuario=cuenta;this.nombre=nombre;this.bloqueado=bloqueado;this.activo=activo;
    }
    public int getIdUsuario(){return idUsuario;}
    public String getNombreUsuario(){return nombreUsuario;}
    public String getNombre(){return nombre;}
    public boolean isBloqueado(){return bloqueado;}
    public boolean isActivo(){return activo;}
    public abstract int getRolId();
    public abstract String getRol();
}
