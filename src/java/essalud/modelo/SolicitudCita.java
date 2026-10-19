package essalud.modelo;

import java.util.*;

/** Cabecera de la solicitud y su detalle de atenciones, equivalente al flujo de la semana 10. */
public final class SolicitudCita {
    private final int idPaciente;
    private final List<Cita> detalle=new ArrayList<>();
    public SolicitudCita(int paciente,List<Integer> horarios,String motivo) {
        if(horarios==null||horarios.isEmpty()||horarios.size()>3||new HashSet<>(horarios).size()!=horarios.size())throw new IllegalArgumentException("Selecciona entre uno y tres horarios diferentes.");
        idPaciente=paciente;List<Integer> ordenados=new ArrayList<>(horarios);Collections.sort(ordenados);
        for(int id:ordenados)detalle.add(new Cita(paciente,id,motivo));
    }
    public int getIdPaciente(){return idPaciente;}
    public List<Cita> getDetalle(){return Collections.unmodifiableList(detalle);}
}
