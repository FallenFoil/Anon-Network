import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.UnsupportedEncodingException;
import java.security.*;

public class AESEncryptionManager {
    public static byte [] encryptData(String key, byte[] data) throws NoSuchAlgorithmException, UnsupportedEncodingException, NoSuchPaddingException, InvalidAlgorithmParameterException, InvalidKeyException, BadPaddingException, IllegalBlockSizeException {
        //Generating IV
        byte[] iv = "random_bytes_iv_".getBytes();
        IvParameterSpec ivParameterSpec = new IvParameterSpec(iv);

        // Hashing key.
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        digest.update(key.getBytes("UTF-8"));
        byte[] keyBytes = new byte[16];
        System.arraycopy(digest.digest(), 0, keyBytes, 0, keyBytes.length);
        SecretKeySpec secretKeySpec = new SecretKeySpec(keyBytes, "AES");

        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec, ivParameterSpec);
        return cipher.doFinal(data);
    }

    public static byte [] decryptData(String key, byte [] encryptedData) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidAlgorithmParameterException, InvalidKeyException, BadPaddingException, IllegalBlockSizeException {
        byte[] iv = "random_bytes_iv_".getBytes();
        IvParameterSpec ivParameterSpec = new IvParameterSpec(iv);

        byte[] keyBytes = new byte[key.length()];
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        md.update(key.getBytes());
        System.arraycopy(md.digest(), 0, keyBytes, 0, keyBytes.length);
        SecretKeySpec secretKeySpec = new SecretKeySpec(keyBytes, "AES");

        Cipher cipherDecrypt = Cipher.getInstance("AES/CBC/PKCS5Padding");
        cipherDecrypt.init(Cipher.DECRYPT_MODE, secretKeySpec, ivParameterSpec);

        return cipherDecrypt.doFinal(encryptedData);
    }
}
