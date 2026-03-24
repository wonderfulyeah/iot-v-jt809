package org.iot.v.jt809.core.util;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * BCDUtil 单元测试
 *
 * @author haye
 * @date 2026-03-24
 */
@DisplayName("BCD编解码工具测试")
class BCDUtilTest {

    @Test
    @DisplayName("编码空字符串")
    void testEncodeEmptyString() {
        byte[] result = BCDUtil.encode("");
        assertArrayEquals(new byte[0], result, "空字符串应返回空数组");
    }

    @Test
    @DisplayName("编码null字符串")
    void testEncodeNullString() {
        byte[] result = BCDUtil.encode((String) null);
        assertArrayEquals(new byte[0], result, "null字符串应返回空数组");
    }

    @Test
    @DisplayName("编码偶数长度字符串")
    void testEncodeEvenLengthString() {
        byte[] result = BCDUtil.encode("123456");
        
        // "123456" -> 0x12 0x34 0x56
        assertArrayEquals(new byte[]{0x12, 0x34, 0x56}, result);
    }

    @Test
    @DisplayName("编码奇数长度字符串")
    void testEncodeOddLengthString() {
        byte[] result = BCDUtil.encode("12345");
        
        // "12345" -> 高位优先编码
        // 第1个字符'1'放到高4位，'2'放到低4位 -> 0x12
        // 第3个字符'3'放到高4位，'4'放到低4位 -> 0x34
        // 第5个字符'5'放到高4位，低4位为0 -> 0x50
        assertEquals(3, result.length);
        assertEquals(0x12, result[0] & 0xFF);
        assertEquals(0x34, result[1] & 0xFF);
        assertEquals(0x50, result[2] & 0xFF);
    }

    @Test
    @DisplayName("编码字符串到指定长度")
    void testEncodeWithLength() {
        byte[] result = BCDUtil.encode("123456", 4);
        
        assertEquals(4, result.length);
        assertEquals(0x12, result[0]);
        assertEquals(0x34, result[1]);
        assertEquals(0x56, result[2]);
        assertEquals(0x00, result[3]);
    }

    @Test
    @DisplayName("编码long值")
    void testEncodeLong() {
        byte[] result = BCDUtil.encode(123456L, 4);
        
        assertEquals(4, result.length);
        // "123456" -> 0x12 0x34 0x56 0x00
        assertEquals(0x12, result[0]);
        assertEquals(0x34, result[1]);
        assertEquals(0x56, result[2]);
    }

    @Test
    @DisplayName("解码BCD到字符串")
    void testDecodeToString() {
        byte[] data = {0x12, 0x34, 0x56};
        String result = BCDUtil.decode(data);
        
        assertEquals("123456", result);
    }

    @Test
    @DisplayName("解码空数组")
    void testDecodeEmptyArray() {
        String result = BCDUtil.decode(new byte[0]);
        assertEquals("", result);
    }

    @Test
    @DisplayName("解码null数组")
    void testDecodeNullArray() {
        String result = BCDUtil.decode(null);
        assertEquals("", result);
    }

    @Test
    @DisplayName("解码BCD到long")
    void testDecodeToLong() {
        byte[] data = {0x12, 0x34, 0x56};
        long result = BCDUtil.decodeToLong(data);
        
        assertEquals(123456L, result);
    }

    @Test
    @DisplayName("编解码往返测试")
    void testEncodeDecodeRoundTrip() {
        String original = "12345678901234567890";
        
        byte[] encoded = BCDUtil.encode(original);
        String decoded = BCDUtil.decode(encoded);
        
        assertEquals(original, decoded, "编解码往返应保持一致");
    }

    @Test
    @DisplayName("从ByteBuf读取BCD字符串")
    void testReadStringFromByteBuf() {
        ByteBuf buf = Unpooled.buffer();
        buf.writeBytes(new byte[]{0x12, 0x34, 0x56});
        
        String result = BCDUtil.readString(buf, 3);
        assertEquals("123456", result);
        
        buf.release();
    }

    @Test
    @DisplayName("从ByteBuf读取BCD long值")
    void testReadLongFromByteBuf() {
        ByteBuf buf = Unpooled.buffer();
        buf.writeBytes(new byte[]{0x12, 0x34, 0x56});
        
        long result = BCDUtil.readLong(buf, 3);
        assertEquals(123456L, result);
        
        buf.release();
    }

    @Test
    @DisplayName("写入BCD字符串到ByteBuf")
    void testWriteStringToByteBuf() {
        ByteBuf buf = Unpooled.buffer();
        
        BCDUtil.writeString(buf, "123456", 4);
        
        assertEquals(4, buf.readableBytes());
        assertEquals(0x12, buf.readByte());
        assertEquals(0x34, buf.readByte());
        assertEquals(0x56, buf.readByte());
        assertEquals(0x00, buf.readByte());
        
        buf.release();
    }

    @Test
    @DisplayName("写入BCD long值到ByteBuf")
    void testWriteLongToByteBuf() {
        ByteBuf buf = Unpooled.buffer();
        
        BCDUtil.writeLong(buf, 123456L, 4);
        
        assertEquals(4, buf.readableBytes());
        assertEquals(0x12, buf.readByte());
        assertEquals(0x34, buf.readByte());
        assertEquals(0x56, buf.readByte());
        assertEquals(0x00, buf.readByte());
        
        buf.release();
    }

    @Test
    @DisplayName("无效字符抛出异常")
    void testInvalidCharacter() {
        assertThrows(IllegalArgumentException.class, () -> {
            BCDUtil.encode("12A456");
        });
    }

    @Test
    @DisplayName("别名方法encodeBCD测试")
    void testEncodeBCDAlias() {
        byte[] result = BCDUtil.encodeBCD("123456", 4);
        assertEquals(4, result.length);
    }

    @Test
    @DisplayName("别名方法decodeBCD测试")
    void testDecodeBCDAlias() {
        byte[] data = {0x12, 0x34, 0x56};
        String result = BCDUtil.decodeBCD(data);
        assertEquals("123456", result);
    }
}
