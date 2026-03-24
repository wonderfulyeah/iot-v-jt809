package org.iot.v.jt809.server.handler;

import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import lombok.extern.slf4j.Slf4j;
import org.iot.v.jt809.core.session.Session;
import org.iot.v.jt809.core.session.SessionManager;

/**
 * 服务端异常处理器
 *
 * @author haye
 * @date 2026-03-24
 */
@Slf4j
@ChannelHandler.Sharable
public class ServerExceptionHandler extends ChannelInboundHandlerAdapter {
    
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        Session session = SessionManager.getInstance().getSession(ctx.channel().id().asLongText());
        
        if (session != null) {
            log.error("Exception occurred: sessionId={}, platformId={}, error={}",
                session.getSessionId(), session.getPlatformId(), cause.getMessage(), cause);
        } else {
            log.error("Exception occurred: channelId={}, error={}",
                ctx.channel().id().asLongText(), cause.getMessage(), cause);
        }
        
        // 关闭连接
        ctx.close();
    }
}
