package org.iot.v.jt809.client;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.timeout.IdleStateHandler;
import io.netty.util.concurrent.DefaultThreadFactory;
import lombok.extern.slf4j.Slf4j;
import org.iot.v.jt809.client.config.ClientProperties;
import org.iot.v.jt809.client.handler.ClientHandler;
import org.iot.v.jt809.client.handler.HeartbeatHandler;
import org.iot.v.jt809.client.handler.ReconnectHandler;
import org.iot.v.jt809.core.codec.decoder.JT809ProtocolDecoder;
import org.iot.v.jt809.core.codec.JT809Encoder;
import org.iot.v.jt809.core.codec.decoder.JT809DelimiterFrameDecoder;
import org.iot.v.jt809.core.codec.decoder.JT809UnescapeDecoder;
import org.iot.v.jt809.core.constant.SessionState;
import org.iot.v.jt809.core.message.base.BaseMessage;
import org.iot.v.jt809.core.message.upstream.UpConnectReq;
import org.iot.v.jt809.core.message.upstream.UpDisconnectReq;
import org.iot.v.jt809.core.message.upstream.UpLinkTestReq;
import org.iot.v.jt809.core.session.Session;
import org.iot.v.jt809.core.session.SessionManager;
import org.springframework.context.SmartLifecycle;

import java.net.InetSocketAddress;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * JT809客户端
 * 作为下级平台，向上级平台发起连接并上报数据
 *
 * @author haye
 * @date 2026-03-24
 */
@Slf4j
public class JT809Client implements SmartLifecycle {
    
    private final ClientProperties config;
    private final ClientHandler clientHandler;
    private final SessionManager sessionManager;
    
    // Netty组件
    private EventLoopGroup workerGroup;
    private EventLoopGroup businessGroup;
    private Channel clientChannel;
    private Bootstrap bootstrap;
    
    // 定时任务
    private ScheduledFuture<?> heartbeatFuture;
    private ScheduledFuture<?> reconnectFuture;
    
    // 生命周期状态
    private volatile boolean running = false;
    private volatile boolean loginSuccess = false;
    private volatile int reconnectAttempts = 0;
    
    // 会话
    private Session session;
    
    public JT809Client(ClientProperties config, SessionManager sessionManager) {
        this.config = config;
        this.sessionManager = sessionManager;
        this.clientHandler = new ClientHandler(config, this);
    }
    
