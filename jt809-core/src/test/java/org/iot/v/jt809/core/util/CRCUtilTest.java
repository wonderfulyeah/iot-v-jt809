package org.iot.v.jt809.core.util;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * CRCUtil 单元测试
 *
 * @author haye
 * @date 2026-03-24
 */
@DisplayName("CRC校验工具测试")
class CRCUtilTest {

    @Test
    @DisplayName("计算空数据的CRC")
    void testCalculateEmptyData() {
        byte[] data = new byte[0];
        int crc = CRCUtil.calculate(data);
        assertEquals(0xFFFF, crc, "空数据的CRC应为0xFFFF");
    }

    @Test
    @DisplayName("计算单字节数据的CRC")
    void testCalculateSingleByte() {
        byte[] data = {0x01};
        int crc = CRCUtil.calculate(data);
        assertTrue(crc >= 0 && crc <= 0xFFFF, "CRC应在有效范围内");
    }

    @Test
    @DisplayName("计算多字节数据的CRC")
    void testCalculateMultiBytes() {
        byte[] data = {0x01, 0x02, 0x03, 0x04, 0x05};
        int crc = CRCUtil.calculate(data);
        
        // 验证CRC是16位值
        assertTrue(crc >= 0 && crc <= 0xFFFF);
        
        // 相同数据应该产生相同的CRC
        int crc2 = CRCUtil.calculate(data);
        assertEquals(crc, crc2, "相同数据的CRC应相同");
    }

    @Test
    @DisplayName("计算ByteBuf的CRC")
    void testCalculateByteBuf() {
        byte[] data = {0x01, 0x02, 0x03, 0x04, 0x05};
        ByteBuf buf = Unpooled.wrappedBuffer(data);
        
        int crcFromBytes = CRCUtil.calculate(data);
        int crcFromBuf = CRCUtil.calculate(buf);
        
        assertEquals(crcFromBytes, crcFromBuf, "ByteBuf和字节数组的CRC应相同");
        buf.release();
    }

    @Test
    @DisplayName("CRC验证-正确校验")
    void testVerifySuccess() {
        byte[] data = {0x01, 0x02, 0x03, 0x04, 0x05};
        int crc = CRCUtil.calculate(data);
        
        assertTrue(CRCUtil.verify(data, crc), "正确的CRC验证应通过");
    }

    @Test
    @DisplayName("CRC验证-错误校验")
    void testVerifyFailure() {
        byte[] data = {0x01, 0x02, 0x03, 0x04, 0x05};
        int wrongCrc = 0x1234;
        
        assertFalse(CRCUtil.verify(data, wrongCrc), "错误的CRC验证应失败");
    }

    @Test
    @DisplayName("CRC验证-ByteBuf")
    void testVerifyByteBuf() {
        byte[] data = {0x01, 0x02, 0x03, 0x04, 0x05};
        ByteBuf buf = Unpooled.wrappedBuffer(data);
        int crc = CRCUtil.calculate(data);
        
        assertTrue(CRCUtil.verify(buf, crc), "ByteBuf的CRC验证应通过");
        buf.release();
    }

    @Test
    @DisplayName("不同数据产生不同CRC")
    void testDifferentDataDifferentCrc() {
        byte[] data1 = {0x01, 0x02, 0x03};
        byte[] data2 = {0x01, 0x02, 0x04};
        
        int crc1 = CRCUtil.calculate(data1);
        int crc2 = CRCUtil.calculate(data2);
        
        assertNotEquals(crc1, crc2, "不同数据应产生不同的CRC");
    }
}
