# Netty配置指南

## 1. 版本说明

本项目基于以下核心版本：
- **Spring Boot**: 2.6.3
- **Spring Framework**: 5.3.15
- **Netty**: 4.1.72.Final

## 2. Maven依赖配置

### 2.1 核心模块依赖

```xml
<dependencies>
    <!-- Spring Boot Starter -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter</artifactId>
        <version>2.6.3</version>
    </dependency>
    
    <!-- Spring Boot Configuration Processor -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-configuration-processor</artifactId>
        <version>2.6.3</version>
        <optional>true</optional>
    </dependency>
    
    <!-- Netty -->
    <dependency>
        <groupId>io.netty</groupId>
        <artifactId>netty-all</artifactId>
        <version>4.1.72.Final</version>
    </dependency>
    
    <!-- Lombok -->
    <dependency>
        <groupId>org.projectlombok</groupId>
        <artifactId>lombok</artifactId>
        <version>1.18.22</version>
        <optional>true</optional>
    </dependency>
</dependencies>
```

### 2.2 各子模块依赖

#### jt809-core
```xml
<dependencies>
    <!-- Netty (仅核心网络功能) -->
    <dependency>
        <groupId>io.netty</groupId>
        <artifactId>netty-buffer</artifactId>
        <version>${netty.version}</version>
    </dependency>
    <dependency>
        <groupId>io.netty</groupId>
        <artifactId>netty-codec</artifactId>
        <version>${netty.version}</version>
    </dependency>
    <dependency>
        <groupId>io.netty</groupId>
        <artifactId>netty-transport</artifactId>
        <version>${netty.version}</version>
    </dependency>
    
    <!-- SLF4J -->
    <dependency>
        <groupId>org.slf4j</groupId>
        <artifactId>slf4j-api</artifactId>
        <version>1.7.32</version>
    </dependency>
    
    <!-- Lombok -->
    <dependency>
        <groupId>org.projectlombok</groupId>
        <artifactId>lombok</artifactId>
        <optional>true</optional>
    </dependency>
</dependencies>
```

#### jt809-server / jt809-client
```xml
<dependencies>
    <!-- jt809-core -->
    <dependency>
        <groupId>org.iot.v</groupId>
        <artifactId>jt809-core</artifactId>
        <version>${project.version}</version>
    </dependency>
    
    <!-- Netty (完整版) -->
    <dependency>
        <groupId>io.netty</groupId>
        <artifactId>netty-all</artifactId>
        <version>${netty.version}</version>
    </dependency>
    
    <!-- Spring Context (可选) -->
    <dependency>
        <groupId>org.springframework</groupId>
        <artifactId>spring-context</artifactId>
        <version>5.3.15</version>
        <optional>true</optional>
    </dependency>
</dependencies>
```

#### jt809-spring-boot-starter
```xml
<dependencies>
    <!-- Spring Boot Autoconfigure -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-autoconfigure</artifactId>
        <version>2.6.3</version>
    </dependency>
    
    <!-- jt809-core -->
    <dependency>
        <groupId>org.iot.v</groupId>
        <artifactId>jt809-core</artifactId>
        <version>${project.version}</version>
    </dependency>
    
    <!-- jt809-server -->
    <dependency>
        <groupId>org.iot.v</groupId>
        <artifactId>jt809-server</artifactId>
        <version>${project.version}</version>
        <optional>true</optional>
    </dependency>
    
    <!-- jt809-client -->
    <dependency>
        <groupId>org.iot.v</groupId>
        <artifactId>jt809-client</artifactId>
        <version>${project.version}</version>
        <optional>true</optional>
    </dependency>
    
    <!-- jt809-handler -->
    <dependency>
        <groupId>org.iot.v</groupId>
        <artifactId>jt809-handler</artifactId>
        <version>${project.version}</version>
        <optional>true</optional>
    </dependency>
</dependencies>
```

## 3. Netty核心配置

### 3.1 ByteBuf分配器配置

