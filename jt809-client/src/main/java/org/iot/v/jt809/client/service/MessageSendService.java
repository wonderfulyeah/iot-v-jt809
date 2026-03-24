package org.iot.v.jt809.client.service;

import io.netty.channel.Channel;
import org.iot.v.jt809.core.message.base.BaseMessage;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

/**
 * 消息发送服务接口
 *
 * @author haye
 * @date 2026-03-24
 */
public interface MessageSendService {
    
    /**
     * 发送消息（异步）
     *
     * @param message 消息
     * @return 发送结果
     */
    CompletableFuture<Void> sendAsync(BaseMessage message);
    
    /**
     * 发送消息（同步）
     *
     * @param message 消息
     * @throws Exception 发送异常
     */
    void send(BaseMessage message) throws Exception;
    
    /**
     * 发送消息并等待响应
     *
     * @param message   消息
     * @param timeout   超时时间
     * @param unit      时间单位
     * @param respClass 响应消息类型
     * @param <T>       响应类型
     * @return 响应消息
     */
    <T extends BaseMessage> CompletableFuture<T> sendAndWait(
            BaseMessage message, 
            long timeout, 
            TimeUnit unit, 
            Class<T> respClass);
    
    /**
     * 检查通道是否可用
     *
     * @return true-可用, false-不可用
     */
    boolean isChannelAvailable();
    
    /**
     * 获取当前通道
     *
     * @return 通道
     */
    Channel getChannel();
}
