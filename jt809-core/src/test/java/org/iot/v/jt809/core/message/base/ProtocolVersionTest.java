package org.iot.v.jt809.core.message.base;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * ProtocolVersion 单元测试
 *
 * @author haye
 * @date 2026-03-24
 */
@DisplayName("协议版本测试")
class ProtocolVersionTest {

    @Test
    @DisplayName("创建默认版本")
    void testCreateDefault() {
        ProtocolVersion version = new ProtocolVersion();
        
        assertEquals(1, version.getMajor());
        assertEquals(0, version.getMinor());
    }

    @Test
    @DisplayName("创建指定版本")
    void testCreateWithVersion() {
        ProtocolVersion version = new ProtocolVersion((byte) 2, (byte) 5, (byte) 0);
        
        assertEquals(2, version.getMajor());
        assertEquals(5, version.getMinor());
    }

    @Test
    @DisplayName("获取版本字符串")
    void testGetVersion() {
        ProtocolVersion version = new ProtocolVersion((byte) 1, (byte) 0, (byte) 0);
        
        assertEquals("1.0", version.getVersion());
    }

    @Test
    @DisplayName("解析版本字符串")
    void testParseVersion() {
        ProtocolVersion version = ProtocolVersion.parse("2.5");
        
        assertEquals(2, version.getMajor());
        assertEquals(5, version.getMinor());
    }

    @Test
    @DisplayName("解析空版本字符串")
    void testParseEmptyVersion() {
        ProtocolVersion version = ProtocolVersion.parse("");
        
        assertEquals(1, version.getMajor());
        assertEquals(0, version.getMinor());
    }

    @Test
    @DisplayName("解析null版本字符串")
    void testParseNullVersion() {
        ProtocolVersion version = ProtocolVersion.parse(null);
        
        assertEquals(1, version.getMajor());
        assertEquals(0, version.getMinor());
    }

    @Test
    @DisplayName("解析无效版本字符串")
    void testParseInvalidVersion() {
        ProtocolVersion version = ProtocolVersion.parse("invalid");
        
        // 无效格式应返回默认版本
        assertEquals(1, version.getMajor());
        assertEquals(0, version.getMinor());
    }

    @Test
    @DisplayName("解析单部分版本字符串")
    void testParseSinglePartVersion() {
        ProtocolVersion version = ProtocolVersion.parse("5");
        
        // 单部分格式应返回默认版本
        assertEquals(1, version.getMajor());
        assertEquals(0, version.getMinor());
    }

    @Test
    @DisplayName("版本相等性比较")
    void testEquals() {
        ProtocolVersion v1 = new ProtocolVersion((byte) 1, (byte) 0, (byte) 0);
        ProtocolVersion v2 = new ProtocolVersion((byte) 1, (byte) 0, (byte) 0);
        ProtocolVersion v3 = new ProtocolVersion((byte) 2, (byte) 0, (byte) 0);
        
        assertEquals(v1, v2);
        assertNotEquals(v1, v3);
    }

    @Test
    @DisplayName("版本哈希码")
    void testHashCode() {
        ProtocolVersion v1 = new ProtocolVersion((byte) 1, (byte) 0, (byte) 0);
        ProtocolVersion v2 = new ProtocolVersion((byte) 1, (byte) 0, (byte) 0);
        
        assertEquals(v1.hashCode(), v2.hashCode());
    }
}