```java
/**
 * Netty ByteBuf分配器配置
 * Spring Boot 2.6.3 + Netty 4.1.72.Final
 */
@Configuration
public class NettyBufferConfig {
    
    @Bean
    @ConditionalOnProperty(prefix = "jt809.server", name = "use-pooled-allocator", 
        havingValue = "true", matchIfMissing = true)
    public ByteBufAllocator pooledByteBufAllocator(JT809Properties properties) {
        ServerConfig config = properties.getServer();
        
        return new PooledByteBufAllocator(
            config.isPreferDirectBuffer(),  // preferDirect
            0,      // nHeapArena (0 = auto)
            0,      // nDirectArena (0 = auto)
            8192,   // pageSize (8KB)
            11,     // maxOrder (pageSize << 11 = 16MB)
            0,      // tinyCacheSize
            256,    // smallCacheSize
            64      // normalCacheSize
        );
    }
    
    @Bean
    @ConditionalOnProperty(prefix = "jt809.server", name = "use-pooled-allocator", 
        havingValue = "false")
    public ByteBufAllocator unpooledByteBufAllocator(JT809Properties properties) {
        ServerConfig config = properties.getServer();
        
        return new UnpooledByteBufAllocator(
            config.isPreferDirectBuffer()
        );
    }
}
```

### 3.2 EventLoopGroup配置

```java
/**
 * Netty EventLoopGroup配置
 */
@Configuration
public class NettyEventLoopConfig {
    
    @Bean(name = "jt809ServerBossGroup")
    @Scope("prototype")
    public EventLoopGroup serverBossGroup(JT809Properties properties) {
        int threads = properties.getServer().getBossThreads();
        
        return new NioEventLoopGroup(threads, 
            new DefaultThreadFactory("jt809-server-boss", true));
    }
    
    @Bean(name = "jt809ServerWorkerGroup")
    @Scope("prototype")
    public EventLoopGroup serverWorkerGroup(JT809Properties properties) {
        int threads = properties.getServer().getWorkerThreads();
        
        return new NioEventLoopGroup(threads, 
            new DefaultThreadFactory("jt809-server-worker", true));
    }
    
    @Bean(name = "jt809ServerBusinessGroup")
    @Scope("prototype")
    public EventLoopGroup serverBusinessGroup(JT809Properties properties) {
        int threads = properties.getServer().getBusinessThreads();
        
        return new NioEventLoopGroup(threads, 
            new DefaultThreadFactory("jt809-server-business", true));
    }
    
    @Bean(name = "jt809ClientWorkerGroup")
    @Scope("prototype")
    public EventLoopGroup clientWorkerGroup(JT809Properties properties) {
        int threads = properties.getClient().getWorkerThreads();
        
        return new NioEventLoopGroup(threads, 
            new DefaultThreadFactory("jt809-client-worker", true));
    }
    
    @Bean(name = "jt809ClientBusinessGroup")
    @Scope("prototype")
    public EventLoopGroup clientBusinessGroup(JT809Properties properties) {
        int threads = properties.getClient().getBusinessThreads();
        
        return new NioEventLoopGroup(threads, 
            new DefaultThreadFactory("jt809-client-business", true));
    }
}
```

### 3.3 Channel选项配置

```java
/**
 * Netty Channel选项配置工具类
 */
public class NettyChannelOptions {
    
    /**
     * 配置服务端Channel选项
     */
    public static void configureServerOptions(ServerBootstrap bootstrap, 
                                               ServerConfig config,
                                               ByteBufAllocator allocator) {
        // 服务端Socket选项
        bootstrap.option(ChannelOption.SO_BACKLOG, config.getSoBacklog());
        bootstrap.option(ChannelOption.SO_REUSEADDR, config.isSoReuseaddr());
        
        // 客户端Socket选项
        bootstrap.childOption(ChannelOption.TCP_NODELAY, config.isTcpNodelay());
        bootstrap.childOption(ChannelOption.SO_KEEPALIVE, config.isSoKeepalive());
        bootstrap.childOption(ChannelOption.SO_RCVBUF, config.getSoRcvbuf());
        bootstrap.childOption(ChannelOption.SO_SNDBUF, config.getSoSndbuf());
        
        // ByteBuf分配器
        bootstrap.childOption(ChannelOption.ALLOCATOR, allocator);
        
        // 写缓冲区水位
        bootstrap.childOption(ChannelOption.WRITE_BUFFER_WATER_MARK,
            new WriteBufferWaterMark(32 * 1024, 64 * 1024));
    }
    
    /**
     * 配置客户端Channel选项
     */
    public static void configureClientOptions(Bootstrap bootstrap,
                                               ClientConfig config,
                                               ByteBufAllocator allocator) {
        bootstrap.option(ChannelOption.TCP_NODELAY, config.isTcpNodelay());
        bootstrap.option(ChannelOption.SO_KEEPALIVE, config.isSoKeepalive());
        bootstrap.option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 
            config.getConnectTimeout());
        bootstrap.option(ChannelOption.SO_RCVBUF, config.getSoRcvbuf());
        bootstrap.option(ChannelOption.SO_SNDBUF, config.getSoSndbuf());
        bootstrap.option(ChannelOption.ALLOCATOR, allocator);
        
        // 写缓冲区水位
        bootstrap.option(ChannelOption.WRITE_BUFFER_WATER_MARK,
            new WriteBufferWaterMark(16 * 1024, 32 * 1024));
    }
}
```

