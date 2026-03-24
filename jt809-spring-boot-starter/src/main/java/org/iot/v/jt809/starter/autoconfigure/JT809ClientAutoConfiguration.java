package org.iot.v.jt809.starter.autoconfigure;

import org.iot.v.jt809.client.JT809Client;
import org.iot.v.jt809.core.session.SessionManager;
import org.iot.v.jt809.starter.properties.JT809Properties;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 客户端自动配置
 *
 * @author haye
 * @date 2026-03-24
 */
@Configuration
@ConditionalOnProperty(prefix = "jt809", name = "mode", havingValue = "client")
public class JT809ClientAutoConfiguration {
    
    /**
     * JT809客户端
     */
    @Bean
    @ConditionalOnMissingBean
    public JT809Client jt809Client(JT809Properties properties, SessionManager sessionManager) {
        return new JT809Client(properties.toClientProperties(), sessionManager);
    }
}
