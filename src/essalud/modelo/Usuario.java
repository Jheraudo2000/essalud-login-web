package essalud.modelo;

import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Arrays;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

/** Entidad base del diagrama: la contraseña se conserva solamente como hash. */
public abstract class Usuario {
    private final String id;
    private final String nombre;
    private final byte[] salt = new byte[16];
    private final byte[] claveHash;
    private final boolean activo = true;

    protected Usuario(String id, String nombre, String clave) {
        this.id = id;
        this.nombre = nombre;
        new SecureRandom().nextBytes(salt);
        claveHash = derivar(clave.toCharArray());
    }

    private byte[] derivar(char[] clave) {
        PBEKeySpec spec = new PBEKeySpec(clave, salt, 120000, 256);
        try {
            return SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256").generateSecret(spec).getEncoded();
        } catch (Exception ex) {
            throw new IllegalStateException("No se pudo preparar la autenticación", ex);
        } finally {
            spec.clearPassword();
            Arrays.fill(clave, '\0');
        }
    }

    public boolean validarCredenciales(String clave) {
        return activo && MessageDigest.isEqual(claveHash, derivar(clave.toCharArray()));
    }
    public String getId() { return id; }
    public String getNombre() { return nombre; }
    public abstract String getRol();
}
