package org.iot.v.jt809.handler;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import lombok.extern.slf4j.Slf4j;
import org.iot.v.jt809.core.message.base.BaseMessage;
import org.iot.v.jt809.core.session.SessionManager;
import org.iot.v.jt809.handler.context.MessageContext;

/**
 * HandlerChain 到 Netty Pipeline 的桥接处理器
 * 将消息传递给 HandlerChain 进行处理
 *
 * @author haye
 * @date 2026-03-26
 */
@Slf4j
public class HandlerChainChannelHandler extends ChannelInboundHandlerAdapter {

    private final HandlerChain handlerChain;
    private final SessionManager sessionManager;

    public HandlerChainChannelHandler(HandlerChain handlerChain, SessionManager sessionManager) {
        this.handlerChain = handlerChain;
        this.sessionManager = sessionManager;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (msg instanceof BaseMessage) {
            BaseMessage message = (BaseMessage) msg;
            
            // 创建消息上下文
            MessageContext context = new MessageContext(sessionManager, ctx.channel());
            
            log.debug("HandlerChainChannelHandler processing message: msgId=0x{}", 
                Integer.toHexString(message.getMsgId()));
            
            // 调用处理器链处理消息
            handlerChain.process(context, message);
        } else {
            // 非BaseMessage消息，继续传递
            super.channelRead(ctx, msg);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        log.error("HandlerChainChannelHandler exception: {}", cause.getMessage(), cause);
        ctx.close();
    }
}
