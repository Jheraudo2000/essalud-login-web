package essalud.modelo;

/** State: los estados finales impiden repetir cancelaciones o modificar citas concluidas. */
public abstract class EstadoCita {
    public abstract void validar(String accion);
    public static EstadoCita de(String valor) {return valor.equals("RESERVADA")?new Reservada():new Finalizada();}
    private static final class Reservada extends EstadoCita {
        @Override public void validar(String accion){if(!java.util.Set.of("cancelar","reprogramar","atender").contains(accion)) throw new IllegalArgumentException("Acción inválida.");}
    }
    private static final class Finalizada extends EstadoCita {
        @Override public void validar(String accion){throw new IllegalArgumentException("La cita ya está cancelada o atendida y no admite cambios.");}
    }
}
