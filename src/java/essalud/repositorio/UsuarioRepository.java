package essalud.repositorio;

import essalud.modelo.Usuario;
import java.sql.SQLException;
import java.util.*;

public interface UsuarioRepository {
    Usuario buscarPorId(int id) throws SQLException;
    List<Map<String,Object>> listar(String filtro) throws SQLException;
    Map<String,Object> buscarFicha(int id) throws SQLException;
    int guardar(Map<String,String> datos,Usuario autor) throws SQLException;
    void eliminar(int id,Usuario autor) throws SQLException;
    void desbloquear(int id,Usuario autor) throws SQLException;
}
