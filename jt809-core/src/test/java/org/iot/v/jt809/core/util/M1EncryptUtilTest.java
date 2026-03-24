package org.iot.v.jt809.core.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;

/**
 * M1EncryptUtil 单元测试
 *
 * @author haye
 * @date 2026-03-24
 */
@DisplayName("M1加密工具测试")
class M1EncryptUtilTest {

    @Test
    @DisplayName("生成M1密钥-正常数据")
    void testGenerateM1Key() {
        long userId = 12345678L;
        String password = "testPassword";
        
        byte[] key = M1EncryptUtil.generateM1Key(userId, password);
        
        assertNotNull(key);
        assertEquals(16, key.length, "AES密钥长度应为16字节");
    }

    @Test
    @DisplayName("生成M1密钥-相同输入产生相同密钥")
    void testGenerateM1KeyConsistency() {
        long userId = 12345678L;
        String password = "testPassword";
        
        byte[] key1 = M1EncryptUtil.generateM1Key(userId, password);
        byte[] key2 = M1EncryptUtil.generateM1Key(userId, password);
        
        assertArrayEquals(key1, key2, "相同输入应产生相同密钥");
    }

    @Test
    @DisplayName("生成M1密钥-不同输入产生不同密钥")
    void testGenerateM1KeyDifference() {
        byte[] key1 = M1EncryptUtil.generateM1Key(12345678L, "password1");
        byte[] key2 = M1EncryptUtil.generateM1Key(87654321L, "password2");
        
        assertFalse(java.util.Arrays.equals(key1, key2), "不同输入应产生不同密钥");
    }

    @Test
    @DisplayName("加密解密往返测试")
    void testEncryptDecryptRoundTrip() {
        long userId = 12345678L;
        String password = "testPassword";
        byte[] originalData = "Hello, JT809 Protocol!".getBytes(StandardCharsets.UTF_8);
        
        // 加密
        byte[] encrypted = M1EncryptUtil.encrypt(originalData, userId, password);
        assertNotNull(encrypted);
        
        // 解密
        byte[] decrypted = M1EncryptUtil.decrypt(encrypted, userId, password);
        assertNotNull(decrypted);
        
        // 验证
        assertArrayEquals(originalData, decrypted, "解密后数据应与原始数据一致");
    }

    @Test
    @DisplayName("加密后数据长度变化")
    void testEncryptedDataLength() {
        byte[] originalData = "Test".getBytes(StandardCharsets.UTF_8);
        
        byte[] encrypted = M1EncryptUtil.encrypt(originalData, 12345678L, "password");
        
        // AES加密会添加填充，长度会变化
        assertTrue(encrypted.length >= originalData.length);
    }

    @Test
    @DisplayName("解密-错误密钥应失败")
    void testDecryptWithWrongKey() {
        byte[] originalData = "Test Data".getBytes(StandardCharsets.UTF_8);
        
        byte[] encrypted = M1EncryptUtil.encrypt(originalData, 12345678L, "password1");
        
        // 使用错误的密码解密应该抛出异常
        assertThrows(RuntimeException.class, () -> {
            M1EncryptUtil.decrypt(encrypted, 12345678L, "password2");
        });
    }

    @Test
    @DisplayName("计算校验码-返回非零值")
    void testCalculateVerifyCode() {
        long userId = 12345678L;
        String password = "testPassword";
        
        long verifyCode = M1EncryptUtil.calculateVerifyCode(userId, password);
        
        // 校验码应该是4字节的非负整数
        assertTrue(verifyCode >= 0);
        assertTrue(verifyCode <= 0xFFFFFFFFL);
    }

    @Test
    @DisplayName("验证校验码-当前时间")
    void testVerifyCodeCurrentTime() {
        long userId = 12345678L;
        String password = "testPassword";
        
        // 计算当前时间的校验码
        long verifyCode = M1EncryptUtil.calculateVerifyCode(userId, password);
        
        // 验证应该在时间容差内通过
        boolean verified = M1EncryptUtil.verifyCode(verifyCode, userId, password, 5000);
        assertTrue(verified, "当前时间计算的校验码应能通过验证");
    }

    @Test
    @DisplayName("验证校验码-错误密码应失败")
    void testVerifyCodeWrongPassword() {
        long userId = 12345678L;
        
        long verifyCode = M1EncryptUtil.calculateVerifyCode(userId, "password1");
        
        boolean verified = M1EncryptUtil.verifyCode(verifyCode, userId, "password2", 5000);
        assertFalse(verified, "使用错误密码验证应失败");
    }
}
