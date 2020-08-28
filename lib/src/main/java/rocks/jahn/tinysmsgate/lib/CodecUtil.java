package rocks.jahn.tinysmsgate.lib;

import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.binary.Hex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CodecUtil {
    private static final Logger LOG = LoggerFactory.getLogger(CodecUtil.class);

    public static byte[] hexStringToBytes(String text) {
        try {
            return Hex.decodeHex(text.toCharArray());
        } catch (DecoderException e) {
            LOG.error(e.getMessage(), e);
            return null;
        }
    }

    public static String hexBytesToString(byte[] bytes) {
        return Hex.encodeHexString(bytes);
    }

    /**
     * Decode base64 string to bytes
     */
    public static byte[] base64StringToBytes(String base64String) {
        return Base64.decodeBase64(base64String);
    }

    /**
     * Encode bytes to base64 string with default UTF-8 charset
     */
    public static String base64BytesToString(byte[] bytes) {
        return Base64.encodeBase64String(bytes);
    }
}