## 4. Pipeline配置

### 4.1 服务端Pipeline配置

```java
/**
 * 服务端Channel Pipeline配置
 */
public class ServerPipelineConfig extends ChannelInitializer<SocketChannel> {
    
    private final JT809Properties.ServerConfig config;
    private final HandlerChain handlerChain;
    private final SessionManager sessionManager;
    private final EventLoopGroup businessGroup;
    
    @Override
    protected void initChannel(SocketChannel ch) {
        ChannelPipeline pipeline = ch.pipeline();
        
        // 1. 空闲检测（Netty 4.1.72.Final）
        pipeline.addLast("idleState", 
            new IdleStateHandler(
                config.getIdleTimeout(),  // 读空闲
                0,                        // 写空闲
                0,                        // 读写空闲
                TimeUnit.SECONDS
            ));
        
        // 2. 编解码器
        pipeline.addLast("decoder", new JT809Decoder());
        pipeline.addLast("encoder", new JT809Encoder());
        
        // 3. 业务处理器（使用业务线程池）
        pipeline.addLast(businessGroup, "serverHandler", 
            new ServerHandler(handlerChain, sessionManager));
        
        // 4. 空闲处理
        pipeline.addLast("idleHandler", new ServerIdleHandler());
        
        // 5. 异常处理
        pipeline.addLast("exceptionHandler", new ServerExceptionHandler());
        
        // 6. 日志（可选）
        if (config.isEnableLog()) {
            pipeline.addLast("logging", 
                new LoggingHandler(LogLevel.DEBUG));
        }
    }
}
```

### 4.2 客户端Pipeline配置

```java
/**
 * 客户端Channel Pipeline配置
 */
public class ClientPipelineConfig extends ChannelInitializer<SocketChannel> {
    
    private final JT809Properties.ClientConfig config;
    private final HandlerChain handlerChain;
    private final SessionManager sessionManager;
    private final EventLoopGroup businessGroup;
    private final JT809Client client;
    
    @Override
    protected void initChannel(SocketChannel ch) {
        ChannelPipeline pipeline = ch.pipeline();
        
        // 1. 空闲检测
        pipeline.addLast("idleState",
            new IdleStateHandler(
                0,  // 读空闲（不检测）
                config.getHeartbeat().getInterval(),  // 写空闲
                0,  // 读写空闲
                TimeUnit.MILLISECONDS
            ));
        
        // 2. 编解码器
        pipeline.addLast("decoder", new JT809Decoder());
        pipeline.addLast("encoder", new JT809Encoder());
        
        // 3. 业务处理器（使用业务线程池）
        pipeline.addLast(businessGroup, "clientHandler",
            new ClientHandler(handlerChain, sessionManager));
        
        // 4. 重连处理器
        pipeline.addLast("reconnectHandler",
            new ReconnectHandler(client));
        
        // 5. 心跳处理器
        pipeline.addLast("heartbeatHandler",
            new HeartbeatHandler(client));
        
        // 6. 日志（可选）
        if (config.isEnableLog()) {
            pipeline.addLast("logging",
                new LoggingHandler(LogLevel.DEBUG));
        }
    }
}
```

## 5. ByteBuf使用规范

### 5.1 正确的ByteBuf使用方式

