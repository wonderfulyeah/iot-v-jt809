package org.iot.v.jt809.core.util;

import io.netty.buffer.ByteBuf;

/**
 * CRC校验工具类
 * 使用CRC-CCITT (XModem)算法
 * 多项式: 0x1021
 * 初始值: 0xFFFF
 *
 * @author haye
 * @date 2026-03-24
 */
public class CRCUtil {
    
    private CRCUtil() {
        throw new UnsupportedOperationException("Utility class");
    }
    
    /**
     * 计算CRC校验码
     *
     * @param data 数据字节数组
     * @return CRC校验码
     */
    public static int calculate(byte[] data) {
        int crc = 0xFFFF;
        for (byte b : data) {
            crc ^= (b & 0xFF) << 8;
            for (int i = 0; i < 8; i++) {
                if ((crc & 0x8000) != 0) {
                    crc = (crc << 1) ^ 0x1021;
                } else {
                    crc <<= 1;
                }
            }
        }
        return crc & 0xFFFF;
    }
    
    /**
     * 计算ByteBuf的CRC校验码
     *
     * @param buf ByteBuf数据
     * @return CRC校验码
     */
    public static int calculate(ByteBuf buf) {
        int crc = 0xFFFF;
        int readerIndex = buf.readerIndex();
        int writerIndex = buf.writerIndex();
        
        for (int i = readerIndex; i < writerIndex; i++) {
            byte b = buf.getByte(i);
            crc ^= (b & 0xFF) << 8;
            for (int j = 0; j < 8; j++) {
                if ((crc & 0x8000) != 0) {
                    crc = (crc << 1) ^ 0x1021;
                } else {
                    crc <<= 1;
                }
            }
        }
        
        return crc & 0xFFFF;
    }
    
    /**
     * 校验数据的CRC是否正确
     *
     * @param data 数据字节数组
     * @param expectedCrc 期望的CRC值
     * @return true-校验通过, false-校验失败
     */
    public static boolean verify(byte[] data, int expectedCrc) {
        return calculate(data) == expectedCrc;
    }
    
    /**
     * 校验ByteBuf的CRC是否正确
     *
     * @param buf ByteBuf数据
     * @param expectedCrc 期望的CRC值
     * @return true-校验通过, false-校验失败
     */
    public static boolean verify(ByteBuf buf, int expectedCrc) {
        return calculate(buf) == expectedCrc;
    }
}
