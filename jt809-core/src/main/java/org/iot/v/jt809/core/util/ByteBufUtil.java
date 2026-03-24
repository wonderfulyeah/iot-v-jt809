package org.iot.v.jt809.core.util;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

/**
 * ByteBuf工具类
 *
 * @author haye
 * @date 2026-03-24
 */
public class ByteBufUtil {
    
    private ByteBufUtil() {
        throw new UnsupportedOperationException("Utility class");
    }
    
    /**
     * 将字节数组转换为十六进制字符串
     *
     * @param bytes 字节数组
     * @return 十六进制字符串
     */
    public static String bytesToHex(byte[] bytes) {
        if (bytes == null || bytes.length == 0) {
            return "";
        }
        
        StringBuilder sb = new StringBuilder(bytes.length * 3);
        for (byte b : bytes) {
            sb.append(String.format("%02X ", b));
        }
        return sb.toString().trim();
    }
    
    /**
     * 将ByteBuf转换为十六进制字符串
     *
     * @param buf ByteBuf
     * @return 十六进制字符串
     */
    public static String byteBufToHex(ByteBuf buf) {
        if (buf == null || !buf.isReadable()) {
            return "";
        }
        
        StringBuilder sb = new StringBuilder(buf.readableBytes() * 3);
        int readerIndex = buf.readerIndex();
        int writerIndex = buf.writerIndex();
        
        for (int i = readerIndex; i < writerIndex; i++) {
            sb.append(String.format("%02X ", buf.getByte(i)));
        }
        
        return sb.toString().trim();
    }
    
    /**
     * 将十六进制字符串转换为字节数组
     *
     * @param hex 十六进制字符串（空格分隔）
     * @return 字节数组
     */
    public static byte[] hexToBytes(String hex) {
        if (hex == null || hex.isEmpty()) {
            return new byte[0];
        }
        
        String[] hexArray = hex.trim().split("\\s+");
        byte[] bytes = new byte[hexArray.length];
        
        for (int i = 0; i < hexArray.length; i++) {
            bytes[i] = (byte) Integer.parseInt(hexArray[i], 16);
        }
        
        return bytes;
    }
    
    /**
     * 从ByteBuf读取固定长度字符串（去除尾部空字节）
     *
     * @param buf ByteBuf
     * @param length 读取长度
     * @return 字符串
     */
    public static String readString(ByteBuf buf, int length) {
        byte[] bytes = new byte[length];
        buf.readBytes(bytes);
        
        // 去除尾部的空字节
        int end = bytes.length;
        while (end > 0 && bytes[end - 1] == 0) {
            end--;
        }
        
        return new String(bytes, 0, end).trim();
    }
    
    /**
     * 将字符串写入ByteBuf（固定长度，不足补零）
     *
     * @param buf ByteBuf
     * @param str 字符串
     * @param length 写入长度
     */
    public static void writeString(ByteBuf buf, String str, int length) {
        byte[] bytes = new byte[length];
        if (str != null) {
            byte[] strBytes = str.getBytes();
            int copyLen = Math.min(strBytes.length, length);
            System.arraycopy(strBytes, 0, bytes, 0, copyLen);
        }
        buf.writeBytes(bytes);
    }
    
    /**
     * 复制ByteBuf的可读内容为新数组
     *
     * @param buf ByteBuf
     * @return 字节数组
     */
    public static byte[] copyToArray(ByteBuf buf) {
        if (buf == null || !buf.isReadable()) {
            return new byte[0];
        }
        
        byte[] bytes = new byte[buf.readableBytes()];
        buf.getBytes(buf.readerIndex(), bytes);
        return bytes;
    }
    
    /**
     * 创建ByteBuf并写入字节数组
     *
     * @param bytes 字节数组
     * @return ByteBuf
     */
    public static ByteBuf wrap(byte[] bytes) {
        return Unpooled.wrappedBuffer(bytes);
    }
    
    /**
     * 比较两个ByteBuf内容是否相同
     *
     * @param buf1 ByteBuf1
     * @param buf2 ByteBuf2
     * @return true-相同, false-不同
     */
    public static boolean equals(ByteBuf buf1, ByteBuf buf2) {
        if (buf1 == buf2) {
            return true;
        }
        
        if (buf1 == null || buf2 == null) {
            return false;
        }
        
        return buf1.equals(buf2);
    }
}
