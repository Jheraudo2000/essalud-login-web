package essalud.seguridad;

import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.Arrays;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

/** Las contraseñas se almacenan como hash con sal; no se guardan ni se recuperan en texto. */
public final class ClaveSegura {
    private static final int ITERACIONES=600000;
    private ClaveSegura() { }
    private static byte[] derivar(String clave,byte[] sal,int vueltas) {
        char[] caracteres=clave.toCharArray();
        PBEKeySpec spec=new PBEKeySpec(caracteres,sal,vueltas,256);
        try { return SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256").generateSecret(spec).getEncoded(); }
        catch(Exception e) { throw new IllegalStateException("No se pudo procesar la contraseña.",e); }
        finally { spec.clearPassword(); Arrays.fill(caracteres,'\0'); }
    }
    public static String generar(String clave) {
        if(clave==null||clave.length()<6||clave.length()>128) throw new IllegalArgumentException("La contraseña debe tener entre 6 y 128 caracteres.");
        byte[] sal=new byte[16]; new SecureRandom().nextBytes(sal);
        return "pbkdf2_sha256$"+ITERACIONES+"$"+Base64.getEncoder().encodeToString(sal)+"$"+Base64.getEncoder().encodeToString(derivar(clave,sal,ITERACIONES));
    }
    public static boolean verificar(String clave,String guardado) {
        if(clave==null||clave.length()>128||guardado==null) return false;
        try {
            String[] p=guardado.split("\\$"); int vueltas=Integer.parseInt(p[1]);
            if(p.length!=4||!p[0].equals("pbkdf2_sha256")||vueltas<100000||vueltas>1000000) return false;
            return MessageDigest.isEqual(Base64.getDecoder().decode(p[3]),derivar(clave,Base64.getDecoder().decode(p[2]),vueltas));
        } catch(IllegalArgumentException|ArrayIndexOutOfBoundsException e) { return false; }
    }
}
