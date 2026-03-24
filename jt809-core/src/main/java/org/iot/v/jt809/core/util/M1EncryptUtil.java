package org.iot.v.jt809.core.util;

import lombok.extern.slf4j.Slf4j;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Arrays;

/**
 * M1加密工具类
 * 用于JT809协议数据加密
 *
 * @author haye
 * @date 2026-03-24
 */
@Slf4j
public class M1EncryptUtil {
    
    private static final String ALGORITHM = "AES";
    private static final String TRANSFORMATION = "AES/ECB/PKCS5Padding";
    
    /**
     * 使用M1进行加密
     * M1 = MD5(用户ID + 密码)
     *
     * @param data     待加密数据
     * @param userId   用户ID
     * @param password 密码
     * @return 加密后的数据
     */
    public static byte[] encrypt(byte[] data, long userId, String password) {
        try {
            byte[] key = generateM1Key(userId, password);
            SecretKeySpec keySpec = new SecretKeySpec(key, ALGORITHM);
            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            cipher.init(Cipher.ENCRYPT_MODE, keySpec);
            return cipher.doFinal(data);
        } catch (Exception e) {
            log.error("M1 encryption failed", e);
            throw new RuntimeException("M1 encryption failed", e);
        }
    }
    
    /**
     * 使用M1进行解密
     *
     * @param data     待解密数据
     * @param userId   用户ID
     * @param password 密码
     * @return 解密后的数据
     */
    public static byte[] decrypt(byte[] data, long userId, String password) {
        try {
            byte[] key = generateM1Key(userId, password);
            SecretKeySpec keySpec = new SecretKeySpec(key, ALGORITHM);
            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            cipher.init(Cipher.DECRYPT_MODE, keySpec);
            return cipher.doFinal(data);
        } catch (Exception e) {
            log.error("M1 decryption failed", e);
            throw new RuntimeException("M1 decryption failed", e);
        }
    }
    
    /**
     * 生成M1密钥
     * M1 = MD5(用户ID + 密码) 的前16字节
     *
     * @param userId   用户ID
     * @param password 密码
     * @return 16字节密钥
     */
    public static byte[] generateM1Key(long userId, String password) {
        try {
            String combined = userId + password;
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] digest = md.digest(combined.getBytes(StandardCharsets.UTF_8));
            return Arrays.copyOf(digest, 16); // AES需要16字节密钥
        } catch (Exception e) {
            log.error("Generate M1 key failed", e);
            throw new RuntimeException("Generate M1 key failed", e);
        }
    }
    
    /**
     * 计算校验码
     * 用于下行连接验证
     *
     * @param userId   用户ID
     * @param password 密码
     * @return 校验码
     */
    public static long calculateVerifyCode(long userId, String password) {
        try {
            String combined = userId + password + System.currentTimeMillis();
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] digest = md.digest(combined.getBytes(StandardCharsets.UTF_8));
            
            // 取前4字节作为校验码
            return ((digest[0] & 0xFFL) << 24) |
                   ((digest[1] & 0xFFL) << 16) |
                   ((digest[2] & 0xFFL) << 8) |
                   (digest[3] & 0xFFL);
        } catch (Exception e) {
            log.error("Calculate verify code failed", e);
            return 0;
        }
    }
    
    /**
     * 验证校验码
     *
     * @param expectedCode 期望的校验码
     * @param userId       用户ID
     * @param password     密码
     * @param tolerance    时间容差（毫秒）
     * @return 是否验证通过
     */
    public static boolean verifyCode(long expectedCode, long userId, String password, long tolerance) {
        long currentTime = System.currentTimeMillis();
        for (long t = currentTime - tolerance; t <= currentTime + tolerance; t += 1000) {
            try {
                String combined = userId + password + t;
                MessageDigest md = MessageDigest.getInstance("MD5");
                byte[] digest = md.digest(combined.getBytes(StandardCharsets.UTF_8));
                
                long code = ((digest[0] & 0xFFL) << 24) |
                           ((digest[1] & 0xFFL) << 16) |
                           ((digest[2] & 0xFFL) << 8) |
                           (digest[3] & 0xFFL);
                
                if (code == expectedCode) {
                    return true;
                }
            } catch (Exception ignored) {
            }
        }
        return false;
    }
}
