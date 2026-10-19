package essalud.modelo;

/** Una atención solicitada: referencia al paciente, cupo y motivo, sin información clínica. */
public final class Cita {
    private final int idPaciente,idHorario;
    private final String motivo;
    public Cita(int paciente,int horario,String motivo) {
        if(paciente<=0||horario<=0||motivo==null||motivo.isBlank()||motivo.length()>200)throw new IllegalArgumentException("Revisa el paciente, horario y motivo de la cita.");
        idPaciente=paciente;idHorario=horario;this.motivo=motivo.trim();
    }
    public int getIdPaciente(){return idPaciente;}
    public int getIdHorario(){return idHorario;}
    public String getMotivo(){return motivo;}
}
