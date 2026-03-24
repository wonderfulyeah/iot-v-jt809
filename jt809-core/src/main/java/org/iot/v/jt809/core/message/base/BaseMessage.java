package org.iot.v.jt809.core.message.base;

import lombok.Data;
import org.iot.v.jt809.core.util.SequenceGenerator;

/**
 * 消息基类
 * 所有JT809消息都需要继承此类
 *
 * @author haye
 * @date 2026-03-24
 */
@Data
public abstract class BaseMessage {
    
    /**
     * 消息头
     */
    private MessageHead head;
    
    /**
     * 消息体
     */
    private MessageBody body;
    
    public BaseMessage() {
        this.head = new MessageHead();
        // 自动生成流水号
        this.head.setMsgSn(SequenceGenerator.getInstance().next());
    }
    
    /**
     * 获取消息ID
     *
     * @return 消息ID
     */
    public int getMsgId() {
        return head.getMsgId();
    }
    
    /**
     * 设置消息ID
     *
     * @param msgId 消息ID
     */
    public void setMsgId(int msgId) {
        head.setMsgId(msgId);
    }
    
    /**
     * 获取消息流水号
     *
     * @return 流水号
     */
    public int getMsgSn() {
        return head.getMsgSn();
    }
    
    /**
     * 获取消息类型名称
     *
     * @return 消息类型名称
     */
    public abstract String getMessageTypeName();
}
