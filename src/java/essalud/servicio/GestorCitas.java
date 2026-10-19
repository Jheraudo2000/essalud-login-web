package essalud.servicio;

import essalud.adaptador.DisponibilidadAdapter;
import essalud.modelo.Usuario;
import essalud.repositorio.CitaRepositoryJdbc;
import essalud.util.Validacion;
import java.sql.SQLException;
import java.util.*;

/** Facade del caso de uso: el controlador no necesita conocer los detalles del almacenamiento. */
public final class GestorCitas {
    private final CitaRepositoryJdbc citas=new CitaRepositoryJdbc();
    private final ProveedorDisponibilidad disponibilidad=new DisponibilidadAdapter();
    public List<Map<String,Object>> buscarCupos(Map<String,String> filtros) throws SQLException {
        return disponibilidad.consultar(filtros.get("especialidad"),filtros.get("medico"),filtros.get("fecha"));
    }
    public int reservar(Map<String,String> f,String[] seleccion,Usuario autor) throws SQLException {
        if(autor.getRolId()==3) throw new SecurityException("El médico consulta su agenda; la reserva corresponde al paciente u operador.");
        int paciente=autor.getRolId()==4?citas.pacientePropio(autor):Validacion.id(f,"idPaciente");
        List<Integer> horarios=new ArrayList<>();
        if(seleccion!=null)for(String id:seleccion)horarios.add(Integer.parseInt(id));
        return citas.reservar(paciente,horarios,Validacion.requerido(f,"motivo",200),autor);
    }
    public void modificar(Map<String,String> f,Usuario autor) throws SQLException {citas.modificar(Validacion.id(f,"idCita"),Validacion.id(f,"idHorario"),f.get("accion"),autor);}
}
