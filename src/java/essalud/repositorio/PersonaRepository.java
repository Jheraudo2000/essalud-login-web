package essalud.repositorio;

import essalud.util.Sql;
import essalud.util.Validacion;
import java.sql.*;
import java.util.*;

/** Persiste la identidad y los contactos en tablas separadas, como indica el DER de la guía. */
public final class PersonaRepository {
    public int guardar(Connection c,int persona,Integer usuario,Map<String,String> f,String autor) throws SQLException {
        Validacion.persona(f);
        Integer sexo=opcional(f,"idSexo"),civil=opcional(f,"idEstadoCivil");
        String nombres=Validacion.requerido(f,"nombres",80),apellidos=Validacion.requerido(f,"apellidos",80),dni=Validacion.requerido(f,"dni",15);
        if(persona==0) persona=Sql.insertar(c,"INSERT DatosPersonales(idUsuario,nombres,apellidos,dni,idSexo,idEstadoCivil,usuarioRegistro) VALUES(?,?,?,?,?,?,?)",usuario,nombres,apellidos,dni,sexo,civil,autor);
        else if(Sql.ejecutar(c,"UPDATE DatosPersonales SET nombres=?,apellidos=?,dni=?,idSexo=?,idEstadoCivil=?,fechaActualizacion=CONVERT(datetime2, SYSDATETIMEOFFSET() AT TIME ZONE 'SA Pacific Standard Time') WHERE idDatosPersonales=?",nombres,apellidos,dni,sexo,civil,persona)!=1)
            throw new IllegalArgumentException("La persona ya no existe.");
        contacto(c,"CorreoElectronico","correoElectronico","correo",persona,f,autor);
        contacto(c,"Telefono","numeroTelefono","telefono",persona,f,autor);
        String calle=Validacion.campo(f,"calleAvenida",150);
        Sql.ejecutar(c,"DELETE Direccion WHERE idDatosPersonales=?",persona);
        if(!calle.isEmpty()) {
            int distrito=Validacion.id(f,"idDistrito");
            if(distrito==0) throw new IllegalArgumentException("Selecciona el distrito de la dirección.");
            Sql.insertar(c,"INSERT Direccion(idDatosPersonales,idDistrito,calleAvenida,numero,referencia,usuarioRegistro) VALUES(?,?,?,?,?,?)",persona,distrito,calle,Validacion.campo(f,"numero",10),Validacion.campo(f,"referencia",255),autor);
        }
        return persona;
    }
    private Integer opcional(Map<String,String> f,String campo) { int n=Validacion.id(f,campo); return n==0?null:n; }
    private void contacto(Connection c,String tabla,String columna,String campo,int id,Map<String,String> f,String autor) throws SQLException {
        // tabla y columna son constantes internas, nunca parámetros recibidos desde el navegador.
        Sql.ejecutar(c,"DELETE "+tabla+" WHERE idDatosPersonales=?",id);
        String valor=Validacion.campo(f,campo,campo.equals("correo")?80:20);
        if(!valor.isEmpty()) Sql.insertar(c,"INSERT "+tabla+"(idDatosPersonales,"+columna+",usuarioRegistro) VALUES(?,?,?)",id,valor,autor);
    }
    public Map<String,Object> buscar(Connection c,int id) throws SQLException {
        return Sql.primero(Sql.consultar(c,"SELECT p.*, (SELECT TOP 1 correoElectronico FROM CorreoElectronico WHERE idDatosPersonales=p.idDatosPersonales ORDER BY idCorreo) correo, (SELECT TOP 1 numeroTelefono FROM Telefono WHERE idDatosPersonales=p.idDatosPersonales ORDER BY idTelefono) telefono, (SELECT TOP 1 calleAvenida FROM Direccion WHERE idDatosPersonales=p.idDatosPersonales ORDER BY idDireccion) calleAvenida, (SELECT TOP 1 numero FROM Direccion WHERE idDatosPersonales=p.idDatosPersonales ORDER BY idDireccion) numero, (SELECT TOP 1 referencia FROM Direccion WHERE idDatosPersonales=p.idDatosPersonales ORDER BY idDireccion) referencia, (SELECT TOP 1 idDistrito FROM Direccion WHERE idDatosPersonales=p.idDatosPersonales ORDER BY idDireccion) idDistrito FROM DatosPersonales p WHERE idDatosPersonales=?",id));
    }
}
