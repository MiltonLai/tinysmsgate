package rocks.jahn.tinysmsgate.lib;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;

public class AESUtil {
    private static final Logger LOG = LoggerFactory.getLogger(AESUtil.class);

    /**
     * AES CBC Decryption with salt
     */
    public static byte[] aesCbcDecrypt(byte[] encrypted, String password, byte[] salt, int iterations) {
        return aesCbcDecrypt(encrypted, password, salt, iterations, null);
    }

    /**
     * AES CBC Decryption with salt and IV
     */
    public static byte[] aesCbcDecrypt(byte[] encrypted, String password, byte[] salt, int iterations, byte[] iv) {
        byte[] raw = getRaw(password, salt, iterations);
        if (raw == null) return null;
        return aesCbcDecrypt(encrypted, raw, iv);
    }

    /**
     * AES CBC Decryption
     */
    public static byte[] aesCbcDecrypt(byte[] encrypted, byte[] raw) {
        return aesCbcDecrypt(encrypted, raw, null);
    }

    /**
     * AES CBC Decryption
     *
     * @param encrypted encrypted content
     * @param raw byte[128] or byte[256]
     * @param iv optional, if not specified, the value of raw will be used instead
     */
    public static byte[] aesCbcDecrypt(byte[] encrypted, byte[] raw, byte[] iv) {
        try {
            SecretKeySpec skeySpec = new SecretKeySpec(raw, "AES");
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            if (iv == null) {
                cipher.init(Cipher.DECRYPT_MODE, skeySpec, new IvParameterSpec(raw));
            } else {
                cipher.init(Cipher.DECRYPT_MODE, skeySpec, new IvParameterSpec(iv));
            }
            return cipher.doFinal(encrypted);
        } catch (NoSuchAlgorithmException
                | InvalidAlgorithmParameterException
                | NoSuchPaddingException
                | InvalidKeyException
                | IllegalBlockSizeException
                | BadPaddingException e) {
            LOG.error(e.getMessage(), e);
            return null;
        }
    }

    /**
     * AES CBC Encryption with salt
     */
    public static byte[] aesCbcEncrypt(byte[] plain, String password, byte[] salt, int iterations) {
        return aesCbcEncrypt(plain, password, salt, iterations, null);
    }

    /**
     * AES CBC Encryption with salt and IV
     */
    public static byte[] aesCbcEncrypt(byte[] plain, String password, byte[] salt, int iterations, byte[] iv) {
        byte[] raw = getRaw(password, salt, iterations);
        if (raw == null) return null;
        return aesCbcEncrypt(plain, raw, iv);
    }

    /**
     * AES CBC Encryption
     */
    public static byte[] aesCbcEncrypt(byte[] plain, byte[] raw) {
        return aesCbcEncrypt(plain, raw, null);
    }

    /**
     * AES CBC Encryption
     *
     * @param plain plain text
     * @param raw byte[128] or byte[256]
     * @param iv optional, if not specified, the value of raw will be used instead
     */
    public static byte[] aesCbcEncrypt(byte[] plain, byte[] raw, byte[] iv) {
        try {
            SecretKeySpec skeySpec = new SecretKeySpec(raw, "AES");
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            if (iv == null) {
                cipher.init(Cipher.ENCRYPT_MODE, skeySpec, new IvParameterSpec(raw));
            } else {
                cipher.init(Cipher.ENCRYPT_MODE, skeySpec, new IvParameterSpec(iv));
            }
            return cipher.doFinal(plain);
        } catch (NoSuchAlgorithmException
                | InvalidAlgorithmParameterException
                | NoSuchPaddingException
                | InvalidKeyException
                | IllegalBlockSizeException
                | BadPaddingException e) {
            LOG.error(e.getMessage(), e);
            return null;
        }
    }

    /**
     * AES ECB Decryption
     * @param raw byte[128] or byte[256]
     */
    public static byte[] aesEcbDecrypt(byte[] encrypted, byte[] raw) {
        try {
            SecretKeySpec skeySpec = new SecretKeySpec(raw, "AES");
            Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
            cipher.init(Cipher.DECRYPT_MODE, skeySpec);
            return cipher.doFinal(encrypted);
        } catch (NoSuchPaddingException
                | NoSuchAlgorithmException
                | InvalidKeyException
                | IllegalBlockSizeException
                | BadPaddingException e) {
            LOG.error(e.getMessage(), e);
            return null;
        }
    }

    /**
     * AES ECB Encryption
     * @param raw byte[128] or byte[256]
     */
    public static byte[] aesEcbEncrypt(byte[] plain, byte[] raw) {
        try {
            SecretKeySpec skeySpec = new SecretKeySpec(raw, "AES");
            Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
            cipher.init(Cipher.ENCRYPT_MODE, skeySpec);
            return cipher.doFinal(plain);
        } catch (NoSuchPaddingException
                | NoSuchAlgorithmException
                | InvalidKeyException
                | IllegalBlockSizeException
                | BadPaddingException e) {
            LOG.error(e.getMessage(), e);
            return null;
        }
    }

    /**
     * Generate key with salt for AES
     *
     * @param password plain text password of any length
     * @param salt salt
     * @param iterations how many iterations -- larger for more secure
     * @return key for AES input
     */
    private static byte[] getRaw(String password, byte[] salt, int iterations) {
        try {
            SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
            KeySpec spec = new PBEKeySpec(password.toCharArray(), salt, iterations, 128);
            return factory.generateSecret(spec).getEncoded();
        } catch (InvalidKeySpecException |NoSuchAlgorithmException e) {
            LOG.error(e.getMessage(), e);
            return new byte[0];
        }
    }

    public static void main(String[] args) {
        byte[] text = "这是一段中文 and English".getBytes(StandardCharsets.UTF_8);
        String password = "8119745113133020";
        byte[] salt = "passcode.salt".getBytes();
        byte[] iv = "8119745113154120".getBytes();
        byte[] encrypted = aesCbcEncrypt(text, password, salt, 100, iv);
        System.out.println(CodecUtil.hexBytesToString(encrypted));
        byte[] decrypted = aesCbcDecrypt(encrypted, password, salt, 100, iv);
        String text2 = new String(decrypted, StandardCharsets.UTF_8);
        System.out.println(text2);

        encrypted = aesEcbEncrypt(text, password.getBytes());
        System.out.println(CodecUtil.hexBytesToString(encrypted));
        decrypted = aesEcbDecrypt(encrypted, password.getBytes());
        text2 = new String(decrypted, StandardCharsets.UTF_8);
        System.out.println(text2);

        encrypted = aesCbcEncrypt(text, password.getBytes());
        System.out.println(CodecUtil.hexBytesToString(encrypted));
        decrypted = aesCbcDecrypt(encrypted, password.getBytes());
        text2 = new String(decrypted, StandardCharsets.UTF_8);
        System.out.println(text2);
    }
}