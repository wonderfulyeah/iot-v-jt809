package org.iot.v.jt809.client.config;

import lombok.Data;

/**
 * 客户端配置属性
 *
 * @author haye
 * @date 2026-03-24
 */
@Data
public class ClientProperties {
    
    /**
     * 服务器IP
     */
    private String serverIp;
    
    /**
     * 服务器端口
     */
    private int serverPort;
    
    /**
     * 本平台ID
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
     * 本地IP
     */
    private String localIp;
    
    /**
     * 本地端口（0表示随机）
     */
    private int localPort = 0;
    
    /**
     * Worker线程数
     */
    private int workerThreads = Runtime.getRuntime().availableProcessors();
    
    /**
     * 业务线程数
     */
    private int businessThreads = Runtime.getRuntime().availableProcessors();
    
    /**
     * TCP_NODELAY参数
     */
    private boolean tcpNodelay = true;
    
    /**
     * SO_KEEPALIVE参数
     */
    private boolean soKeepalive = true;
    
    /**
     * 连接超时（毫秒）
     */
    private int connectTimeout = 10000;
    
    /**
     * 接收缓冲区大小
     */
    private int soRcvbuf = 32 * 1024;
    
    /**
     * 发送缓冲区大小
     */
    private int soSndbuf = 32 * 1024;
    
    /**
     * 是否启用日志
     */
    private boolean enableLog = false;
    
    /**
     * 重连配置
     */
    private ReconnectConfig reconnect = new ReconnectConfig();
    
    /**
     * 心跳配置
     */
    private HeartbeatConfig heartbeat = new HeartbeatConfig();
    
    /**
     * 重连配置
     */
    @Data
    public static class ReconnectConfig {
        /**
         * 是否启用自动重连
         */
        private boolean enabled = true;
        
        /**
         * 重连间隔（毫秒）
         */
        private int interval = 5000;
        
        /**
         * 最大重连次数（0表示无限制）
         */
        private int maxAttempts = 10;
        
        /**
         * 退避倍数
         */
        private double backoffMultiplier = 1.5;
    }
    
    /**
     * 心跳配置
     */
    @Data
    public static class HeartbeatConfig {
        /**
         * 是否启用心跳
         */
        private boolean enabled = true;
        
        /**
         * 心跳间隔（毫秒）
         */
        private int interval = 60000;
        
        /**
         * 超时次数
         */
        private int timeout = 3;
    }
}
