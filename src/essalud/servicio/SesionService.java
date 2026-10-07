package essalud.servicio;

import essalud.modelo.Usuario;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/** Sesiones del servidor: el navegador recibe un identificador aleatorio HttpOnly. */
public final class SesionService {
    private static final long DURACION = 30 * 60 * 1000L;
    private final Map<String, Sesion> sesiones = new ConcurrentHashMap<>();
    private final SecureRandom random = new SecureRandom();
    private static final class Sesion {
        final Usuario usuario;
        final long vence = System.currentTimeMillis() + DURACION;
        Sesion(Usuario usuario) { this.usuario = usuario; }
    }
    public String crear(Usuario usuario) {
        long ahora = System.currentTimeMillis();
        sesiones.entrySet().removeIf(e -> e.getValue().vence <= ahora);
        byte[] bytes = new byte[32]; random.nextBytes(bytes);
        String token = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
        sesiones.put(token, new Sesion(usuario));
        return token;
    }
    public Usuario consultar(String token) {
        if (token == null) return null;
        Sesion sesion = sesiones.get(token);
        if (sesion == null) return null;
        if (sesion.vence <= System.currentTimeMillis()) { cerrar(token); return null; }
        return sesion.usuario;
    }
    public void cerrar(String token) { if (token != null) sesiones.remove(token); }
}
