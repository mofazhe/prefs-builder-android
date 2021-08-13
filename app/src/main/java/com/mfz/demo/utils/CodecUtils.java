package com.mfz.demo.utils;

import com.mfz.prefsbuilder.StringCodec;

import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.binary.Hex;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;

public class CodecUtils {

    private static final String AES_KEY = "Ff4t7h_`p1Mq&K23";
    // private static final String CIPHER_PADDING = "AES/ECB/PKCS5Padding";
    private static final String CIPHER_PADDING = "AES/CBC/PKCS5Padding";

    @StringCodec.Encode(id = 1)
    public static String encodeByBase64(String text) {
        Base64 base64 = new Base64(true);
        return base64.encodeToString(text.getBytes());
    }

    @StringCodec.Decode(id = 1)
    public static String decodeByBase64(String base64Text) {
        Base64 base64 = new Base64(true);
        return base64.encodeToString(base64Text.getBytes());
    }

    @StringCodec.Encode(id = 2)
    public static String encryptByAes(String text) {
        try {
            byte[] raw = AES_KEY.getBytes();
            SecretKeySpec keySpec = new SecretKeySpec(raw, CIPHER_PADDING);
            Cipher cipher = Cipher.getInstance(CIPHER_PADDING);
            cipher.init(Cipher.ENCRYPT_MODE, keySpec);
            byte[] encrypted = cipher.doFinal(text.getBytes());
            return Hex.encodeHexString(encrypted);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (NoSuchPaddingException e) {
            e.printStackTrace();
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        } catch (BadPaddingException e) {
            e.printStackTrace();
        } catch (IllegalBlockSizeException e) {
            e.printStackTrace();
        }
        return null;
    }

    @StringCodec.Decode(id = 2)
    public static String decryptByAes(String text) {
        try {
            byte[] raw = AES_KEY.getBytes();
            SecretKeySpec skeySpec = new SecretKeySpec(raw, CIPHER_PADDING);
            Cipher cipher = Cipher.getInstance(CIPHER_PADDING);
            cipher.init(Cipher.DECRYPT_MODE, skeySpec);
            byte[] encrypted = Hex.decodeHex(text);
            byte[] original = cipher.doFinal(encrypted);
            return new String(original);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (NoSuchPaddingException e) {
            e.printStackTrace();
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        } catch (DecoderException e) {
            e.printStackTrace();
        } catch (BadPaddingException e) {
            e.printStackTrace();
        } catch (IllegalBlockSizeException e) {
            e.printStackTrace();
        }
        return null;
    }
}
