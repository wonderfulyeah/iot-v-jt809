package org.iot.v.jt809.client.handler;

import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import lombok.extern.slf4j.Slf4j;
import org.iot.v.jt809.client.JT809Client;

/**
 * 重连处理器
 * 处理连接断开后的重连逻辑
 *
 * @author haye
 * @date 2026-03-24
 */
@Slf4j
@ChannelHandler.Sharable
public class ReconnectHandler extends ChannelInboundHandlerAdapter {
    
    private final JT809Client client;
    
    public ReconnectHandler(JT809Client client) {
        this.client = client;
    }
    
    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        // 检查客户端是否仍在运行，避免关闭时触发重连
        if (!client.isRunning()) {
            log.info("Client is shutting down, skip reconnect");
            super.channelInactive(ctx);
            return;
        }
        
        log.warn("Connection lost, will attempt to reconnect...");
        
        // 登录状态重置
        client.setLoginSuccess(false);
        
        // 停止心跳
        client.stopHeartbeat();
        
        // 调度重连
        client.scheduleReconnect();
        
        super.channelInactive(ctx);
    }
    
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        log.error("Connection error: {}", cause.getMessage(), cause);
        
        // 登录状态重置
        client.setLoginSuccess(false);
        
        // 关闭连接，触发重连
        ctx.close();
    }
}
