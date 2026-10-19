package essalud.fabrica;

/** Factory Method para recordatorios internos. No envía mensajes a servicios externos. */
public abstract class RecordatorioFactory {
    public abstract String crearRecordatorio(String codigo,String fecha);
    public static RecordatorioFactory portal(){return new RecordatorioPortal();}
    private static final class RecordatorioPortal extends RecordatorioFactory {
        @Override public String crearRecordatorio(String codigo,String fecha){return "Tu cita "+codigo+" está programada para "+fecha+". Revisa tu constancia.";}
    }
}
