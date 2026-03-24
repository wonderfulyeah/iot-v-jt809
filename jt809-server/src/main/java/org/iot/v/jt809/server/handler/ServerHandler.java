package org.iot.v.jt809.server.handler;

import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import lombok.extern.slf4j.Slf4j;
import org.iot.v.jt809.core.constant.MessageType;
import org.iot.v.jt809.core.constant.SessionState;
import org.iot.v.jt809.core.message.base.BaseMessage;
import org.iot.v.jt809.core.message.upstream.*;
import org.iot.v.jt809.core.session.Session;
import org.iot.v.jt809.core.session.SessionManager;
import org.iot.v.jt809.core.util.SequenceGenerator;
import org.iot.v.jt809.server.config.ServerProperties;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * 服务端消息处理器
 *
 * @author haye
 * @date 2026-03-24
 */
@Slf4j
@ChannelHandler.Sharable
public class ServerHandler extends ChannelInboundHandlerAdapter {
    
    private final ServerProperties config;
    private final SessionManager sessionManager;
    
    /**
     * 平台配置缓存
     */
    private final Map<Long, ServerProperties.PlatformConfig> platformConfigMap = new ConcurrentHashMap<>();
    
    /**
     * 消息监听器列表
     */
    private final List<MessageListener> messageListeners = new CopyOnWriteArrayList<>();
    
    public ServerHandler(ServerProperties config, SessionManager sessionManager) {
        this.config = config;
        this.sessionManager = sessionManager;
        
        // 初始化平台配置缓存
        for (ServerProperties.PlatformConfig platform : config.getPlatforms()) {
            if (platform.isEnabled()) {
                platformConfigMap.put(platform.getPlatformId(), platform);
            }
        }
    }
    
    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        // 创建会话
        Session session = new Session(ctx.channel());
        sessionManager.addSession(session);
        
        log.info("Client connected: sessionId={}, remoteAddress={}",
            session.getSessionId(), session.getRemoteAddress());
        
        super.channelActive(ctx);
    }
    
    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        Session session = sessionManager.getSession(ctx.channel().id().asLongText());
        
        if (session != null) {
            sessionManager.removeSession(session.getSessionId());
            log.info("Client disconnected: sessionId={}, platformId={}",
                session.getSessionId(), session.getPlatformId());
        }
        
        super.channelInactive(ctx);
    }
    
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (!(msg instanceof BaseMessage)) {
            return;
        }
        
        BaseMessage message = (BaseMessage) msg;
        Session session = sessionManager.getSession(ctx.channel().id().asLongText());
        
        if (session == null) {
            log.warn("Session not found for channel: {}", ctx.channel().id().asLongText());
            return;
        }
        
        // 更新活跃时间
        session.updateLastActiveTime();
        
        // 根据消息类型处理
        int msgId = message.getMsgId();
        
        switch (msgId) {
            case MessageType.UP_CONNECT_REQ:
                handleLogin(session, (UpConnectReq) message);
                break;
            case MessageType.UP_LINK_TEST_REQ:
                handleHeartbeat(session, (UpLinkTestReq) message);
                break;
            case MessageType.UP_DISCONNECT_REQ:
                handleDisconnect(session, (UpDisconnectReq) message);
                break;
            default:
                // 其他消息需要验证登录状态
                if (session.getState() != SessionState.AUTHENTICATED) {
                    log.warn("Unauthorized message from platform: sessionId={}, msgId={}",
                        session.getSessionId(), msgId);
                    session.close();
                    return;
                }
                // 通知消息监听器
                notifyMessageListeners(session, message);
                // 传递给下一个处理器（如果有自定义处理器链）
                ctx.fireChannelRead(msg);
                break;
        }
    }
    
    /**
     * 通知消息监听器
     */
    private void notifyMessageListeners(Session session, BaseMessage message) {
        for (MessageListener listener : messageListeners) {
            try {
                listener.onMessage(session, message);
            } catch (Exception e) {
                log.error("Message listener error: {}", e.getMessage(), e);
            }
        }
    }
    
    /**
     * 添加消息监听器
     *
     * @param listener 监听器
     */
    public void addMessageListener(MessageListener listener) {
        messageListeners.add(listener);
        log.info("Added message listener: {}", listener.getClass().getSimpleName());
    }
    
    /**
     * 移除消息监听器
     *
     * @param listener 监听器
     */
    public void removeMessageListener(MessageListener listener) {
        messageListeners.remove(listener);
        log.info("Removed message listener: {}", listener.getClass().getSimpleName());
    }
    
    /**
     * 消息监听器接口
     */
    @FunctionalInterface
    public interface MessageListener {
        /**
         * 接收消息
         *
         * @param session 会话
         * @param message 消息
         */
        void onMessage(Session session, BaseMessage message);
    }
    
    /**
     * 处理登录请求
     */
    private void handleLogin(Session session, UpConnectReq req) {
        UpConnectReq.Body body = (UpConnectReq.Body) req.getBody();
        long userId = body.getUserId();
        String password = body.getPassword();
        
        log.info("Login request received: userId={}, password=****", userId);
        
        UpConnectResp resp = new UpConnectResp();
        UpConnectResp.Body respBody = new UpConnectResp.Body();
        
        // 查找平台配置
        ServerProperties.PlatformConfig platformConfig = platformConfigMap.get(userId);
        
        if (platformConfig == null) {
            // 用户不存在
            respBody.setResult((byte) 3);
            log.warn("Login failed: platform not found, userId={}", userId);
        } else if (!platformConfig.getPassword().equals(password)) {
            // 密码错误
            respBody.setResult((byte) 4);
            log.warn("Login failed: invalid password, platformId={}", userId);
        } else {
            // 登录成功
            respBody.setResult((byte) 0);
            // 生成校验码
            long verifyCode = SequenceGenerator.getInstance().next();
            respBody.setVerifyCode(verifyCode);
            
            // 更新会话状态
            session.setPlatformId(userId);
            session.setVerifyCode(verifyCode);
            session.setState(SessionState.AUTHENTICATED);
            session.setLoginTime(java.time.Instant.now());
            
            // 更新会话管理器中的平台映射
            sessionManager.addSession(session);
            
            log.info("Login success: platformId={}, sessionId={}, verifyCode={}", 
                userId, session.getSessionId(), verifyCode);
        }
        
        resp.setBody(respBody);
        log.info("Sending login response: result={}", respBody.getResult());
        session.send(resp);
    }
    
    /**
     * 处理心跳请求
     */
    private void handleHeartbeat(Session session, UpLinkTestReq req) {
        UpLinkTestResp resp = new UpLinkTestResp();
        session.send(resp);
        
        log.debug("Heartbeat received from platform: platformId={}", session.getPlatformId());
    }
    
    /**
     * 处理断开连接请求
     */
    private void handleDisconnect(Session session, UpDisconnectReq req) {
        log.info("Disconnect request from platform: platformId={}", session.getPlatformId());
        session.close();
    }
    
    /**
     * 检查平台是否授权
     */
    public boolean isPlatformAuthorized(long platformId) {
        return platformConfigMap.containsKey(platformId);
    }
    
    /**
     * 获取平台配置
     */
    public ServerProperties.PlatformConfig getPlatformConfig(long platformId) {
        return platformConfigMap.get(platformId);
    }
}
