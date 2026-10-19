package essalud.fabrica;

import essalud.modelo.*;

/** Factory Method: cada creador concreto decide qué especialización de Usuario construir. */
public abstract class UsuarioFactory {
    public abstract Usuario crearUsuario(int id,String cuenta,String nombre,boolean bloqueado,boolean activo);
    public static UsuarioFactory paraRol(int rol) {
        switch(rol) {
            case 1:return new CreadorAdministrador(); case 2:return new CreadorOperador();
            case 3:return new CreadorMedico(); case 4:return new CreadorPaciente();
            default:throw new IllegalArgumentException("Rol desconocido.");
        }
    }
    private static final class CreadorAdministrador extends UsuarioFactory {
        @Override public Usuario crearUsuario(int i,String c,String n,boolean b,boolean a){return new Administrador(i,c,n,b,a);}
    }
    private static final class CreadorOperador extends UsuarioFactory {
        @Override public Usuario crearUsuario(int i,String c,String n,boolean b,boolean a){return new OperadorCitas(i,c,n,b,a);}
    }
    private static final class CreadorMedico extends UsuarioFactory {
        @Override public Usuario crearUsuario(int i,String c,String n,boolean b,boolean a){return new Medico(i,c,n,b,a);}
    }
    private static final class CreadorPaciente extends UsuarioFactory {
        @Override public Usuario crearUsuario(int i,String c,String n,boolean b,boolean a){return new Paciente(i,c,n,b,a);}
    }
}
