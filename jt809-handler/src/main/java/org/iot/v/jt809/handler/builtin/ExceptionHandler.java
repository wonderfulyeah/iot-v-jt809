package org.iot.v.jt809.handler.builtin;

import io.netty.channel.ChannelHandlerContext;
import lombok.extern.slf4j.Slf4j;
import org.iot.v.jt809.core.exception.JT809Exception;
import org.iot.v.jt809.handler.context.MessageContext;

/**
 * 内置异常处理器
 * 统一处理消息处理过程中发生的异常
 *
 * @author haye
 * @date 2026-03-24
 */
@Slf4j
public class ExceptionHandler {
    
    /**
     * 处理异常
     *
     * @param ctx     通道上下文
     * @param cause   异常原因
     */
    public void handleException(ChannelHandlerContext ctx, Throwable cause) {
        if (cause instanceof JT809Exception) {
            handleJT809Exception(ctx, (JT809Exception) cause);
        } else {
            handleGenericException(ctx, cause);
        }
    }
    
    /**
     * 处理JT809协议异常
     */
    private void handleJT809Exception(ChannelHandlerContext ctx, JT809Exception e) {
        log.error("JT809 protocol error: {}", e.getMessage());
        
        // 根据错误类型决定是否关闭连接
        switch (e.getErrorCode()) {
            case JT809Exception.ERR_INVALID_MESSAGE:
            case JT809Exception.ERR_DECODE_FAILED:
            case JT809Exception.ERR_ENCODE_FAILED:
                // 协议错误，记录日志但不关闭连接
                log.warn("Protocol error, message ignored: {}", e.getMessage());
                break;
                
            case JT809Exception.ERR_UNAUTHORIZED:
            case JT809Exception.ERR_AUTH_FAILED:
                // 认证错误，关闭连接
                log.error("Authentication error, closing connection: {}", e.getMessage());
                ctx.close();
                break;
                
            default:
                // 其他错误
                log.error("Unknown error: {}", e.getMessage());
                ctx.close();
                break;
        }
    }
    
    /**
     * 处理通用异常
     */
    private void handleGenericException(ChannelHandlerContext ctx, Throwable cause) {
        log.error("Unexpected error during message processing", cause);
        
        // 关闭连接
        ctx.close();
    }
    
    /**
     * 处理消息处理上下文中的异常
     *
     * @param context 消息上下文
     * @param cause   异常原因
     */
    public void handleContextException(MessageContext context, Throwable cause) {
        log.error("Error processing message from platform {}: {}", 
            context.getSession() != null ? context.getSession().getPlatformId() : "unknown",
            cause.getMessage(), cause);
        
        // 记录错误计数
        if (context.getSession() != null) {
            context.getSession().incrementErrorCount();
        }
    }
}
