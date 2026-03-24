package org.iot.v.jt809.core.message.base;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * MessageHead 单元测试
 *
 * @author haye
 * @date 2026-03-24
 */
@DisplayName("消息头测试")
class MessageHeadTest {

    @Test
    @DisplayName("创建消息头-默认值")
    void testCreateDefault() {
        MessageHead head = new MessageHead();
        
        assertEquals(0, head.getMsgLength());
        assertEquals(0, head.getMsgSn());
        assertEquals(0, head.getMsgId());
        assertEquals(0, head.getPlatformId());
        assertNotNull(head.getVersion());
        assertEquals(0, head.getSuperPlatformId());
        assertEquals(0, head.getEncrypt());
        assertEquals(0, head.getEncryptKey());
    }

    @Test
    @DisplayName("设置和获取消息长度")
    void testMsgLength() {
        MessageHead head = new MessageHead();
        head.setMsgLength(100);
        
        assertEquals(100, head.getMsgLength());
    }

    @Test
    @DisplayName("设置和获取流水号")
    void testMsgSn() {
        MessageHead head = new MessageHead();
        head.setMsgSn(12345);
        
        assertEquals(12345, head.getMsgSn());
    }

    @Test
    @DisplayName("设置和获取消息ID")
    void testMsgId() {
        MessageHead head = new MessageHead();
        head.setMsgId(0x1001);
        
        assertEquals(0x1001, head.getMsgId());
    }

    @Test
    @DisplayName("设置和获取平台ID")
    void testPlatformId() {
        MessageHead head = new MessageHead();
        head.setPlatformId(12345678901234L);
        
        assertEquals(12345678901234L, head.getPlatformId());
    }

    @Test
    @DisplayName("设置和获取协议版本")
    void testVersion() {
        MessageHead head = new MessageHead();
        ProtocolVersion version = new ProtocolVersion((byte) 2, (byte) 0);
        head.setVersion(version);
        
        assertEquals(version, head.getVersion());
        assertEquals(2, head.getVersion().getMajor());
        assertEquals(0, head.getVersion().getMinor());
    }

    @Test
    @DisplayName("设置和获取上级平台ID")
    void testSuperPlatformId() {
        MessageHead head = new MessageHead();
        head.setSuperPlatformId(9999L);
        
        assertEquals(9999L, head.getSuperPlatformId());
    }

    @Test
    @DisplayName("设置和获取加密方式")
    void testEncrypt() {
        MessageHead head = new MessageHead();
        head.setEncrypt((byte) 1);
        
        assertEquals(1, head.getEncrypt());
    }

    @Test
    @DisplayName("设置和获取加密密钥")
    void testEncryptKey() {
        MessageHead head = new MessageHead();
        head.setEncryptKey(12345678L);
        
        assertEquals(12345678L, head.getEncryptKey());
    }

    @Test
    @DisplayName("边界值测试-最大消息长度")
    void testMaxMsgLength() {
        MessageHead head = new MessageHead();
        head.setMsgLength(Integer.MAX_VALUE);
        
        assertEquals(Integer.MAX_VALUE, head.getMsgLength());
    }

    @Test
    @DisplayName("边界值测试-最大平台ID")
    void testMaxPlatformId() {
        MessageHead head = new MessageHead();
        head.setPlatformId(Long.MAX_VALUE);
        
        assertEquals(Long.MAX_VALUE, head.getPlatformId());
    }
}
