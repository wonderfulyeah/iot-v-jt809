package org.iot.v.jt809.starter.autoconfigure;

import org.iot.v.jt809.core.session.SessionManager;
import org.iot.v.jt809.handler.HandlerChain;
import org.iot.v.jt809.server.JT809Server;
import org.iot.v.jt809.starter.properties.JT809Properties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 服务端自动配置
 *
 * @author haye
 * @date 2026-03-24
 */
@Configuration
@ConditionalOnProperty(prefix = "jt809", name = "mode", havingValue = "server", matchIfMissing = true)
public class JT809ServerAutoConfiguration {
    
    @Autowired(required = false)
    private HandlerChain handlerChain;
    
    /**
     * JT809服务端
     */
    @Bean
    @ConditionalOnMissingBean
    public JT809Server jt809Server(JT809Properties properties, SessionManager sessionManager) {
        return new JT809Server(properties.toServerProperties(), sessionManager, handlerChain);
    }
}
