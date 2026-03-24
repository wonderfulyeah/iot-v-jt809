package org.iot.v.jt809.starter.conditional;

import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;

/**
 * JT809服务端模式条件
 * 当配置 jt809.mode=server 时生效
 *
 * @author haye
 * @date 2026-03-24
 */
public class OnJT809ServerMode implements Condition {
    
    @Override
    public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {
        String mode = context.getEnvironment()
            .getProperty("jt809.mode", "");
        String enabled = context.getEnvironment()
            .getProperty("jt809.enabled", "false");
        return "true".equalsIgnoreCase(enabled) && "server".equalsIgnoreCase(mode);
    }
}
