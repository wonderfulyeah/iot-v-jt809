package org.iot.v.jt809.starter.autoconfigure;

import org.iot.v.jt809.core.session.SessionManager;
import org.iot.v.jt809.handler.HandlerChain;
import org.iot.v.jt809.starter.properties.JT809Properties;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * JT809自动配置类
 * 处理器的注册由 JT809HandlerAutoConfiguration 负责
 *
 * @author haye
 * @date 2026-03-24
 */
@Configuration
@EnableConfigurationProperties(JT809Properties.class)
@ConditionalOnProperty(prefix = "jt809", name = "enabled", havingValue = "true", matchIfMissing = true)
public class JT809AutoConfiguration {
    
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
     * 处理器的注册由 JT809HandlerAutoConfiguration.configureHandlers() 负责
     */
    @Bean
    @ConditionalOnMissingBean
    public HandlerChain handlerChain() {
        return new HandlerChain();
    }
}
