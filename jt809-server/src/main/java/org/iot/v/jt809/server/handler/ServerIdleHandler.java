package org.iot.v.jt809.server.handler;

import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.timeout.IdleStateEvent;
import lombok.extern.slf4j.Slf4j;
import org.iot.v.jt809.core.session.Session;
import org.iot.v.jt809.core.session.SessionManager;

/**
 * 服务端空闲处理器
 * 处理客户端空闲超时
 *
 * @author haye
 * @date 2026-03-24
 */
@Slf4j
@ChannelHandler.Sharable
public class ServerIdleHandler extends ChannelInboundHandlerAdapter {
    
    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt instanceof IdleStateEvent) {
            IdleStateEvent event = (IdleStateEvent) evt;
            
            if (event.state() == io.netty.handler.timeout.IdleState.READER_IDLE) {
                Session session = SessionManager.getInstance().getSession(ctx.channel().id().asLongText());
                
                if (session != null) {
                    log.warn("Channel idle timeout, closing: sessionId={}, platformId={}",
                        session.getSessionId(), session.getPlatformId());
                } else {
                    log.warn("Channel idle timeout, closing: channelId={}", ctx.channel().id().asLongText());
                }
                
                ctx.close();
            }
        }
        
        super.userEventTriggered(ctx, evt);
    }
}
