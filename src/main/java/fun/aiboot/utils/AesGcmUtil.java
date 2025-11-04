package fun.aiboot.utils;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * AES-GCM 加解密工具（推荐模式：AES/GCM/NoPadding）
 * 特点：认证加密（防篡改）、无需填充、性能高
 */
public class AesGcmUtil {
    private static final String ALGORITHM = "AES";
    private static final String TRANSFORMATION = "AES/GCM/NoPadding";
    private static final int GCM_IV_LENGTH = 12; // 96-bit IV
    private static final int GCM_TAG_LENGTH = 16; // 128-bit auth tag

    /**
     * 生成随机密钥（Base64编码）
     */
    public static String generateKey() {
        try {
            KeyGenerator keyGen = KeyGenerator.getInstance(ALGORITHM);
            keyGen.init(256); // AES-256
            SecretKey key = keyGen.generateKey();
            return Base64.getEncoder().encodeToString(key.getEncoded());
        } catch (Exception e) {
            throw new RuntimeException("生成密钥失败", e);
        }
    }

    /**
     * 加密
     *
     * @param plainText 明文
     * @param base64Key Base64编码的密钥
     * @return Base64编码的密文（格式：IV + 密文 + AuthTag）
     */
    public static String encrypt(String plainText, String base64Key) {
        try {
            byte[] keyBytes = Base64.getDecoder().decode(base64Key);
            SecretKey key = new SecretKeySpec(keyBytes, ALGORITHM);

            // 生成随机IV
            byte[] iv = new byte[GCM_IV_LENGTH];
            new SecureRandom().nextBytes(iv);

            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            GCMParameterSpec spec = new GCMParameterSpec(GCM_TAG_LENGTH * 8, iv);
            cipher.init(Cipher.ENCRYPT_MODE, key, spec);

            byte[] cipherText = cipher.doFinal(plainText.getBytes(StandardCharsets.UTF_8));

            // 组合 IV + 密文
            byte[] result = new byte[iv.length + cipherText.length];
            System.arraycopy(iv, 0, result, 0, iv.length);
            System.arraycopy(cipherText, 0, result, iv.length, cipherText.length);

            return Base64.getEncoder().encodeToString(result);
        } catch (Exception e) {
            throw new RuntimeException("加密失败", e);
        }
    }

    /**
     * 解密
     *
     * @param base64CipherText Base64编码的密文
     * @param base64Key        Base64编码的密钥
     * @return 明文
     */
    public static String decrypt(String base64CipherText, String base64Key) {
        try {
            byte[] keyBytes = Base64.getDecoder().decode(base64Key);
            SecretKey key = new SecretKeySpec(keyBytes, ALGORITHM);

            byte[] cipherData = Base64.getDecoder().decode(base64CipherText);

            // 分离 IV 和密文
            byte[] iv = new byte[GCM_IV_LENGTH];
            byte[] cipherText = new byte[cipherData.length - GCM_IV_LENGTH];
            System.arraycopy(cipherData, 0, iv, 0, iv.length);
            System.arraycopy(cipherData, iv.length, cipherText, 0, cipherText.length);

            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            GCMParameterSpec spec = new GCMParameterSpec(GCM_TAG_LENGTH * 8, iv);
            cipher.init(Cipher.DECRYPT_MODE, key, spec);

            byte[] plainText = cipher.doFinal(cipherText);
            return new String(plainText, StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new RuntimeException("解密失败", e);
        }
    }
}