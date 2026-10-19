package essalud.repositorio;

import essalud.modelo.Usuario;
import java.sql.SQLException;
import java.util.*;

public interface CitaRepository {
    int reservar(int paciente,List<Integer> horarios,String motivo,Usuario autor) throws SQLException;
    void modificar(int cita,int horario,String accion,Usuario autor) throws SQLException;
    List<Map<String,Object>> listar(Usuario usuario) throws SQLException;
    Map<String,Object> constancia(int solicitud,Usuario usuario) throws SQLException;
}
