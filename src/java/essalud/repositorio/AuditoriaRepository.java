package essalud.repositorio;

import essalud.util.Sql;
import java.sql.*;
import java.util.*;

public final class AuditoriaRepository {
    public void registrar(Connection c,Integer usuario,String modulo,String accion,String detalle) throws SQLException {
        Sql.insertar(c,"INSERT Log_Auditoria(idUsuario,modulo,accion,detalle) VALUES(?,?,?,?)",usuario,modulo,accion,detalle);
    }
    public List<Map<String,Object>> listar() throws SQLException {
        return Sql.consultar("SELECT TOP 300 a.idAuditoria,a.fecha,COALESCE(u.nombreUsuario,N'sistema') usuario,a.modulo,a.accion,a.detalle FROM Log_Auditoria a LEFT JOIN Usuarios u ON u.idUsuario=a.idUsuario ORDER BY a.idAuditoria DESC");
    }
    public List<Map<String,Object>> accesos() throws SQLException {
        return Sql.consultar("SELECT TOP 100 idHistorial,fecha,nombreUsuario,resultado FROM Historial_Login ORDER BY idHistorial DESC");
    }
}
