package com.mca.project.securedocsearch.util;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.SecureRandom;

public class AESUtil {

    private static final String TRANSFORMATION = "AES/CBC/PKCS5Padding";

    // encrypt: returns iv(16) + ciphertext
    public static byte[] encrypt(byte[] plaintext, byte[] key) throws Exception {
        SecureRandom rnd = new SecureRandom();
        byte[] iv = new byte[16];
        rnd.nextBytes(iv);

        SecretKeySpec keySpec = new SecretKeySpec(key, "AES");
        IvParameterSpec ivSpec = new IvParameterSpec(iv);

        Cipher cipher = Cipher.getInstance(TRANSFORMATION);
        cipher.init(Cipher.ENCRYPT_MODE, keySpec, ivSpec);
        byte[] ct = cipher.doFinal(plaintext);

        byte[] out = new byte[iv.length + ct.length];
        System.arraycopy(iv, 0, out, 0, iv.length);
        System.arraycopy(ct, 0, out, iv.length, ct.length);
        return out;
    }

    // decrypt: input is iv + ciphertext
    public static byte[] decrypt(byte[] ivAndCipher, byte[] key) throws Exception {
        if (ivAndCipher.length < 17) return new byte[0];
        byte[] iv = new byte[16];
        System.arraycopy(ivAndCipher, 0, iv, 0, 16);
        int ctLen = ivAndCipher.length - 16;
        byte[] ct = new byte[ctLen];
        System.arraycopy(ivAndCipher, 16, ct, 0, ctLen);

        SecretKeySpec keySpec = new SecretKeySpec(key, "AES");
        IvParameterSpec ivSpec = new IvParameterSpec(iv);

        Cipher cipher = Cipher.getInstance(TRANSFORMATION);
        cipher.init(Cipher.DECRYPT_MODE, keySpec, ivSpec);
        return cipher.doFinal(ct);
    }
}
