package org.iot.v.jt809.core.constant;

/**
 * JT809协议常量定义
 *
 * @author haye
 * @date 2026-03-24
 */
public interface JT809Constant {
    
    /**
     * 消息起始标识
     */
    byte FLAG_START = 0x5B;
    
    /**
     * 消息结束标识
     */
    byte FLAG_END = 0x5D;
    
    /**
     * 转义字符
     */
    byte ESCAPE_CHAR = 0x5A;
    
    /**
     * 转义字符2
     */
    byte ESCAPE_CHAR_2 = 0x5E;
    
    /**
     * 消息头长度（包含消息长度字段）
     * 消息长度(4) + 流水号(4) + 消息ID(2) + 平台接入码(8) + 版本(2) + 上级平台(4) + 加密(1) + 密钥(4) = 29字节
     * JT809协议中，消息长度字段的值包含自身
     */
    int MESSAGE_HEAD_LENGTH = 29;
    
    /**
     * CRC校验长度
     */
    int CRC_LENGTH = 2;
    
    /**
     * 最小消息长度 = 起始符(1) + 消息头(22) + CRC(2) + 结束符(1)
     */
    int MIN_MESSAGE_LENGTH = 1 + MESSAGE_HEAD_LENGTH + CRC_LENGTH + 1;
    
    /**
     * 加密方式 - 不加密
     */
    byte ENCRYPT_NONE = 0;
    
    /**
     * 加密方式 - 加密
     */
    byte ENCRYPT_RSA = 1;
    
    /**
     * 协议版本 - 主版本
     */
    byte VERSION_MAJOR = 1;
    
    /**
     * 协议版本 - 次版本
     */
    byte VERSION_MINOR = 0;
    
    /**
     * 登录成功
     */
    byte LOGIN_SUCCESS = 0;
    
    /**
     * 登录失败 - IP地址不正确
     */
    byte LOGIN_FAIL_IP = 1;
    
    /**
     * 登录失败 - 接入码不正确
     */
    byte LOGIN_FAIL_CODE = 2;
    
    /**
     * 登录失败 - 用户不存在
     */
    byte LOGIN_FAIL_USER = 3;
    
    /**
     * 登录失败 - 密码错误
     */
    byte LOGIN_FAIL_PASSWORD = 4;
    
    /**
     * 登录失败 - 其他
     */
    byte LOGIN_FAIL_OTHER = 5;
    
    /**
     * 断开成功
     */
    byte DISCONNECT_SUCCESS = 0;
    
    /**
     * 断开失败
     */
    byte DISCONNECT_FAIL = 1;
}
