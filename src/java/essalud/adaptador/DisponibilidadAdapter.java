package essalud.adaptador;

import essalud.repositorio.HorarioRepositoryJdbc;
import essalud.servicio.ProveedorDisponibilidad;
import java.sql.SQLException;
import java.util.*;

/** Adapter: presenta los horarios SQL como la disponibilidad que consume el flujo de citas. */
public final class DisponibilidadAdapter implements ProveedorDisponibilidad {
    private final HorarioRepositoryJdbc horarios;
    public DisponibilidadAdapter(){this(new HorarioRepositoryJdbc());}
    public DisponibilidadAdapter(HorarioRepositoryJdbc horarios){this.horarios=horarios;}
    @Override public List<Map<String,Object>> consultar(String especialidad,String medico,String fecha) throws SQLException {
        List<Map<String,Object>> disponibles=new ArrayList<>();
        for(Map<String,Object> origen:horarios.buscarDisponibles(especialidad,medico,fecha)) {
            Map<String,Object> cupo=new LinkedHashMap<>();
            cupo.put("idHorario",origen.get("idHorario"));cupo.put("medico",origen.get("nombreMedico"));
            cupo.put("especialidad",origen.get("nombreEspecialidad"));cupo.put("establecimiento",origen.get("nombreEstablecimiento"));
            cupo.put("inicio",origen.get("inicio"));cupo.put("fin",origen.get("fin"));disponibles.add(cupo);
        }
        return disponibles;
    }
}