    /**
     * 启动客户端
     */
    @Override
    public void start() {
        if (running) {
            return;
        }
        
        log.info("Starting JT809 Client, target: {}:{}", config.getServerIp(), config.getServerPort());
        
        try {
            // 1. 创建EventLoopGroup
            workerGroup = new NioEventLoopGroup(config.getWorkerThreads(),
                new DefaultThreadFactory("jt809-client-worker"));
            
            businessGroup = new NioEventLoopGroup(config.getBusinessThreads(),
                new DefaultThreadFactory("jt809-client-business"));
            
            // 2. 配置Bootstrap
            bootstrap = new Bootstrap();
            bootstrap.group(workerGroup)
                .channel(NioSocketChannel.class)
                .option(ChannelOption.TCP_NODELAY, config.isTcpNodelay())
                .option(ChannelOption.SO_KEEPALIVE, config.isSoKeepalive())
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, config.getConnectTimeout())
                .option(ChannelOption.SO_RCVBUF, config.getSoRcvbuf())
                .option(ChannelOption.SO_SNDBUF, config.getSoSndbuf())
                .option(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT)
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel ch) {
                        initChannelPipeline(ch);
                    }
                });
            
            // 3. 连接服务器
            connect();
            
            running = true;
            log.info("JT809 Client started successfully");
            
        } catch (Exception e) {
            log.error("Start JT809 Client failed", e);
            shutdown();
            throw new RuntimeException("Start JT809 Client failed", e);
        }
    }
    
    /**
     * 初始化Channel Pipeline
     */
    private void initChannelPipeline(SocketChannel ch) {
        ChannelPipeline pipeline = ch.pipeline();
        
        // 1. 空闲检测
        pipeline.addLast("idleState",
            new IdleStateHandler(0, config.getHeartbeat().getInterval() / 1000, 0, TimeUnit.MILLISECONDS));
        
        // 2. 编解码器
        pipeline.addLast("delimiterFrameDecoder",new JT809DelimiterFrameDecoder());
        pipeline.addLast("unescapeDecoder",new JT809UnescapeDecoder());
        pipeline.addLast("decoder", new JT809ProtocolDecoder());
        pipeline.addLast("encoder", new JT809Encoder());
        
        // 3. 业务处理器（使用业务线程池）
        pipeline.addLast(businessGroup, "clientHandler", clientHandler);
        
        // 4. 重连处理器
        pipeline.addLast("reconnectHandler", new ReconnectHandler(this));
        
        // 5. 心跳处理器
        pipeline.addLast("heartbeatHandler", new HeartbeatHandler(this));
        
        // 6. 日志处理器（可选）
        if (config.isEnableLog()) {
            pipeline.addLast("logging", new LoggingHandler(LogLevel.DEBUG));
        }
    }
    
    /**
     * 连接服务器
     */
    public void connect() {
        if (clientChannel != null && clientChannel.isActive()) {
            log.info("Client is already connected");
            return;
        }
        
        ChannelFuture connectFuture;
        if (config.getLocalIp() != null && config.getLocalPort() > 0) {
            connectFuture = bootstrap.connect(
                new InetSocketAddress(config.getServerIp(), config.getServerPort()),
                new InetSocketAddress(config.getLocalIp(), config.getLocalPort())
            );
        } else {
            connectFuture = bootstrap.connect(config.getServerIp(), config.getServerPort());
        }
        
        connectFuture.addListener((ChannelFutureListener) future -> {
            if (future.isSuccess()) {
                clientChannel = future.channel();
                reconnectAttempts = 0;
                
                log.info("Connected to server: {}:{}", config.getServerIp(), config.getServerPort());
                
                // 创建会话
                session = new Session(clientChannel);
                sessionManager.addSession(session);
                
                // 发送登录请求
                login();
                
            } else {
                log.error("Failed to connect to server: {}:{}", config.getServerIp(), config.getServerPort());
                
                // 触发重连
                if (config.getReconnect().isEnabled()) {
                    scheduleReconnect();
                }
            }
        });
    }
    
    /**
     * 断开连接
     */
    @Override
    public void stop() {
        if (!running) {
            return;
        }
        
        // 首先设置运行状态为false，防止关闭时触发重连
        running = false;
        
        log.info("Stopping JT809 Client...");
        
        // 1. 停止心跳和重连任务
        stopHeartbeat();
        stopReconnect();
        
        // 2. 发送断开请求
        if (clientChannel != null && clientChannel.isActive() && loginSuccess) {
            sendDisconnectRequest();
        }
        
        // 3. 关闭Channel
        if (clientChannel != null) {
            clientChannel.close().awaitUninterruptibly();
        }
        
        // 4. 清理会话
        if (session != null) {
            sessionManager.removeSession(session.getSessionId());
        }
        
        // 5. 优雅关闭EventLoopGroup
        shutdown();
        
        loginSuccess = false;
        log.info("JT809 Client stopped");
    }
    
    /**
     * 发送消息
     *
     * @param message 消息对象
     */
    public void send(BaseMessage message) {
        if (clientChannel != null && clientChannel.isActive() && loginSuccess) {
            clientChannel.writeAndFlush(message).addListener((ChannelFutureListener) future -> {
                if (!future.isSuccess()) {
                    log.error("Send message failed", future.cause());
                }
            });
        } else {
            log.warn("Client channel is not active or not logged in, cannot send message");
        }
    }
    
    /**
     * 登录
     */
    private void login() {
        UpConnectReq loginReq = new UpConnectReq();
        UpConnectReq.Body body = new UpConnectReq.Body();
        body.setUserId(config.getPlatformId());
        body.setPassword(config.getPassword());
        body.setDownLinkIp(config.getLocalIp() != null ? config.getLocalIp() : "0.0.0.0");
        body.setDownLinkPort(config.getLocalPort());
        loginReq.setBody(body);
        
        // 设置消息头
        loginReq.getHead().setPlatformId(config.getPlatformId());
        
        // 直接发送，不经过登录状态检查
        if (clientChannel != null && clientChannel.isActive()) {
            clientChannel.writeAndFlush(loginReq);
            log.info("Sent login request to server");
        }
    }
    
    /**
     * 发送断开请求
     */
    private void sendDisconnectRequest() {
        UpDisconnectReq disconnectReq = new UpDisconnectReq();
        disconnectReq.getHead().setPlatformId(config.getPlatformId());
        
        if (clientChannel != null && clientChannel.isActive()) {
            clientChannel.writeAndFlush(disconnectReq);
            log.info("Sent disconnect request to server");
        }
    }
    
    /**
     * 启动心跳
     */
    public void startHeartbeat() {
        if (!config.getHeartbeat().isEnabled() || workerGroup == null) {
            return;
        }
        
        stopHeartbeat();
        
        heartbeatFuture = workerGroup.scheduleAtFixedRate(() -> {
            if (clientChannel != null && clientChannel.isActive() && loginSuccess) {
                UpLinkTestReq heartbeat = new UpLinkTestReq();
                send(heartbeat);
                log.debug("Sent heartbeat to server");
            }
        }, config.getHeartbeat().getInterval(), config.getHeartbeat().getInterval(), TimeUnit.MILLISECONDS);
        
        log.info("Started heartbeat task, interval: {}ms", config.getHeartbeat().getInterval());
    }
    
    /**
     * 停止心跳
     */
    public void stopHeartbeat() {
        if (heartbeatFuture != null) {
            heartbeatFuture.cancel(false);
            heartbeatFuture = null;
        }
    }
    
    /**
     * 调度重连
     */
    public void scheduleReconnect() {
        // 检查客户端是否仍在运行
        if (!running) {
            log.debug("Client is not running, skip reconnect");
            return;
        }
        
        if (!config.getReconnect().isEnabled() || workerGroup == null) {
            return;
        }
        
        // 检查最大重连次数
        int maxAttempts = config.getReconnect().getMaxAttempts();
        if (maxAttempts > 0 && reconnectAttempts >= maxAttempts) {
            log.error("Max reconnect attempts reached: {}", maxAttempts);
            return;
        }
        
        stopReconnect();
        
        // 计算重连间隔（指数退避）
        long interval = (long) (config.getReconnect().getInterval() *
            Math.pow(config.getReconnect().getBackoffMultiplier(), reconnectAttempts));
        
        reconnectFuture = workerGroup.schedule(() -> {
            reconnectAttempts++;
            log.info("Reconnecting to server: {}:{}, attempt: {}",
                config.getServerIp(), config.getServerPort(), reconnectAttempts);
            connect();
        }, interval, TimeUnit.MILLISECONDS);
        
        log.info("Scheduled reconnect after {}ms", interval);
    }
    
    /**
     * 停止重连
     */
    public void stopReconnect() {
        if (reconnectFuture != null) {
            reconnectFuture.cancel(false);
            reconnectFuture = null;
        }
    }
    
    /**
     * 优雅关闭
     */
    private void shutdown() {
        if (workerGroup != null) {
            workerGroup.shutdownGracefully(100, 300, TimeUnit.MILLISECONDS);
        }
        if (businessGroup != null) {
            businessGroup.shutdownGracefully(100, 300, TimeUnit.MILLISECONDS);
        }
    }
    
    @Override
    public boolean isRunning() {
        return running;
    }
    
    @Override
    public int getPhase() {
        return 0;
    }
    
    @Override
    public boolean isAutoStartup() {
        return true;
    }
    
    @Override
    public void stop(Runnable callback) {
        stop();
        callback.run();
    }
    
    // Getters and Setters
    
    public boolean isLoginSuccess() {
        return loginSuccess;
    }
    
    public void setLoginSuccess(boolean loginSuccess) {
        this.loginSuccess = loginSuccess;
        
        // 登录成功后更新会话状态
        if (loginSuccess && session != null) {
            session.setState(SessionState.AUTHENTICATED);
            session.setPlatformId(config.getPlatformId());
        }
    }
    
    public ClientProperties getConfig() {
        return config;
    }
    
    public Session getSession() {
        return session;
    }
    
    public void setSession(Session session) {
        this.session = session;
    }
    
    /**
     * 检查客户端是否已连接
     *
     * @return true-已连接, false-未连接
     */
    public boolean isConnected() {
        return clientChannel != null && clientChannel.isActive();
    }
}
