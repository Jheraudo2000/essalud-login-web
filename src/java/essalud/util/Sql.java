package essalud.util;

import essalud.configuracion.ConexionBD;
import java.sql.*;
import java.util.*;

/** Utilidades de JDBC. Los textos SQL se definen exclusivamente en los repositorios. */
public final class Sql {
    private Sql() { }
    public static void parametros(PreparedStatement ps, Object... args) throws SQLException {
        for (int i=0; i<args.length; i++) ps.setObject(i+1, args[i]);
    }
    public static List<Map<String,Object>> consultar(Connection c, String sql, Object... args) throws SQLException {
        try (PreparedStatement ps=c.prepareStatement(sql)) {
            parametros(ps,args);
            try (ResultSet rs=ps.executeQuery()) {
                List<Map<String,Object>> filas=new ArrayList<>();
                ResultSetMetaData meta=rs.getMetaData();
                while(rs.next()) {
                    Map<String,Object> fila=new LinkedHashMap<>();
                    for(int i=1;i<=meta.getColumnCount();i++) fila.put(meta.getColumnLabel(i),rs.getObject(i));
                    filas.add(fila);
                }
                return filas;
            }
        }
    }
    public static List<Map<String,Object>> consultar(String sql,Object... args) throws SQLException {
        try(Connection c=ConexionBD.getInstancia().getConexion()) { return consultar(c,sql,args); }
    }
    public static int ejecutar(Connection c,String sql,Object... args) throws SQLException {
        try(PreparedStatement ps=c.prepareStatement(sql)) { parametros(ps,args); return ps.executeUpdate(); }
    }
    public static int insertar(Connection c,String sql,Object... args) throws SQLException {
        try(PreparedStatement ps=c.prepareStatement(sql,Statement.RETURN_GENERATED_KEYS)) {
            parametros(ps,args); ps.executeUpdate();
            try(ResultSet rs=ps.getGeneratedKeys()) { if(rs.next()) return rs.getInt(1); }
            throw new SQLException("La inserción no devolvió un identificador.");
        }
    }
    public static int entero(Object v) { return v==null?0:Integer.parseInt(v.toString()); }
    public static String texto(Object v) { return v==null?"":v.toString(); }
    public static Map<String,Object> primero(List<Map<String,Object>> filas) {
        if(filas.isEmpty()) throw new IllegalArgumentException("El registro no existe.");
        return filas.get(0);
    }
}
