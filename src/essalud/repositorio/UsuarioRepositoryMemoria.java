package essalud.repositorio;

import essalud.modelo.*;
import java.util.HashMap;
import java.util.Map;

/** Datos ficticios, sin conexión con los sistemas de EsSalud. */
public final class UsuarioRepositoryMemoria implements UsuarioRepository {
    private final Map<String, Usuario> usuarios = new HashMap<>();
    public UsuarioRepositoryMemoria() {
        agregar(new Paciente("paciente", "Alex Ejemplo", "Paciente2026!"));
        agregar(new OperadorCitas("operador", "Camila Ejemplo", "Operador2026!"));
        agregar(new Medico("medico", "Daniel Ejemplo", "Medico2026!"));
        agregar(new Administrador("admin", "Andrea Ejemplo", "Admin2026!"));
    }
    private void agregar(Usuario usuario) { usuarios.put(usuario.getId(), usuario); }
    @Override public Usuario buscarPorId(String id) { return usuarios.get(id); }
}
