package org.iot.v.jt809.server.config;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * 服务端配置属性
 *
 * @author haye
 * @date 2026-03-24
 */
@Data
public class ServerProperties {
    
    /**
     * 监听端口
     */
    private int port = 9000;
    
    /**
     * Boss线程数
     */
    private int bossThreads = 1;
    
    /**
     * Worker线程数
     */
    private int workerThreads = Runtime.getRuntime().availableProcessors() * 2;
    
    /**
     * 业务线程数
     */
    private int businessThreads = Runtime.getRuntime().availableProcessors() * 2;
    
    /**
     * TCP SO_BACKLOG参数
     */
    private int soBacklog = 1024;
    
    /**
     * TCP SO_REUSEADDR参数
     */
    private boolean soReuseaddr = true;
    
    /**
     * TCP_NODELAY参数
     */
    private boolean tcpNodelay = true;
    
    /**
     * SO_KEEPALIVE参数
     */
    private boolean soKeepalive = true;
    
    /**
     * 接收缓冲区大小
     */
    private int soRcvbuf = 128 * 1024;
    
    /**
     * 发送缓冲区大小
     */
    private int soSndbuf = 128 * 1024;
    
    /**
     * 空闲超时时间（秒）
     */
    private int idleTimeout = 180;
    
    /**
     * 是否启用日志
     */
    private boolean enableLog = false;
    
    /**
     * 授权的平台列表
     */
    private List<PlatformConfig> platforms = new ArrayList<>();
    
    /**
     * 平台配置
     */
    @Data
    public static class PlatformConfig {
        /**
         * 平台ID
         */
        private long platformId;
        
        /**
         * 密码
         */
        private String password;
        
        /**
         * 协议版本
         */
        private String version = "1.0";
        
        /**
         * 是否启用
         */
        private boolean enabled = true;
    }
}
