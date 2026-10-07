package essalud.servicio;

import essalud.modelo.Usuario;
import essalud.repositorio.UsuarioRepository;

public final class AuthService {
    private final UsuarioRepository repository;
    public AuthService(UsuarioRepository repository) { this.repository = repository; }
    public Usuario autenticar(String id, String clave) {
        if (id == null || clave == null || id.length() > 40 || clave.length() > 128) return null;
        Usuario usuario = repository.buscarPorId(id.trim());
        return usuario != null && usuario.validarCredenciales(clave) ? usuario : null;
    }
}
