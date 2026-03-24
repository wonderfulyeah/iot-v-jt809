package org.iot.v.jt809.starter.properties;

import lombok.Data;
import org.iot.v.jt809.client.config.ClientProperties;
import org.iot.v.jt809.server.config.ServerProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.List;

/**
 * JT809配置属性
 *
 * @author haye
 * @date 2026-03-24
 */
@Data
@ConfigurationProperties(prefix = "jt809")
public class JT809Properties {
    
    /**
     * 是否启用JT809
     */
    private boolean enabled = true;
    
    /**
     * 运行模式: server - 服务端（上级平台），client - 客户端（下级平台）
     */
    private String mode = "server";
    
    /**
     * 服务端配置
     */
    private ServerConfig server = new ServerConfig();
    
    /**
     * 客户端配置
     */
    private ClientConfig client = new ClientConfig();
    
    /**
     * 授权平台列表（服务端模式）
     */
    private List<PlatformConfig> platforms = new ArrayList<>();
    
    /**
     * 服务端配置
     */
    @Data
    public static class ServerConfig {
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
    }
    
    /**
     * 客户端配置
     */
    @Data
    public static class ClientConfig {
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
    }
    
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
    
    /**
     * 转换为服务端配置
     */
    public ServerProperties toServerProperties() {
        ServerProperties props = new ServerProperties();
        props.setPort(server.getPort());
        props.setBossThreads(server.getBossThreads());
        props.setWorkerThreads(server.getWorkerThreads());
        props.setBusinessThreads(server.getBusinessThreads());
        props.setSoBacklog(server.getSoBacklog());
        props.setSoReuseaddr(server.isSoReuseaddr());
        props.setTcpNodelay(server.isTcpNodelay());
        props.setSoKeepalive(server.isSoKeepalive());
        props.setSoRcvbuf(server.getSoRcvbuf());
        props.setSoSndbuf(server.getSoSndbuf());
        props.setIdleTimeout(server.getIdleTimeout());
        props.setEnableLog(server.isEnableLog());
        
        // 转换平台配置
        for (PlatformConfig platform : platforms) {
            if (platform.isEnabled()) {
                ServerProperties.PlatformConfig pc = new ServerProperties.PlatformConfig();
                pc.setPlatformId(platform.getPlatformId());
                pc.setPassword(platform.getPassword());
                pc.setVersion(platform.getVersion());
                pc.setEnabled(platform.isEnabled());
                props.getPlatforms().add(pc);
            }
        }
        
        return props;
    }
    
    /**
     * 转换为客户端配置
     */
    public ClientProperties toClientProperties() {
        ClientProperties props = new ClientProperties();
        props.setServerIp(client.getServerIp());
        props.setServerPort(client.getServerPort());
        props.setPlatformId(client.getPlatformId());
        props.setPassword(client.getPassword());
        props.setVersion(client.getVersion());
        props.setLocalIp(client.getLocalIp());
        props.setLocalPort(client.getLocalPort());
        props.setWorkerThreads(client.getWorkerThreads());
        props.setBusinessThreads(client.getBusinessThreads());
        props.setTcpNodelay(client.isTcpNodelay());
        props.setSoKeepalive(client.isSoKeepalive());
        props.setConnectTimeout(client.getConnectTimeout());
        props.setSoRcvbuf(client.getSoRcvbuf());
        props.setSoSndbuf(client.getSoSndbuf());
        props.setEnableLog(client.isEnableLog());
        
        // 重连配置
        ClientProperties.ReconnectConfig reconnect = new ClientProperties.ReconnectConfig();
        reconnect.setEnabled(client.getReconnect().isEnabled());
        reconnect.setInterval(client.getReconnect().getInterval());
        reconnect.setMaxAttempts(client.getReconnect().getMaxAttempts());
        reconnect.setBackoffMultiplier(client.getReconnect().getBackoffMultiplier());
        props.setReconnect(reconnect);
        
        // 心跳配置
        ClientProperties.HeartbeatConfig heartbeat = new ClientProperties.HeartbeatConfig();
        heartbeat.setEnabled(client.getHeartbeat().isEnabled());
        heartbeat.setInterval(client.getHeartbeat().getInterval());
        heartbeat.setTimeout(client.getHeartbeat().getTimeout());
        props.setHeartbeat(heartbeat);
        
        return props;
    }
}
