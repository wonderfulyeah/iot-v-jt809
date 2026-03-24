package org.iot.v.jt809.client.handler;

import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.timeout.IdleStateEvent;
import lombok.extern.slf4j.Slf4j;
import org.iot.v.jt809.client.JT809Client;
import org.iot.v.jt809.core.message.upstream.UpLinkTestReq;

/**
 * 心跳处理器
 * 处理客户端心跳逻辑
 *
 * @author haye
 * @date 2026-03-24
 */
@Slf4j
@ChannelHandler.Sharable
public class HeartbeatHandler extends ChannelInboundHandlerAdapter {
    
    private final JT809Client client;
    private int failedCount = 0;
    
    public HeartbeatHandler(JT809Client client) {
        this.client = client;
    }
    
    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt instanceof IdleStateEvent) {
            IdleStateEvent event = (IdleStateEvent) evt;
            
            if (event.state() == io.netty.handler.timeout.IdleState.WRITER_IDLE) {
                // 写空闲，发送心跳
                if (client.isLoginSuccess()) {
                    UpLinkTestReq heartbeat = new UpLinkTestReq();
                    ctx.writeAndFlush(heartbeat).addListener(future -> {
                        if (!future.isSuccess()) {
                            log.warn("Heartbeat send failed: {}", future.cause().getMessage());
                            failedCount++;
                            
                            // 检查失败次数
                            int timeout = client.getConfig().getHeartbeat().getTimeout();
                            if (timeout > 0 && failedCount >= timeout) {
                                log.error("Heartbeat timeout count reached: {}, closing connection", failedCount);
                                ctx.close();
                            }
                        } else {
                            failedCount = 0;
                            log.debug("Heartbeat sent successfully");
                        }
                    });
                }
            }
        }
        
        super.userEventTriggered(ctx, evt);
    }
    
    /**
     * 重置失败计数
     */
    public void resetFailedCount() {
        this.failedCount = 0;
    }
}
