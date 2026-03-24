package org.iot.v.jt809.starter.conditional;

import org.iot.v.jt809.starter.properties.JT809Properties;
import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;

/**
 * JT809启用条件
 * 当配置 jt809.enabled=true 时生效
 *
 * @author haye
 * @date 2026-03-24
 */
public class OnJT809Enabled implements Condition {
    
    @Override
    public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {
        String enabled = context.getEnvironment()
            .getProperty("jt809.enabled", "false");
        return "true".equalsIgnoreCase(enabled);
    }
}
