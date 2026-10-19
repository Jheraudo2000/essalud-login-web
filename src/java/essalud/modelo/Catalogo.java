package essalud.modelo;

public final class Catalogo {
    private final String nombre,detalle;
    private Catalogo(Builder b){nombre=b.nombre;detalle=b.detalle;}
    public String getNombre(){return nombre;}public String getDetalle(){return detalle;}
    /** Builder mantiene las validaciones compartidas de los catálogos de servicios médicos. */
    public static final class Builder {
        private String nombre="",detalle="";private boolean direccion;
        public Builder nombre(String valor){nombre=valor==null?"":valor.trim();return this;}
        public Builder detalle(String valor){detalle=valor==null?"":valor.trim();return this;}
        public Builder requiereDireccion(boolean valor){direccion=valor;return this;}
        public Catalogo construir(){
            if(nombre.isEmpty()||nombre.length()>100)throw new IllegalArgumentException("Indica un nombre de hasta 100 caracteres.");
            if(detalle.length()>(direccion?180:255)||(direccion&&detalle.isEmpty()))throw new IllegalArgumentException("Revisa la descripción o dirección del catálogo.");
            return new Catalogo(this);
        }
    }
}
