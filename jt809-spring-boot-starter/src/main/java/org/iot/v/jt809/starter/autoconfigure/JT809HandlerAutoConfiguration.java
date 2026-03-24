package org.iot.v.jt809.starter.autoconfigure;

import lombok.extern.slf4j.Slf4j;
import org.iot.v.jt809.handler.HandlerChain;
import org.iot.v.jt809.handler.MessageHandler;
import org.iot.v.jt809.handler.annotation.JT809Handler;
import org.iot.v.jt809.handler.interceptor.LoggingInterceptor;
import org.iot.v.jt809.handler.interceptor.MetricInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;
import java.util.Map;

/**
 * 处理器自动配置
 * 自动注册带有@JT809Handler注解的处理器
 *
 * @author haye
 * @date 2026-03-24
 */
@Slf4j
@Configuration
public class JT809HandlerAutoConfiguration {
    
    @Autowired(required = false)
    private List<MessageHandler> handlers;
    
    @Autowired(required = false)
    private Map<String, MessageHandler> handlerMap;
    
    /**
     * 日志拦截器
     */
    @Bean
    @ConditionalOnMissingBean
    public LoggingInterceptor loggingInterceptor() {
        return new LoggingInterceptor();
    }
    
    /**
     * 监控拦截器
     */
    @Bean
    @ConditionalOnMissingBean
    public MetricInterceptor metricInterceptor() {
        return new MetricInterceptor();
    }
    
    /**
     * 自动注册处理器到处理器链
     * 此方法在HandlerChain创建后被调用
     */
    @Autowired(required = false)
    public void configureHandlers(HandlerChain handlerChain) {
        if (handlerChain == null) {
            return;
        }
        
        int registered = 0;
        
        // 从List中注册
        if (handlers != null) {
            for (MessageHandler handler : handlers) {
                registerHandler(handlerChain, handler);
                registered++;
            }
        }
        
        // 从Map中注册（处理通过@Bean方法注册的处理器）
        if (handlerMap != null) {
            for (MessageHandler handler : handlerMap.values()) {
                registerHandler(handlerChain, handler);
                registered++;
            }
        }
        
        log.info("JT809 handlers configured: total={}", registered);
    }
    
    /**
     * 注册处理器
     */
    private void registerHandler(HandlerChain chain, MessageHandler handler) {
        if (handler == null) {
            return;
        }
        
        // 检查是否带有@JT809Handler注解
        JT809Handler annotation = handler.getClass().getAnnotation(JT809Handler.class);
        if (annotation != null) {
            log.info("Registering JT809 handler: {}, order={}, types={}",
                handler.getName(),
                annotation.order(),
                java.util.Arrays.toString(annotation.messageTypes()));
        }
        
        chain.addHandler(handler);
    }
}
