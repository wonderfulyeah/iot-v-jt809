package org.iot.v.jt809.core.message.base;

import lombok.Data;
import org.iot.v.jt809.core.constant.JT809Constant;

import java.time.Instant;

/**
 * 消息头
 *
 * @author haye
 * @date 2026-03-24
 */
@Data
public class MessageHead {
    
    /**
     * 消息长度（包括消息头、消息体、CRC校验）
     */
    private int msgLength;
    
    /**
     * 消息流水号
     */
    private int msgSn;
    
    /**
     * 消息ID
     */
    private int msgId;
    
    /**
     * 下级平台接入码（4字节 UINT32）
     */
    private long platformId;
    
    /**
     * 协议版本号
     */
    private ProtocolVersion version = new ProtocolVersion();
    
    /**
     * 加密方式
     * 0: 不加密
     * 1: 加密
     */
    private byte encrypt = JT809Constant.ENCRYPT_NONE;
    
    /**
     * 加密密钥（不加密时为0）
     */
    private long encryptKey;

    /**
     * 消息时间
     */
    private Instant time;
}
