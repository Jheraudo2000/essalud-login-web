package essalud.modelo;

import java.util.*;

/** Prototype: prepara un nuevo formulario con contactos comunes y sin duplicar la identidad. */
public final class FichaPaciente {
    private final Map<String,Object> datos;
    public FichaPaciente(Map<String,Object> datos){this.datos=new LinkedHashMap<>(datos);}
    public FichaPaciente copiarDatosBase() {
        Map<String,Object> copia=new LinkedHashMap<>(datos);
        for(String campo:Arrays.asList("id","idPaciente","idDatosPersonales","idUsuario","dni","numeroSeguro","nombres","apellidos")) copia.remove(campo);
        return new FichaPaciente(copia);
    }
    public Map<String,Object> getDatos(){return Collections.unmodifiableMap(datos);}
}
