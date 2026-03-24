package org.iot.v.jt809.core.codec;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.iot.v.jt809.core.constant.JT809Constant;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * EscapeHandler 单元测试
 *
 * @author haye
 * @date 2026-03-24
 */
@DisplayName("转义处理器测试")
class EscapeHandlerTest {

    private EscapeHandler escapeHandler;

    @BeforeEach
    void setUp() {
        escapeHandler = new EscapeHandler();
    }

    @Test
    @DisplayName("转义0x5B")
    void testEscapeFlagStart() {
        byte[] data = {JT809Constant.FLAG_START}; // 0x5B
        byte[] escaped = escapeHandler.escape(data);
        
        // 0x5B -> 0x5A 0x01
        assertArrayEquals(new byte[]{JT809Constant.ESCAPE_CHAR, 0x01}, escaped);
    }

    @Test
    @DisplayName("转义0x5A")
    void testEscapeEscapeChar() {
        byte[] data = {JT809Constant.ESCAPE_CHAR}; // 0x5A
        byte[] escaped = escapeHandler.escape(data);
        
        // 0x5A -> 0x5A 0x02
        assertArrayEquals(new byte[]{JT809Constant.ESCAPE_CHAR, 0x02}, escaped);
    }

    @Test
    @DisplayName("转义0x5D")
    void testEscapeFlagEnd() {
        byte[] data = {JT809Constant.FLAG_END}; // 0x5D
        byte[] escaped = escapeHandler.escape(data);
        
        // 0x5D -> 0x5E 0x01
        assertArrayEquals(new byte[]{JT809Constant.ESCAPE_CHAR_2, 0x01}, escaped);
    }

    @Test
    @DisplayName("转义0x5E")
    void testEscapeEscapeChar2() {
        byte[] data = {JT809Constant.ESCAPE_CHAR_2}; // 0x5E
        byte[] escaped = escapeHandler.escape(data);
        
        // 0x5E -> 0x5E 0x02
        assertArrayEquals(new byte[]{JT809Constant.ESCAPE_CHAR_2, 0x02}, escaped);
    }

    @Test
    @DisplayName("转义普通数据")
    void testEscapeNormalData() {
        byte[] data = {0x01, 0x02, 0x03, 0x04};
        byte[] escaped = escapeHandler.escape(data);
        
        // 普通数据不变
        assertArrayEquals(data, escaped);
    }

    @Test
    @DisplayName("转义混合数据")
    void testEscapeMixedData() {
        // 混合包含所有特殊字节的数据
        byte[] data = {0x01, 0x5B, 0x02, 0x5A, 0x03, 0x5D, 0x04, 0x5E, 0x05};
        byte[] escaped = escapeHandler.escape(data);
        
        // 验证长度增加
        assertEquals(data.length + 4, escaped.length);
        
        // 验证各部分
        assertEquals(0x01, escaped[0]);
        assertEquals(0x5A, escaped[1]);
        assertEquals(0x01, escaped[2]);
        assertEquals(0x02, escaped[3]);
        assertEquals(0x5A, escaped[4]);
        assertEquals(0x02, escaped[5]);
        assertEquals(0x03, escaped[6]);
        assertEquals(0x5E, escaped[7]);
        assertEquals(0x01, escaped[8]);
        assertEquals(0x04, escaped[9]);
        assertEquals(0x5E, escaped[10]);
        assertEquals(0x02, escaped[11]);
        assertEquals(0x05, escaped[12]);
    }

    @Test
    @DisplayName("反转义0x5A 0x01")
    void testUnescapeFlagStart() {
        byte[] data = {JT809Constant.ESCAPE_CHAR, 0x01}; // 0x5A 0x01
        byte[] unescaped = escapeHandler.unescape(data);
        
        // 0x5A 0x01 -> 0x5B
        assertArrayEquals(new byte[]{JT809Constant.FLAG_START}, unescaped);
    }

    @Test
    @DisplayName("反转义0x5A 0x02")
    void testUnescapeEscapeChar() {
        byte[] data = {JT809Constant.ESCAPE_CHAR, 0x02}; // 0x5A 0x02
        byte[] unescaped = escapeHandler.unescape(data);
        
        // 0x5A 0x02 -> 0x5A
        assertArrayEquals(new byte[]{JT809Constant.ESCAPE_CHAR}, unescaped);
    }

