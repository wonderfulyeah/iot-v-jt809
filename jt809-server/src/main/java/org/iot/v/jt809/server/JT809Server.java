package org.iot.v.jt809.server;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.timeout.IdleStateHandler;
import io.netty.util.concurrent.DefaultThreadFactory;
import lombok.extern.slf4j.Slf4j;
import org.iot.v.jt809.core.codec.JT809Decoder;
import org.iot.v.jt809.core.codec.JT809Encoder;
import org.iot.v.jt809.core.session.SessionManager;
import org.iot.v.jt809.server.config.ServerProperties;
import org.iot.v.jt809.server.handler.ServerExceptionHandler;
import org.iot.v.jt809.server.handler.ServerHandler;
import org.iot.v.jt809.server.handler.ServerIdleHandler;
import org.springframework.context.SmartLifecycle;

import java.util.concurrent.TimeUnit;

/**
 * JT809服务端
 * 作为上级平台，接收下级平台的连接和数据上报
 *
 * @author haye
 * @date 2026-03-24
 */
@Slf4j
public class JT809Server implements SmartLifecycle {
    
    private final ServerProperties config;
    private final ServerHandler serverHandler;
    private final SessionManager sessionManager;
    
    // Netty组件
    private EventLoopGroup bossGroup;
    private EventLoopGroup workerGroup;
    private EventLoopGroup businessGroup;
    private Channel serverChannel;
    
    // 生命周期状态
    private volatile boolean running = false;
    
    public JT809Server(ServerProperties config, SessionManager sessionManager) {
        this.config = config;
        this.sessionManager = sessionManager;
        this.serverHandler = new ServerHandler(config, sessionManager);
    }
    
    /**
     * 启动服务端
     */
    @Override
    public void start() {
        if (running) {
            return;
        }
        
        log.info("Starting JT809 Server on port: {}", config.getPort());
        
        try {
            // 1. 创建EventLoopGroup
            bossGroup = new NioEventLoopGroup(config.getBossThreads(),
                new DefaultThreadFactory("jt809-server-boss"));
            
            workerGroup = new NioEventLoopGroup(config.getWorkerThreads(),
                new DefaultThreadFactory("jt809-server-worker"));
            
            businessGroup = new NioEventLoopGroup(config.getBusinessThreads(),
                new DefaultThreadFactory("jt809-server-business"));
            
            // 2. 配置ServerBootstrap
            ServerBootstrap bootstrap = new ServerBootstrap();
            bootstrap.group(bossGroup, workerGroup)
                .channel(NioServerSocketChannel.class)
                .option(ChannelOption.SO_BACKLOG, config.getSoBacklog())
                .option(ChannelOption.SO_REUSEADDR, config.isSoReuseaddr())
                .childOption(ChannelOption.TCP_NODELAY, config.isTcpNodelay())
                .childOption(ChannelOption.SO_KEEPALIVE, config.isSoKeepalive())
                .childOption(ChannelOption.SO_RCVBUF, config.getSoRcvbuf())
                .childOption(ChannelOption.SO_SNDBUF, config.getSoSndbuf())
                .childOption(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT)
                .childHandler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel ch) {
                        initChannelPipeline(ch);
                    }
                });
            
            // 3. 绑定端口
            ChannelFuture future = bootstrap.bind(config.getPort()).sync();
            serverChannel = future.channel();
            
            running = true;
            log.info("JT809 Server started successfully on port: {}", config.getPort());
            
        } catch (InterruptedException e) {
            log.error("Start JT809 Server failed", e);
            shutdown();
            Thread.currentThread().interrupt();
            throw new RuntimeException("Start JT809 Server failed", e);
        }
    }
    
    /**
     * 初始化Channel Pipeline
     */
    private void initChannelPipeline(SocketChannel ch) {
        ChannelPipeline pipeline = ch.pipeline();
        
        // 1. 空闲检测
        pipeline.addLast("idleState",
            new IdleStateHandler(config.getIdleTimeout(), 0, 0, TimeUnit.SECONDS));
        
        // 2. 编解码器
        pipeline.addLast("decoder", new JT809Decoder());
        pipeline.addLast("encoder", new JT809Encoder());
        
        // 3. 业务处理器（使用业务线程池）
        pipeline.addLast(businessGroup, "serverHandler", serverHandler);
        
        // 4. 空闲处理
        pipeline.addLast("idleHandler", new ServerIdleHandler());
        
        // 5. 异常处理
        pipeline.addLast("exceptionHandler", new ServerExceptionHandler());
        
        // 6. 日志处理器（可选）
        if (config.isEnableLog()) {
            pipeline.addLast("logging", new LoggingHandler(LogLevel.DEBUG));
        }
    }
    
    /**
     * 停止服务端
     */
    @Override
    public void stop() {
        if (!running) {
            return;
        }
        
        log.info("Stopping JT809 Server...");
        
        // 1. 关闭服务端Channel
        if (serverChannel != null) {
            serverChannel.close().awaitUninterruptibly();
        }
        
        // 2. 优雅关闭EventLoopGroup
        shutdown();
        
        running = false;
        log.info("JT809 Server stopped");
    }
    
    /**
     * 优雅关闭
     */
    private void shutdown() {
        if (bossGroup != null) {
            bossGroup.shutdownGracefully(100, 300, TimeUnit.MILLISECONDS);
        }
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
    
    /**
     * 获取服务端处理器（用于测试或扩展）
     */
    public ServerHandler getServerHandler() {
        return serverHandler;
    }
}
