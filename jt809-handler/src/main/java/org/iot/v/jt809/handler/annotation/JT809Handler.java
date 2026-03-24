package org.iot.v.jt809.handler.annotation;

import org.springframework.stereotype.Component;

import java.lang.annotation.*;

/**
 * JT809处理器注解
 * 标注自定义消息处理器的注解
 * 被@Component元注解标注，会自动注册为Spring Bean
 *
 * @author haye
 * @date 2026-03-24
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Component
public @interface JT809Handler {
    
    /**
     * 处理的消息类型ID数组
     * 空数组表示处理所有消息类型
     *
     * @return 消息类型ID数组
     */
    int[] messageTypes() default {};
    
    /**
     * 处理器顺序
     * 数值越小优先级越高
     *
     * @return 顺序值
     */
    int order() default 0;
    
    /**
     * 处理器名称
     * 默认使用类名
     *
     * @return 名称
     */
    String name() default "";
}
