package essalud.repositorio;

import essalud.util.Sql;
import java.sql.SQLException;
import java.util.*;

/** Interfaz del almacenamiento de horarios, independiente de los nombres que necesita la vista. */
public final class HorarioRepositoryJdbc {
    public List<Map<String,Object>> buscarDisponibles(String especialidad,String medico,String fecha) throws SQLException {
        String sql="SELECT TOP 150 h.idHorario,CONCAT(p.nombres,N' ',p.apellidos) nombreMedico,e.nombre nombreEspecialidad,s.nombre nombreEstablecimiento,h.inicio,h.fin FROM Horarios h JOIN Medicos m ON m.idMedico=h.idMedico JOIN DatosPersonales p ON p.idDatosPersonales=m.idDatosPersonales JOIN Usuarios u ON u.idUsuario=p.idUsuario JOIN Especialidades e ON e.idEspecialidad=m.idEspecialidad JOIN Establecimientos s ON s.idEstablecimiento=m.idEstablecimiento WHERE h.estado='Activo' AND m.estado='Activo' AND u.estado='Activo' AND e.estado='Activo' AND s.estado='Activo' AND h.inicio>CONVERT(datetime2, SYSDATETIMEOFFSET() AT TIME ZONE 'SA Pacific Standard Time') AND NOT EXISTS(SELECT 1 FROM Citas c WHERE c.idHorario=h.idHorario AND c.estado IN ('RESERVADA','ATENDIDA'))";
        List<Object> args=new ArrayList<>();
        if(especialidad!=null&&!especialidad.isEmpty()){sql+=" AND m.idEspecialidad=?";args.add(Integer.parseInt(especialidad));}
        if(medico!=null&&!medico.isEmpty()){sql+=" AND m.idMedico=?";args.add(Integer.parseInt(medico));}
        if(fecha!=null&&!fecha.isEmpty()){sql+=" AND CONVERT(date,h.inicio)=?";args.add(java.sql.Date.valueOf(fecha));}
        return Sql.consultar(sql+" ORDER BY h.inicio",args.toArray());
    }
}
