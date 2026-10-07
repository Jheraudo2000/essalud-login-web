package essalud.vista;

import com.sun.net.httpserver.HttpExchange;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

/** Presentación HTTP, sin validaciones del modelo. */
public final class WebView {
    private WebView() { }
    public static void enviar(HttpExchange exchange, int status, String type, byte[] body) throws IOException {
        exchange.getResponseHeaders().set("Content-Type", type);
        exchange.getResponseHeaders().set("Cache-Control", "no-store");
        exchange.getResponseHeaders().set("X-Content-Type-Options", "nosniff");
        exchange.getResponseHeaders().set("Referrer-Policy", "same-origin");
        exchange.getResponseHeaders().set("Content-Security-Policy", "default-src 'self'; style-src 'self'; script-src 'self'; img-src 'self'; frame-ancestors 'none'; form-action 'self'; base-uri 'none'");
        exchange.sendResponseHeaders(status, body.length);
        try (java.io.OutputStream out = exchange.getResponseBody()) { out.write(body); }
    }
    public static void json(HttpExchange e, int status, String body) throws IOException {
        enviar(e, status, "application/json; charset=utf-8", body.getBytes(StandardCharsets.UTF_8));
    }
    public static void recurso(HttpExchange e, String name, String type) throws IOException {
        try (InputStream in = WebView.class.getResourceAsStream("/web/" + name)) {
            if (in == null) { json(e, 404, "{\"error\":\"Página no encontrada.\"}"); return; }
            enviar(e, 200, type, in.readAllBytes());
        }
    }
    public static void redirigir(HttpExchange e, String path) throws IOException {
        e.getResponseHeaders().set("Location", path);
        e.getResponseHeaders().set("Cache-Control", "no-store");
        e.sendResponseHeaders(303, -1); e.close();
    }
}
