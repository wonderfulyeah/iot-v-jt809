package org.iot.v.jt809.starter.autoconfigure;

import org.iot.v.jt809.core.session.SessionManager;
import org.iot.v.jt809.handler.HandlerChain;
import org.iot.v.jt809.handler.MessageHandler;
import org.iot.v.jt809.starter.properties.JT809Properties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * JT809自动配置类
 *
 * @author haye
 * @date 2026-03-24
 */
@Configuration
@EnableConfigurationProperties(JT809Properties.class)
@ConditionalOnProperty(prefix = "jt809", name = "enabled", havingValue = "true", matchIfMissing = true)
public class JT809AutoConfiguration {
    
    @Autowired(required = false)
    private List<MessageHandler> handlers;
    
    /**
     * 会话管理器
     */
    @Bean
    @ConditionalOnMissingBean
    public SessionManager sessionManager() {
        return SessionManager.getInstance();
    }
    
    /**
     * 处理器链
     */
    @Bean
    @ConditionalOnMissingBean
    public HandlerChain handlerChain() {
        HandlerChain chain = new HandlerChain();
        
        // 注册所有处理器
        if (handlers != null && !handlers.isEmpty()) {
            handlers.forEach(chain::addHandler);
        }
        
        return chain;
    }
}
