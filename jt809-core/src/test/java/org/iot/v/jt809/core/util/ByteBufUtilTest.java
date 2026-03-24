package org.iot.v.jt809.core.util;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * ByteBufUtil 单元测试
 *
 * @author haye
 * @date 2026-03-24
 */
@DisplayName("ByteBuf工具测试")
class ByteBufUtilTest {

    @Test
    @DisplayName("字节数组转十六进制字符串")
    void testBytesToHex() {
        byte[] bytes = {0x12, 0x34, (byte) 0xAB, (byte) 0xCD};
        String hex = ByteBufUtil.bytesToHex(bytes);
        
        assertEquals("12 34 AB CD", hex);
    }

    @Test
    @DisplayName("空字节数组转十六进制字符串")
    void testBytesToHexEmpty() {
        String hex = ByteBufUtil.bytesToHex(new byte[0]);
        assertEquals("", hex);
    }

    @Test
    @DisplayName("null字节数组转十六进制字符串")
    void testBytesToHexNull() {
        String hex = ByteBufUtil.bytesToHex(null);
        assertEquals("", hex);
    }

    @Test
    @DisplayName("ByteBuf转十六进制字符串")
    void testByteBufToHex() {
        ByteBuf buf = Unpooled.wrappedBuffer(new byte[]{0x12, 0x34, (byte) 0xAB});
        String hex = ByteBufUtil.byteBufToHex(buf);
        
        assertEquals("12 34 AB", hex);
        buf.release();
    }

    @Test
    @DisplayName("空ByteBuf转十六进制字符串")
    void testByteBufToHexEmpty() {
        ByteBuf buf = Unpooled.EMPTY_BUFFER;
        String hex = ByteBufUtil.byteBufToHex(buf);
        assertEquals("", hex);
    }

    @Test
    @DisplayName("十六进制字符串转字节数组")
    void testHexToBytes() {
        String hex = "12 34 AB CD";
        byte[] bytes = ByteBufUtil.hexToBytes(hex);
        
        assertArrayEquals(new byte[]{0x12, 0x34, (byte) 0xAB, (byte) 0xCD}, bytes);
    }

    @Test
    @DisplayName("十六进制字符串转字节数组-无空格")
    void testHexToBytesNoSpace() {
        String hex = "1234";
        byte[] bytes = ByteBufUtil.hexToBytes(hex);
        
        // 无空格时会被当作一个整体
        assertEquals(1, bytes.length);
    }

    @Test
    @DisplayName("空十六进制字符串转字节数组")
    void testHexToBytesEmpty() {
        byte[] bytes = ByteBufUtil.hexToBytes("");
        assertArrayEquals(new byte[0], bytes);
    }

    @Test
    @DisplayName("null十六进制字符串转字节数组")
    void testHexToBytesNull() {
        byte[] bytes = ByteBufUtil.hexToBytes(null);
        assertArrayEquals(new byte[0], bytes);
    }

    @Test
    @DisplayName("从ByteBuf读取字符串")
    void testReadString() {
        ByteBuf buf = Unpooled.buffer();
        buf.writeBytes("Hello".getBytes());
        buf.writeZero(5); // 补零
        
        String str = ByteBufUtil.readString(buf, 10);
        assertEquals("Hello", str);
        
        buf.release();
    }

    @Test
    @DisplayName("从ByteBuf读取字符串-无尾部空字节")
    void testReadStringNoTrailingZero() {
        ByteBuf buf = Unpooled.buffer();
        buf.writeBytes("HelloWorld".getBytes());
        
        String str = ByteBufUtil.readString(buf, 10);
        assertEquals("HelloWorld", str);
        
        buf.release();
    }

    @Test
    @DisplayName("写入字符串到ByteBuf")
    void testWriteString() {
        ByteBuf buf = Unpooled.buffer();
        
        ByteBufUtil.writeString(buf, "Hello", 10);
        
        assertEquals(10, buf.readableBytes());
        
        byte[] bytes = new byte[10];
        buf.readBytes(bytes);
        
        assertEquals("Hello", new String(bytes, 0, 5).trim());
        
        buf.release();
    }

    @Test
    @DisplayName("写入null字符串到ByteBuf")
    void testWriteNullString() {
        ByteBuf buf = Unpooled.buffer();
        
        ByteBufUtil.writeString(buf, null, 5);
        
        assertEquals(5, buf.readableBytes());
        // 应该全是零
        for (int i = 0; i < 5; i++) {
            assertEquals(0, buf.readByte());
        }
        
        buf.release();
    }

    @Test
    @DisplayName("复制ByteBuf到数组")
    void testCopyToArray() {
        ByteBuf buf = Unpooled.wrappedBuffer(new byte[]{1, 2, 3, 4, 5});
        
        byte[] array = ByteBufUtil.copyToArray(buf);
        
        assertArrayEquals(new byte[]{1, 2, 3, 4, 5}, array);
        buf.release();
    }

    @Test
    @DisplayName("复制空ByteBuf到数组")
    void testCopyToArrayEmpty() {
        ByteBuf buf = Unpooled.EMPTY_BUFFER;
        
        byte[] array = ByteBufUtil.copyToArray(buf);
        
        assertArrayEquals(new byte[0], array);
    }

    @Test
    @DisplayName("复制null ByteBuf到数组")
    void testCopyToArrayNull() {
        byte[] array = ByteBufUtil.copyToArray(null);
        assertArrayEquals(new byte[0], array);
    }

    @Test
    @DisplayName("包装字节数组为ByteBuf")
    void testWrap() {
        byte[] bytes = {1, 2, 3, 4, 5};
        
        ByteBuf buf = ByteBufUtil.wrap(bytes);
        
        assertEquals(5, buf.readableBytes());
        assertEquals(1, buf.readByte());
        
        buf.release();
    }

    @Test
    @DisplayName("比较相等的ByteBuf")
    void testEqualsSame() {
        ByteBuf buf1 = Unpooled.wrappedBuffer(new byte[]{1, 2, 3});
        ByteBuf buf2 = Unpooled.wrappedBuffer(new byte[]{1, 2, 3});
        
        assertTrue(ByteBufUtil.equals(buf1, buf2));
        
        buf1.release();
        buf2.release();
    }

    @Test
    @DisplayName("比较不相等的ByteBuf")
    void testEqualsDifferent() {
        ByteBuf buf1 = Unpooled.wrappedBuffer(new byte[]{1, 2, 3});
        ByteBuf buf2 = Unpooled.wrappedBuffer(new byte[]{1, 2, 4});
        
        assertFalse(ByteBufUtil.equals(buf1, buf2));
        
        buf1.release();
        buf2.release();
    }

    @Test
    @DisplayName("比较null ByteBuf")
    void testEqualsNull() {
        ByteBuf buf = Unpooled.wrappedBuffer(new byte[]{1, 2, 3});
        
        assertFalse(ByteBufUtil.equals(buf, null));
        assertFalse(ByteBufUtil.equals(null, buf));
        assertTrue(ByteBufUtil.equals(null, null));
        
        buf.release();
    }

    @Test
    @DisplayName("比较同一个ByteBuf实例")
    void testEqualsSameInstance() {
        ByteBuf buf = Unpooled.wrappedBuffer(new byte[]{1, 2, 3});
        
        assertTrue(ByteBufUtil.equals(buf, buf));
        
        buf.release();
    }
}
