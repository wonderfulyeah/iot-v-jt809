package org.iot.v.jt809.handler.builtin;

import lombok.extern.slf4j.Slf4j;
import org.iot.v.jt809.core.constant.MessageType;
import org.iot.v.jt809.core.message.base.BaseMessage;
import org.iot.v.jt809.core.message.upstream.UpConnectReq;
import org.iot.v.jt809.core.message.upstream.UpConnectResp;
import org.iot.v.jt809.core.message.downstream.DownConnectReq;
import org.iot.v.jt809.core.message.downstream.DownConnectResp;
import org.iot.v.jt809.core.session.Session;
import org.iot.v.jt809.core.session.SessionManager;
import org.iot.v.jt809.handler.MessageHandler;
import org.iot.v.jt809.handler.context.MessageContext;

/**
 * 内置登录处理器
 * 处理登录请求并返回响应
 *
 * @author haye
 * @date 2026-03-24
 */
@Slf4j
public class LoginHandler implements MessageHandler {
    
    /**
     * 认证回调接口
     */
    public interface Authenticator {
        /**
         * 验证登录请求
         *
         * @param connectReq 连接请求
         * @return 验证结果码
         */
        int authenticate(UpConnectReq connectReq);
    }
    
    private Authenticator authenticator;
    
    public LoginHandler() {
    }
    
    public LoginHandler(Authenticator authenticator) {
        this.authenticator = authenticator;
    }
    
    public void setAuthenticator(Authenticator authenticator) {
        this.authenticator = authenticator;
    }
    
    @Override
    public int[] supportedMessageTypes() {
        return new int[] {
            MessageType.UP_CONNECT_REQ,
            MessageType.DOWN_CONNECT_REQ
        };
    }
    
    @Override
    public boolean handle(MessageContext context, BaseMessage message) {
        int msgId = message.getMsgId();
        
        if (msgId == MessageType.UP_CONNECT_REQ) {
            return handleUpConnect(context, (UpConnectReq) message);
        } else if (msgId == MessageType.DOWN_CONNECT_REQ) {
            return handleDownConnect(context, (DownConnectReq) message);
        }
        
        return true;
    }
    
    /**
     * 处理上行连接请求
     */
    private boolean handleUpConnect(MessageContext context, UpConnectReq req) {
        UpConnectResp resp = new UpConnectResp();
        
        // 验证请求
        int result;
        if (authenticator != null) {
            result = authenticator.authenticate(req);
        } else {
            // 默认允许所有请求
            result = 0x00;
            log.warn("No authenticator configured, allowing all connections");
        }
        
        UpConnectResp.Body respBody = (UpConnectResp.Body) resp.getBody();
        respBody.setResult((byte) result);
        
        Session session = context.getSession();
        if (result == 0x00) {
            // 登录成功，创建会话
            UpConnectReq.Body reqBody = (UpConnectReq.Body) req.getBody();
            long platformId = reqBody.getUserId();
            if (session != null) {
                session.setPlatformId(platformId);
                session.setAuthenticated(true);
                SessionManager.getInstance().addSession(session);
            }
            log.info("Platform {} login successful", platformId);
        } else {
            UpConnectReq.Body reqBody = (UpConnectReq.Body) req.getBody();
            log.warn("Platform {} login failed, result: {}", 
                reqBody.getUserId(), result);
        }
        
        // 发送响应
        if (session != null) {
            session.send(resp);
        }
        
        return false; // 终止处理链，登录消息不需要继续处理
    }
    
    /**
     * 处理下行连接请求
     */
    private boolean handleDownConnect(MessageContext context, DownConnectReq req) {
        DownConnectResp resp = new DownConnectResp();
        
        // 简单验证校验码
        DownConnectReq.Body reqBody = (DownConnectReq.Body) req.getBody();
        long verifyCode = reqBody.getVerifyCode();
        
        // TODO: 实现校验码验证逻辑
        // 这里暂时总是返回成功
        DownConnectResp.Body respBody = (DownConnectResp.Body) resp.getBody();
        respBody.setResult(0x00);
        
        log.info("Down connect request received, verifyCode: {}", verifyCode);
        
        // 发送响应
        Session session = context.getSession();
        if (session != null) {
            session.send(resp);
        }
        
        return false; // 终止处理链
    }
    
    @Override
    public int getOrder() {
        return Integer.MIN_VALUE + 1; // 第二高优先级
    }
    
    @Override
    public String getName() {
        return "LoginHandler";
    }
}
