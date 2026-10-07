package essalud.controlador;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import essalud.modelo.Usuario;
import essalud.servicio.AuthService;
import essalud.servicio.SesionService;
import essalud.vista.WebView;
import java.io.IOException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class AuthController implements HttpHandler {
    private final AuthService auth;
    private final SesionService sesiones;
    private final String origenPublico;
    private final Map<String, Intentos> intentos = new ConcurrentHashMap<>();
    private static final class Intentos { long inicio = System.currentTimeMillis(); int cantidad; }
    public AuthController(AuthService auth, SesionService sesiones, String origenPublico) {
        this.auth = auth; this.sesiones = sesiones; this.origenPublico = origenPublico;
    }
    private String token(HttpExchange e) {
        String cookie = e.getRequestHeaders().getFirst("Cookie");
        if (cookie != null) for (String item : cookie.split(";")) {
            if (item.trim().startsWith("ESSALUD_SESSION=")) return item.trim().substring(16);
        }
        return null;
    }
    private void cookie(HttpExchange e, String token, int segundos) {
        String secure = origenPublico.startsWith("https://") ? "; Secure" : "";
        e.getResponseHeaders().set("Set-Cookie", "ESSALUD_SESSION=" + token + "; Path=/; HttpOnly; SameSite=Strict; Max-Age=" + segundos + secure);
    }
    private boolean mismoOrigen(HttpExchange e) {
        String esperado = origenPublico.isEmpty() ? "http://" + e.getRequestHeaders().getFirst("Host") : origenPublico;
        return esperado.equals(e.getRequestHeaders().getFirst("Origin"));
    }
    private synchronized boolean puedeIntentar(String ip) {
        long ahora = System.currentTimeMillis();
        intentos.entrySet().removeIf(e -> ahora - e.getValue().inicio > 60000);
        Intentos item = intentos.computeIfAbsent(ip, key -> new Intentos());
        return ++item.cantidad <= 5;
    }
    private String usuarioJson(Usuario u) {
        // Campos de las cuentas ficticias definidas por el repositorio, nunca datos del formulario.
        return "{\"usuario\":\"" + u.getId() + "\",\"nombre\":\"" + u.getNombre() + "\",\"rol\":\"" + u.getRol() + "\"}";
    }
    @Override public void handle(HttpExchange e) throws IOException {
        String path = e.getRequestURI().getPath();
        try {
            if ("/api/sesion".equals(path) && "GET".equals(e.getRequestMethod())) {
                Usuario u = sesiones.consultar(token(e));
                if (u == null) { cookie(e, "", 0); WebView.json(e, 401, "{\"error\":\"Inicia sesión para continuar.\"}"); }
                else WebView.json(e, 200, usuarioJson(u));
            } else if (("/api/login".equals(path) || "/api/logout".equals(path)) && "POST".equals(e.getRequestMethod())) {
                if (!mismoOrigen(e)) { WebView.json(e, 403, "{\"error\":\"Origen de solicitud no permitido.\"}"); return; }
                if ("/api/logout".equals(path)) {
                    sesiones.cerrar(token(e)); cookie(e, "", 0); WebView.json(e, 200, "{\"ok\":true}"); return;
                }
                String ip = e.getRemoteAddress().getAddress().getHostAddress();
                if (!puedeIntentar(ip)) { e.getResponseHeaders().set("Retry-After", "60"); WebView.json(e, 429, "{\"error\":\"Demasiados intentos. Espera un minuto y vuelve a intentar.\"}"); return; }
                String contentType = e.getRequestHeaders().getFirst("Content-Type");
                if (contentType == null || !contentType.startsWith("application/x-www-form-urlencoded")) { WebView.json(e, 415, "{\"error\":\"Formato no permitido.\"}"); return; }
                byte[] bytes = e.getRequestBody().readNBytes(2049);
                if (bytes.length > 2048) { WebView.json(e, 413, "{\"error\":\"Solicitud demasiado extensa.\"}"); return; }
                Map<String, String> form = new HashMap<>();
                for (String pair : new String(bytes, StandardCharsets.UTF_8).split("&")) {
                    String[] kv = pair.split("=", 2);
                    if (kv.length == 2) form.put(URLDecoder.decode(kv[0], "UTF-8"), URLDecoder.decode(kv[1], "UTF-8"));
                }
                Usuario u = auth.autenticar(form.get("usuario"), form.get("clave"));
                if (u == null) { WebView.json(e, 401, "{\"error\":\"Usuario o contraseña incorrectos.\"}"); return; }
                sesiones.cerrar(token(e)); cookie(e, sesiones.crear(u), 1800); WebView.json(e, 200, usuarioJson(u));
            } else if (path.startsWith("/api/")) {
                if (path.equals("/api/login") || path.equals("/api/logout") || path.equals("/api/sesion")) {
                    e.getResponseHeaders().set("Allow", path.equals("/api/sesion") ? "GET" : "POST");
                    WebView.json(e, 405, "{\"error\":\"Método no permitido.\"}");
                } else WebView.json(e, 404, "{\"error\":\"Ruta no encontrada.\"}");
            } else if (!"GET".equals(e.getRequestMethod())) {
                WebView.json(e, 405, "{\"error\":\"Método no permitido.\"}");
            } else if (path.equals("/") || path.equals("/LoginView.html")) {
                if (sesiones.consultar(token(e)) != null) WebView.redirigir(e, "/inicio");
                else WebView.recurso(e, "LoginView.html", "text/html; charset=utf-8");
            } else if (path.equals("/inicio") || path.equals("/InicioView.html")) {
                if (sesiones.consultar(token(e)) == null) WebView.redirigir(e, "/");
                else WebView.recurso(e, "InicioView.html", "text/html; charset=utf-8");
            } else if (path.equals("/styles.css")) WebView.recurso(e, "styles.css", "text/css; charset=utf-8");
            else if (path.equals("/app.js")) WebView.recurso(e, "app.js", "text/javascript; charset=utf-8");
            else WebView.json(e, 404, "{\"error\":\"Ruta no encontrada.\"}");
        } catch (IllegalArgumentException ex) {
            WebView.json(e, 400, "{\"error\":\"Solicitud inválida.\"}");
        } finally { e.close(); }
    }
}
