package essalud.servicio;
import java.sql.SQLException;
import java.util.*;
public interface ProveedorDisponibilidad {
    List<Map<String,Object>> consultar(String especialidad,String medico,String fecha) throws SQLException;
}
