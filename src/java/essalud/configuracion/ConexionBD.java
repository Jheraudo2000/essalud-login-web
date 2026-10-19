package essalud.configuracion;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/** Singleton: una configuración compartida. Cada transacción recibe su propia conexión JDBC. */
public final class ConexionBD {
    private final Properties propiedades = new Properties();
    private ConexionBD() {
        String archivo = System.getenv("ESSALUD_CONFIG");
        if (archivo == null) archivo = System.getProperty("catalina.base", ".") + "/conf/essalud-db.properties";
        Path ruta = Path.of(archivo);
        try {
            if (Files.exists(ruta)) try (InputStream in = Files.newInputStream(ruta)) { propiedades.load(in); }
            Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
        } catch (Exception ex) { throw new IllegalStateException("No se pudo cargar la configuración JDBC.", ex); }
    }
    private static final class Instancia { private static final ConexionBD VALOR = new ConexionBD(); }
    public static ConexionBD getInstancia() { return Instancia.VALOR; }
    private String valor(String entorno, String clave, String defecto) {
        String v = System.getenv(entorno);
        return v == null ? propiedades.getProperty(clave, defecto) : v;
    }
    public Connection getConexion() throws SQLException {
        String url = valor("ESSALUD_DB_URL", "db.url", "jdbc:sqlserver://localhost:1433;databaseName=EsSaludCitasSemana10;encrypt=true;trustServerCertificate=true");
        String usuario = valor("ESSALUD_DB_USUARIO", "db.usuario", "");
        return usuario.isEmpty() ? DriverManager.getConnection(url)
                : DriverManager.getConnection(url, usuario, valor("ESSALUD_DB_CLAVE", "db.clave", ""));
    }
}
