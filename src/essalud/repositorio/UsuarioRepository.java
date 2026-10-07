package essalud.repositorio;
import essalud.modelo.Usuario;
public interface UsuarioRepository {
    Usuario buscarPorId(String id);
}
