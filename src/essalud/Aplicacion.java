package essalud;

import com.sun.net.httpserver.HttpServer;
import essalud.controlador.AuthController;
import essalud.repositorio.UsuarioRepositoryMemoria;
import essalud.servicio.AuthService;
import essalud.servicio.SesionService;
import java.net.InetSocketAddress;
import java.util.concurrent.Executors;

public final class Aplicacion {
    public static void main(String[] args) throws Exception {
        String valorPuerto = System.getenv("PORT");
        int puerto = valorPuerto == null ? 8080 : Integer.parseInt(valorPuerto);
        String origen = System.getenv("PUBLIC_ORIGIN");
        if (origen == null || origen.isEmpty()) origen = System.getenv("RENDER_EXTERNAL_URL");
        if (origen == null) origen = "";
        if (!origen.isEmpty() && !origen.matches("https?://[^/]+")) throw new IllegalArgumentException("PUBLIC_ORIGIN debe ser un origen sin ruta ni barra final");
        HttpServer servidor = HttpServer.create(new InetSocketAddress("0.0.0.0", puerto), 64);
        servidor.createContext("/", new AuthController(new AuthService(new UsuarioRepositoryMemoria()), new SesionService(), origen));
        servidor.setExecutor(Executors.newFixedThreadPool(8));
        Runtime.getRuntime().addShutdownHook(new Thread(() -> servidor.stop(0)));
        servidor.start();
        System.out.println("Portal de acceso iniciado en el puerto " + puerto + ".");
    }
}