```java
/**
 * ByteBuf使用规范示例
 */
public class ByteBufUsageExample {
    
    /**
     * 错误示例：忘记释放ByteBuf
     */
    public void wrongUsage(ChannelHandlerContext ctx) {
        ByteBuf buffer = ctx.alloc().buffer(1024);
        buffer.writeInt(100);
        
        // 忘记release，导致内存泄漏！
    }
    
    /**
     * 正确示例1：使用try-finally
     */
    public void correctUsage1(ChannelHandlerContext ctx) {
        ByteBuf buffer = ctx.alloc().buffer(1024);
        try {
            buffer.writeInt(100);
            ctx.writeAndFlush(buffer);
        } finally {
            // 注意：writeAndFlush后会自动release，不需要手动release
            // 如果不发送，必须手动release
        }
    }
    
    /**
     * 正确示例2：使用ReferenceCountUtil
     */
    public void correctUsage2(ByteBuf buffer) {
        try {
            // 处理数据
            int value = buffer.readInt();
            // ...
        } finally {
            ReferenceCountUtil.release(buffer);
        }
    }
    
    /**
     * 正确示例3：使用slice/duplicate（共享引用计数）
     */
    public void correctUsage3(ByteBuf source) {
        // slice和duplicate共享引用计数
        ByteBuf slice = source.slice(0, 10);
        ByteBuf duplicate = source.duplicate();
        
        // 只需要release原始ByteBuf
        ReferenceCountUtil.release(source);
        // slice和duplicate会自动释放
    }
    
    /**
     * 正确示例4：使用CompositeByteBuf
     */
    public void correctUsage4(ByteBuf buf1, ByteBuf buf2) {
        CompositeByteBuf composite = Unpooled.wrappedBuffer(buf1, buf2);
        
        // composite会增加buf1和buf2的引用计数
        // 使用完后只需release composite
        ReferenceCountUtil.release(composite);
    }
}
```

### 5.2 内存泄漏检测

```java
/**
 * ByteBuf内存泄漏检测配置
 */
@Configuration
public class ByteBufLeakDetectionConfig {
    
    /**
     * 启用内存泄漏检测
     * 在开发环境启用，生产环境可关闭
     */
    @PostConstruct
    public void enableLeakDetection() {
        // 级别说明：
        // - DISABLED: 禁用检测
        // - SIMPLE: 简单检测（默认，性能影响小）
        // - ADVANCED: 高级检测（性能影响中等）
        // - PARANOID: 偏执模式（检测所有ByteBuf，性能影响大）
        
        // 开发环境使用ADVANCED或PARANOID
        ResourceLeakDetector.setLevel(ResourceLeakDetector.Level.ADVANCED);
    }
}
```

## 6. 性能调优

### 6.1 JVM参数配置

```bash
#!/bin/bash
# 启动脚本示例

# 堆内存
JAVA_OPTS="$JAVA_OPTS -Xms2g"
JAVA_OPTS="$JAVA_OPTS -Xmx2g"

# 年轻代
JAVA_OPTS="$JAVA_OPTS -Xmn1g"

# 直接内存（重要！Netty使用）
JAVA_OPTS="$JAVA_OPTS -XX:MaxDirectMemorySize=512m"

# GC配置（G1GC推荐）
JAVA_OPTS="$JAVA_OPTS -XX:+UseG1GC"
JAVA_OPTS="$JAVA_OPTS -XX:MaxGCPauseMillis=200"
JAVA_OPTS="$JAVA_OPTS -XX:G1HeapRegionSize=4m"

# GC日志
JAVA_OPTS="$JAVA_OPTS -Xlog:gc*:file=logs/gc.log:time,uptime:filecount=5,filesize=10m"

# 元空间
JAVA_OPTS="$JAVA_OPTS -XX:MetaspaceSize=128m"
JAVA_OPTS="$JAVA_OPTS -XX:MaxMetaspaceSize=256m"

# 启动应用
java $JAVA_OPTS -jar jt809-server.jar
```

### 6.2 Netty性能监控

