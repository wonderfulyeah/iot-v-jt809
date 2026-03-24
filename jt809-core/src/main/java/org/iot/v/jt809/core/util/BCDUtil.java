package org.iot.v.jt809.core.util;

import io.netty.buffer.ByteBuf;

/**
 * BCD码编码工具类
 * BCD码使用压缩格式，每个字节表示两位十进制数
 *
 * @author haye
 * @date 2026-03-24
 */
public class BCDUtil {
    
    private BCDUtil() {
        throw new UnsupportedOperationException("Utility class");
    }
    
    /**
     * 字符串编码为BCD码
     * 例如: "123456" -> 0x12 0x34 0x56
     *
     * @param str 字符串
     * @return BCD编码字节数组
     */
    public static byte[] encode(String str) {
        if (str == null || str.isEmpty()) {
            return new byte[0];
        }
        
        int len = str.length();
        int byteLen = (len + 1) / 2;
        byte[] result = new byte[byteLen];
        
        for (int i = 0; i < len; i++) {
            char c = str.charAt(i);
            int digit = Character.digit(c, 10);
            
            if (digit < 0) {
                throw new IllegalArgumentException("Invalid BCD character: " + c);
            }
            
            if (i % 2 == 0) {
                result[i / 2] = (byte) (digit << 4);
            } else {
                result[i / 2] |= digit;
            }
        }
        
        // 如果长度为奇数，最后一个字节的高4位已经设置，低4位为0
        if (len % 2 != 0) {
            // 已经正确处理，无需额外操作
        }
        
        return result;
    }
    
    /**
     * 将字符串编码为指定长度的BCD码（右侧补零）
     *
     * @param str 字符串
     * @param length 目标字节数组长度
     * @return BCD编码字节数组
     */
    public static byte[] encode(String str, int length) {
        if (str == null) {
            str = "";
        }
        
        byte[] result = new byte[length];
        byte[] encoded = encode(str);
        
        // 左对齐，右侧补零
        int copyLen = Math.min(encoded.length, length);
        System.arraycopy(encoded, 0, result, 0, copyLen);
        
        return result;
    }
    
    /**
     * 将long类型数字编码为BCD码（指定字节数）
     *
     * @param value 数字值
     * @param length 字节数组长度
     * @return BCD编码字节数组
     */
    public static byte[] encode(long value, int length) {
        return encode(String.valueOf(value), length);
    }
    
    /**
     * BCD码解码为字符串
     *
     * @param data BCD编码字节数组
     * @return 字符串
     */
    public static String decode(byte[] data) {
        if (data == null || data.length == 0) {
            return "";
        }
        
        StringBuilder sb = new StringBuilder(data.length * 2);
        
        for (byte b : data) {
            int high = (b >> 4) & 0x0F;
            int low = b & 0x0F;
            
            sb.append(high);
            
            // 低4位如果为0x0F，表示未使用的位（某些协议规范）
            if (low != 0x0F) {
                sb.append(low);
            }
        }
        
        return sb.toString();
    }
    
    /**
     * BCD码解码为long类型
     *
     * @param data BCD编码字节数组
     * @return long值
     */
    public static long decodeToLong(byte[] data) {
        String str = decode(data);
        if (str.isEmpty()) {
            return 0L;
        }
        
        try {
            return Long.parseLong(str);
        } catch (NumberFormatException e) {
            return 0L;
        }
    }
    
    /**
     * 从ByteBuf读取BCD码并解码为字符串
     *
     * @param buf ByteBuf
     * @param length 读取长度
     * @return 字符串
     */
    public static String readString(ByteBuf buf, int length) {
        byte[] data = new byte[length];
        buf.readBytes(data);
        return decode(data);
    }
    
    /**
     * 从ByteBuf读取BCD码并解码为long
     *
     * @param buf ByteBuf
     * @param length 读取长度
     * @return long值
     */
    public static long readLong(ByteBuf buf, int length) {
        byte[] data = new byte[length];
        buf.readBytes(data);
        return decodeToLong(data);
    }
    
    /**
     * 将字符串编码为BCD码并写入ByteBuf
     *
     * @param buf ByteBuf
     * @param str 字符串
     * @param length 写入长度
     */
    public static void writeString(ByteBuf buf, String str, int length) {
        byte[] data = encode(str, length);
        buf.writeBytes(data);
    }
    
    /**
     * 将long值编码为BCD码并写入ByteBuf
     *
     * @param buf ByteBuf
     * @param value long值
     * @param length 写入长度
     */
    public static void writeLong(ByteBuf buf, long value, int length) {
        byte[] data = encode(value, length);
        buf.writeBytes(data);
    }
    
    // ==================== 别名方法（兼容性） ====================
    
    /**
     * 编码BCD（别名方法）
     *
     * @param str 字符串
     * @param length 目标长度
     * @return BCD编码字节数组
     */
    public static byte[] encodeBCD(String str, int length) {
        return encode(str, length);
    }
    
    /**
     * 解码BCD（别名方法）
     *
     * @param data BCD编码字节数组
     * @return 字符串
     */
    public static String decodeBCD(byte[] data) {
        return decode(data);
    }
}
