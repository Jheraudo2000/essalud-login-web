package essalud.modelo;

import java.time.LocalDateTime;

public final class Horario {
    private final int idMedico;
    private final LocalDateTime inicio,fin;
    private Horario(Builder b){idMedico=b.medico;inicio=b.inicio;fin=b.fin;}
    public int getIdMedico(){return idMedico;} public LocalDateTime getInicio(){return inicio;} public LocalDateTime getFin(){return fin;}
    /** Builder valida el objeto completo antes de permitir su uso. */
    public static final class Builder {
        private int medico; private LocalDateTime inicio,fin;
        public Builder medico(int id){medico=id;return this;}
        public Builder inicio(String valor){inicio=LocalDateTime.parse(valor);return this;}
        public Builder fin(String valor){fin=LocalDateTime.parse(valor);return this;}
        public Horario construir(){
            if(medico<=0||inicio==null||fin==null||!fin.isAfter(inicio)||!inicio.isAfter(LocalDateTime.now())) throw new IllegalArgumentException("El horario debe ser futuro y su fin posterior al inicio.");
            if(!inicio.toLocalDate().equals(fin.toLocalDate())) throw new IllegalArgumentException("El horario debe comenzar y terminar el mismo día.");
            return new Horario(this);
        }
    }
}
