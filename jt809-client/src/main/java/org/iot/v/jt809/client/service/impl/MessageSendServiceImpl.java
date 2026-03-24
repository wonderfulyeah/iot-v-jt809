package org.iot.v.jt809.client.service.impl;

import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import lombok.extern.slf4j.Slf4j;
import org.iot.v.jt809.client.service.MessageSendService;
import org.iot.v.jt809.core.message.base.BaseMessage;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * 消息发送服务实现
 *
 * @author haye
 * @date 2026-03-24
 */
@Slf4j
public class MessageSendServiceImpl implements MessageSendService {
    
    private Channel channel;
    
    /**
     * 等待响应的请求映射 (msgSn -> future)
     */
    private final Map<Integer, CompletableFuture<BaseMessage>> pendingRequests = new ConcurrentHashMap<>();
    
    /**
     * 超时调度器
     */
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
    
    /**
     * 默认超时时间（秒）
     */
    private static final int DEFAULT_TIMEOUT = 10;
    
    /**
     * 设置通道
     */
    public void setChannel(Channel channel) {
        this.channel = channel;
    }
    
    @Override
    public CompletableFuture<Void> sendAsync(BaseMessage message) {
        if (!isChannelAvailable()) {
            CompletableFuture<Void> future = new CompletableFuture<>();
            future.completeExceptionally(new IllegalStateException("Channel not available"));
            return future;
        }
        
        CompletableFuture<Void> future = new CompletableFuture<>();
        channel.writeAndFlush(message).addListener((ChannelFuture f) -> {
            if (f.isSuccess()) {
                future.complete(null);
                log.debug("Message sent: msgId=0x{}", Integer.toHexString(message.getMsgId()));
            } else {
                future.completeExceptionally(f.cause());
                log.error("Failed to send message", f.cause());
            }
        });
        
        return future;
    }
    
    @Override
    public void send(BaseMessage message) throws Exception {
        sendAsync(message).get(DEFAULT_TIMEOUT, TimeUnit.SECONDS);
    }
    
    @Override
    @SuppressWarnings("unchecked")
    public <T extends BaseMessage> CompletableFuture<T> sendAndWait(
            BaseMessage message, 
            long timeout, 
            TimeUnit unit, 
            Class<T> respClass) {
        
        if (!isChannelAvailable()) {
            CompletableFuture<T> future = new CompletableFuture<>();
            future.completeExceptionally(new IllegalStateException("Channel not available"));
            return future;
        }
        
        // 创建响应等待的Future
        CompletableFuture<BaseMessage> responseFuture = new CompletableFuture<>();
        int msgSn = message.getMsgSn();
        pendingRequests.put(msgSn, responseFuture);
        
        // 设置超时
        scheduler.schedule(() -> {
            CompletableFuture<BaseMessage> f = pendingRequests.remove(msgSn);
            if (f != null && !f.isDone()) {
                f.completeExceptionally(new java.util.concurrent.TimeoutException("Request timeout"));
            }
        }, timeout, unit);
        
        // 发送消息
        channel.writeAndFlush(message).addListener((ChannelFuture f) -> {
            if (!f.isSuccess()) {
                pendingRequests.remove(msgSn);
                responseFuture.completeExceptionally(f.cause());
            }
        });
        
        responseFuture.whenComplete((resp, ex) -> {
            pendingRequests.remove(msgSn);
        });
        
        return (CompletableFuture<T>) responseFuture;
    }
    
    /**
     * 处理响应消息
     *
     * @param response 响应消息
     */
    public void handleResponse(BaseMessage response) {
        CompletableFuture<BaseMessage> future = pendingRequests.remove(response.getMsgSn());
        if (future != null) {
            future.complete(response);
        }
    }
    
    @Override
    public boolean isChannelAvailable() {
        return channel != null && channel.isActive();
    }
    
    @Override
    public Channel getChannel() {
        return channel;
    }
}
