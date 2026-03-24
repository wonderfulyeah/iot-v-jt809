package org.iot.v.jt809.client.service.impl;

import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import lombok.extern.slf4j.Slf4j;
import org.iot.v.jt809.client.config.ClientProperties;
import org.iot.v.jt809.client.service.RegisterService;
import org.iot.v.jt809.core.message.upstream.UpConnectReq;
import org.iot.v.jt809.core.message.upstream.UpConnectResp;
import org.iot.v.jt809.core.session.Session;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 注册服务实现
 *
 * @author haye
 * @date 2026-03-24
 */
@Slf4j
public class RegisterServiceImpl implements RegisterService {
    
    private final ClientProperties properties;
    private Session session;
    private Channel channel;
    private final AtomicBoolean loggedIn = new AtomicBoolean(false);
    
    public RegisterServiceImpl(ClientProperties properties) {
        this.properties = properties;
    }
    
    /**
     * 设置通道
     */
    public void setChannel(Channel channel) {
        this.channel = channel;
    }
    
    @Override
    public boolean login() {
        if (channel == null || !channel.isActive()) {
            log.error("Channel not available for login");
            return false;
        }
        
        try {
            // 构建登录请求
            UpConnectReq connectReq = new UpConnectReq();
            UpConnectReq.Body body = (UpConnectReq.Body) connectReq.getBody();
            body.setUserId(properties.getPlatformId());
            body.setPassword(properties.getPassword());
            body.setDownLinkIp(properties.getLocalIp());
            body.setDownLinkPort(properties.getLocalPort());
            
            // 发送登录请求
            channel.writeAndFlush(connectReq).await(5, TimeUnit.SECONDS);
            
            // 等待响应（实际应该通过回调处理）
            // 这里只是简单实现，实际需要等待服务器响应
            log.info("Login request sent for platform: {}", properties.getPlatformId());
            return true;
            
        } catch (Exception e) {
            log.error("Login failed", e);
            return false;
        }
    }
    
    @Override
    public void logout() {
        loggedIn.set(false);
        if (session != null) {
            session.close();
            session = null;
        }
        log.info("Logged out");
    }
    
    @Override
    public boolean isLoggedIn() {
        return loggedIn.get() && channel != null && channel.isActive();
    }
    
    /**
     * 处理登录响应
     */
    public void handleLoginResp(UpConnectResp resp) {
        UpConnectResp.Body body = (UpConnectResp.Body) resp.getBody();
        if (body != null && body.getResult() == 0) {
            loggedIn.set(true);
            log.info("Login successful for platform: {}", properties.getPlatformId());
        } else {
            loggedIn.set(false);
            int result = body != null ? body.getResult() : -1;
            log.error("Login failed with result: {}", result);
        }
    }
    
    @Override
    public Session getSession() {
        return session;
    }
    
    /**
     * 设置会话
     */
    public void setSession(Session session) {
        this.session = session;
    }
}