```java
/**
 * Netty性能监控
 */
@Component
public class NettyPerformanceMonitor {
    
    @Autowired
    private MeterRegistry meterRegistry;
    
    /**
     * 监控ByteBuf内存使用
     */
    @Scheduled(fixedRate = 5000)
    public void monitorMemory() {
        ByteBufAllocator allocator = PooledByteBufAllocator.DEFAULT;
        
        // 堆内存使用
        long heapUsed = allocator.metric().usedHeapMemory();
        meterRegistry.gauge("netty.memory.heap.used", heapUsed);
        
        // 直接内存使用
        long directUsed = allocator.metric().usedDirectMemory();
        meterRegistry.gauge("netty.memory.direct.used", directUsed);
        
        // 总内存使用
        meterRegistry.gauge("netty.memory.total.used", heapUsed + directUsed);
    }
    
    /**
     * 监控Channel状态
     */
    public void monitorChannel(Channel channel) {
        // 写队列大小
        ChannelOutboundBuffer outboundBuffer = channel.unsafe().outboundBuffer();
        if (outboundBuffer != null) {
            int pendingWriteSize = outboundBuffer.totalPendingWriteBytes();
            meterRegistry.gauge("netty.channel.pending.write", pendingWriteSize);
        }
    }
}
```

## 7. 常见问题

### 7.1 Spring Boot 2.6.3与Netty版本兼容性

**问题**: Spring Boot 2.6.3默认使用Netty 4.1.68.Final，本项目使用4.1.72.Final

**解决方案**:
```xml
<properties>
    <netty.version>4.1.72.Final</netty.version>
</properties>

<dependencyManagement>
    <dependencies>
        <!-- 覆盖Spring Boot管理的Netty版本 -->
        <dependency>
            <groupId>io.netty</groupId>
            <artifactId>netty-bom</artifactId>
            <version>${netty.version}</version>
            <type>pom</type>
            <scope>import</scope>
        </dependency>
    </dependencies>
</dependencyManagement>
```

### 7.2 ByteBuf泄漏问题

**问题**: 日志中出现`LEAK: ByteBuf.release() was not called before it's garbage-collected`

**解决方案**:
1. 启用泄漏检测：`ResourceLeakDetector.setLevel(Level.PARANOID)`
2. 检查所有ByteBuf的创建和释放
3. 使用`ReferenceCountUtil.release()`确保释放
4. 避免在循环中创建ByteBuf而不释放

### 7.3 直接内存溢出

**问题**: `OutOfMemoryError: Direct buffer memory`

**解决方案**:
1. 增大直接内存：`-XX:MaxDirectMemorySize=1g`
2. 使用池化ByteBuf
3. 检查ByteBuf是否正确释放
4. 监控直接内存使用情况

### 7.4 线程数过多

**问题**: Netty创建了过多线程

**解决方案**:
1. 合理配置线程数（参考CPU核心数）
2. 使用共享的EventLoopGroup
3. 避免每次创建新的EventLoopGroup

## 8. 最佳实践

### 8.1 ByteBuf使用最佳实践
1. ✅ 使用池化ByteBuf（`PooledByteBufAllocator`）
2. ✅ 优先使用直接内存（`preferDirect = true`）
3. ✅ 及时释放ByteBuf（`ReferenceCountUtil.release()`）
4. ✅ 使用`slice`和`duplicate`避免拷贝
5. ❌ 避免使用`Unpooled.buffer()`创建非池化ByteBuf
6. ❌ 避免忘记release导致内存泄漏

### 8.2 线程配置最佳实践
1. ✅ Boss线程数设置为1
2. ✅ Worker线程数设置为CPU核心数 * 2
3. ✅ 业务处理使用独立的业务线程池
4. ✅ 给线程设置有意义的名称
5. ❌ 避免在I/O线程执行耗时操作

### 8.3 TCP参数配置最佳实践
1. ✅ 启用`TCP_NODELAY`
2. ✅ 启用`SO_KEEPALIVE`
3. ✅ 合理设置缓冲区大小
4. ✅ 使用写缓冲区水位控制
5. ❌ 避免设置过大的缓冲区

### 8.4 Spring集成最佳实践
1. ✅ 实现`SmartLifecycle`管理生命周期
2. ✅ 使用`@ConfigurationProperties`管理配置
3. ✅ 提供合理的默认配置
4. ✅ 支持`@ConditionalOnProperty`条件装配
5. ❌ 避免硬编码配置

## 9. 参考资料

- [Netty官方文档](https://netty.io/wiki/user-guide.html)
- [Netty 4.1.72.Final API文档](https://netty.io/4.1/api/)
- [Spring Boot 2.6.3文档](https://docs.spring.io/spring-boot/docs/2.6.3/reference/html/)
- [ByteBuf使用指南](https://netty.io/wiki/reference-counted-objects.html)
- [Netty性能优化](https://netty.io/wiki/performance.html)