    @Test
    @DisplayName("反转义0x5E 0x01")
    void testUnescapeFlagEnd() {
        byte[] data = {JT809Constant.ESCAPE_CHAR_2, 0x01}; // 0x5E 0x01
        byte[] unescaped = escapeHandler.unescape(data);
        
        // 0x5E 0x01 -> 0x5D
        assertArrayEquals(new byte[]{JT809Constant.FLAG_END}, unescaped);
    }

    @Test
    @DisplayName("反转义0x5E 0x02")
    void testUnescapeEscapeChar2() {
        byte[] data = {JT809Constant.ESCAPE_CHAR_2, 0x02}; // 0x5E 0x02
        byte[] unescaped = escapeHandler.unescape(data);
        
        // 0x5E 0x02 -> 0x5E
        assertArrayEquals(new byte[]{JT809Constant.ESCAPE_CHAR_2}, unescaped);
    }

    @Test
    @DisplayName("反转义普通数据")
    void testUnescapeNormalData() {
        byte[] data = {0x01, 0x02, 0x03, 0x04};
        byte[] unescaped = escapeHandler.unescape(data);
        
        // 普通数据不变
        assertArrayEquals(data, unescaped);
    }

    @Test
    @DisplayName("转义和反转义往返测试")
    void testEscapeUnescapeRoundTrip() {
        byte[] original = {0x01, 0x5B, 0x02, 0x5A, 0x03, 0x5D, 0x04, 0x5E, 0x05};
        
        byte[] escaped = escapeHandler.escape(original);
        byte[] unescaped = escapeHandler.unescape(escaped);
        
        assertArrayEquals(original, unescaped, "转义再反转义应恢复原始数据");
    }

    @Test
    @DisplayName("转义空数据")
    void testEscapeEmptyData() {
        byte[] data = new byte[0];
        byte[] escaped = escapeHandler.escape(data);
        
        assertArrayEquals(new byte[0], escaped);
    }

    @Test
    @DisplayName("反转义空数据")
    void testUnescapeEmptyData() {
        byte[] data = new byte[0];
        byte[] unescaped = escapeHandler.unescape(data);
        
        assertArrayEquals(new byte[0], unescaped);
    }

    @Test
    @DisplayName("转义null数据")
    void testEscapeNullData() {
        ByteBuf result = escapeHandler.escape((ByteBuf) null);
        assertFalse(result.isReadable());
    }

    @Test
    @DisplayName("反转义null数据")
    void testUnescapeNullData() {
        ByteBuf result = escapeHandler.unescape((ByteBuf) null);
        assertFalse(result.isReadable());
    }

    @Test
    @DisplayName("ByteBuf版本转义测试")
    void testEscapeByteBuf() {
        ByteBuf buf = Unpooled.wrappedBuffer(new byte[]{0x01, 0x5B, 0x02});
        ByteBuf escaped = escapeHandler.escape(buf);
        
        assertEquals(4, escaped.readableBytes());
        
        buf.release();
        escaped.release();
    }

    @Test
    @DisplayName("ByteBuf版本反转义测试")
    void testUnescapeByteBuf() {
        ByteBuf buf = Unpooled.wrappedBuffer(new byte[]{0x01, 0x5A, 0x01, 0x02});
        ByteBuf unescaped = escapeHandler.unescape(buf);
        
        assertEquals(3, unescaped.readableBytes());
        assertEquals(0x01, unescaped.readByte());
        assertEquals(0x5B, unescaped.readByte());
        assertEquals(0x02, unescaped.readByte());
        
        buf.release();
        unescaped.release();
    }

    @Test
    @DisplayName("无效转义序列处理")
    void testInvalidEscapeSequence() {
        // 0x5A 后跟非标准字节
        byte[] data = {0x5A, 0x03}; // 0x03 不是有效的转义后缀
        byte[] unescaped = escapeHandler.unescape(data);
        
        // 应该保留原字节
        assertEquals(2, unescaped.length);
        assertEquals(0x5A, unescaped[0]);
        assertEquals(0x03, unescaped[1]);
    }

    @Test
    @DisplayName("末尾单独转义字符处理")
    void testTrailingEscapeChar() {
        byte[] data = {0x01, 0x5A}; // 末尾单独的转义字符
        byte[] unescaped = escapeHandler.unescape(data);
        
        assertEquals(2, unescaped.length);
        assertEquals(0x01, unescaped[0]);
        assertEquals(0x5A, unescaped[1]);
    }
}
