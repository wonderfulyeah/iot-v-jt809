package org.iot.v.jt809.client.handler;

import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import lombok.extern.slf4j.Slf4j;
import org.iot.v.jt809.client.JT809Client;
import org.iot.v.jt809.client.config.ClientProperties;
import org.iot.v.jt809.core.constant.MessageType;
import org.iot.v.jt809.core.message.base.BaseMessage;
import org.iot.v.jt809.core.message.upstream.UpConnectResp;
import org.iot.v.jt809.core.message.upstream.UpLinkTestResp;
import org.iot.v.jt809.core.session.Session;
import org.iot.v.jt809.core.session.SessionManager;

/**
 * 客户端消息处理器
 *
 * @author haye
 * @date 2026-03-24
 */
@Slf4j
@ChannelHandler.Sharable
public class ClientHandler extends ChannelInboundHandlerAdapter {
    
    private final ClientProperties config;
    private final JT809Client client;
    
    public ClientHandler(ClientProperties config, JT809Client client) {
        this.config = config;
        this.client = client;
    }
    
    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        log.info("Channel active: {}", ctx.channel().remoteAddress());
        super.channelActive(ctx);
    }
    
    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        log.info("Channel inactive: {}", ctx.channel().remoteAddress());
        
        // 登录状态重置
        client.setLoginSuccess(false);
        
        // 触发重连
        if (config.getReconnect().isEnabled()) {
            client.scheduleReconnect();
        }
        
        super.channelInactive(ctx);
    }
    
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (!(msg instanceof BaseMessage)) {
            log.warn("Received non-BaseMessage: {}", msg.getClass().getName());
            return;
        }
        
        BaseMessage message = (BaseMessage) msg;
        int msgId = message.getMsgId();
        
        log.info("Client received message: msgId=0x{}, type={}", 
            Integer.toHexString(msgId), message.getMessageTypeName());
        
        // 更新会话活跃时间
        Session session = client.getSession();
        if (session != null) {
            session.updateLastActiveTime();
        }
        
        switch (msgId) {
            case MessageType.UP_CONNECT_RESP:
                handleLoginResponse((UpConnectResp) message);
                break;
            case MessageType.UP_LINK_TEST_RESP:
                handleHeartbeatResponse((UpLinkTestResp) message);
                break;
            default:
                // 其他消息传递给下一个处理器
                ctx.fireChannelRead(msg);
                break;
        }
    }
    
    /**
     * 处理登录响应
     */
    private void handleLoginResponse(UpConnectResp resp) {
        UpConnectResp.Body body = (UpConnectResp.Body) resp.getBody();
        byte result = body.getResult();
        
        if (result == 0) {
            // 登录成功
            client.setLoginSuccess(true);
            
            // 更新会话
            Session session = client.getSession();
            if (session != null) {
                session.setVerifyCode(body.getVerifyCode());
            }
            
            log.info("Login success: platformId={}, verifyCode={}",
                config.getPlatformId(), body.getVerifyCode());
            
            // 启动心跳
            client.startHeartbeat();
            
        } else {
            // 登录失败
            String errorMsg = getLoginErrorMessage(result);
            log.error("Login failed: platformId={}, result={}, message={}",
                config.getPlatformId(), result, errorMsg);
            
            // 不重连
            client.stopReconnect();
        }
    }
    
    /**
     * 处理心跳响应
     */
    private void handleHeartbeatResponse(UpLinkTestResp resp) {
        log.debug("Heartbeat response received");
    }
    
    /**
     * 获取登录错误信息
     */
    private String getLoginErrorMessage(byte result) {
        switch (result) {
            case 1:
                return "IP地址不正确";
            case 2:
                return "接入码不正确";
            case 3:
                return "用户不存在";
            case 4:
                return "密码错误";
            default:
                return "其他错误";
        }
    }
}
