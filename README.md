# JT809 数据级联框架

基于 **Spring Boot 2.6.3** 和 **Netty 4.1.72.Final** 的 JT809 协议数据级联 Spring Boot Starter。

## 项目简介

本项目实现了完整的 JT809 协议数据级联功能，支持：
- 作为上级平台接收下级平台的数据上报
- 作为下级平台向上级平台注册并上报数据
- 灵活的消息处理机制，支持自定义业务逻辑
- 开箱即用的 Spring Boot Starter 集成

## 核心特性

✨ **基于 Netty 高性能网络层**
- 异步事件驱动架构
- 零拷贝优化
- 池化 ByteBuf 管理
- 高效的内存使用

🚀 **高性能设计**
- 支持高并发连接
- 优化的线程模型
- 灵活的编解码器
- 批量消息处理

🔧 **灵活配置**
- 完整的配置选项
- 支持上级/下级双模式
- 可自定义消息处理器
- 支持扩展加密算法

📦 **开箱即用**
- Spring Boot Starter 自动配置
- 完善的生命周期管理
- 内置心跳保活机制
- 自动断线重连

## 技术栈

| 技术 | 版本 | 说明 |
|------|------|------|
| Java | 8+ | 开发语言 |
| Spring Boot | 2.6.3 | 应用框架 |
| Netty | 4.1.72.Final | 网络通信框架 |
| Spring Framework | 5.3.15 | Spring核心 |
| Lombok | 1.18.22 | 代码简化 |

## 模块结构

```
iot-v-jt809/
├── jt809-core                    # 核心模块：协议实现、编解码
├── jt809-server                  # 服务端模块：上级平台
├── jt809-client                  # 客户端模块：下级平台
├── jt809-handler                 # 处理器模块：消息处理链
├── jt809-spring-boot-starter     # 自动配置：Spring Boot集成
└── jt809-example                 # 示例项目：使用演示
```

## 快速开始

### 1. 添加依赖

```xml
<dependency>
    <groupId>org.iot.v</groupId>
    <artifactId>jt809-spring-boot-starter</artifactId>
    <version>1.0.0-SNAPSHOT</version>
</dependency>
```

### 2. 配置文件

**作为上级平台**（接收下级连接）：
```yaml
jt809:
  enabled: true
  mode: server
  server:
    port: 9000
    worker-threads: 8
    business-threads: 8
  platforms:
    - platform-id: 1000001
      password: "123456"
      version: "1.0"
```

**作为下级平台**（连接上级平台）：
```yaml
jt809:
  enabled: true
  mode: client
  client:
    server-ip: 192.168.1.100
    server-port: 9000
    platform-id: 1000001
    password: "123456"
    version: "1.0"
    reconnect:
      enabled: true
      interval: 5000
    heartbeat:
      enabled: true
      interval: 60000
```

### 3. 自定义消息处理器

```java
@JT809Handler(messageTypes = {0x1200}, order = 1)
public class VehicleLocationHandler implements MessageHandler {
    
    @Override
    public boolean handle(MessageContext context, BaseMessage message) {
        VehicleLocationMsg locationMsg = (VehicleLocationMsg) message;
        
        // 处理车辆定位数据
        System.out.println("收到车辆定位: " + locationMsg.getBody().getVehicleNo());
        
        return true;
    }
    
    @Override
    public int[] supportedMessageTypes() {
        return new int[]{0x1200};
    }
}
```

### 4. 发送消息

```java
@Service
public class VehicleService {
    
    @Autowired
    private SessionManager sessionManager;
    
    @Autowired
    private JT809Client client;
    
    // 服务端发送给指定平台
    public void sendToPlatform(long platformId, BaseMessage message) {
        Session session = sessionManager.getSessionByPlatformId(platformId);
        if (session != null) {
            session.send(message);
        }
    }
    
    // 客户端发送给上级平台
    public void sendToServer(BaseMessage message) {
        client.send(message);
    }
}
```

## 核心功能

### 1. 消息编解码
- 完整的 JT809 协议编解码实现
- 自动处理转义字符
- CRC 校验
- 支持自定义消息类型

### 2. 会话管理
- 自动管理连接会话
- 支持会话状态监控
- 提供会话属性存储

### 3. 消息处理链
- 支持多个处理器按顺序执行
- 可通过注解定义处理器
- 支持处理器优先级

### 4. 心跳保活
- 自动发送心跳包
- 检测连接状态
- 超时自动断开

### 5. 断线重连
- 自动重连机制
- 可配置重连间隔
- 支持指数退避策略

## 性能优化

### Netty 优化
- ✅ 使用池化 ByteBuf 减少内存分配开销
- ✅ 使用直接内存实现零拷贝
- ✅ 合理配置线程数
- ✅ 优化 TCP 参数

### JVM 参数建议
```bash
-Xms2g -Xmx2g                    # 堆内存
-Xmn1g                           # 年轻代
-XX:MaxDirectMemorySize=512m     # 直接内存
-XX:+UseG1GC                     # G1垃圾收集器
```

## 文档

- [设计文档](docs/设计文档.md) - 详细的架构设计和实现方案
- [项目结构](docs/项目结构.md) - 模块划分和依赖关系
- [开发指南](docs/开发指南.md) - 开发规范和示例代码
- [协议规范](docs/协议规范.md) - JT809 协议详细说明
- [Netty配置指南](docs/Netty配置指南.md) - Netty 配置和优化

## 开发计划

### 短期目标
- [x] 完成设计文档
- [ ] 实现核心模块
- [ ] 实现服务端和客户端
- [ ] 完成 Spring Boot Starter

### 中期目标
- [ ] 支持 JT809-2019 新标准
- [ ] 提供 Web 管理界面
- [ ] 支持分布式部署
- [ ] 提供监控指标

### 长期目标
- [ ] 支持更多协议扩展
- [ ] 提供云原生支持
- [ ] 建立完整的生态系统

## 参考资料

- [JT/T 809-2019 标准](http://std.samr.gov.cn/)
- [Netty 官方文档](https://netty.io/)
- [Spring Boot 官方文档](https://spring.io/projects/spring-boot)

## 许可证

本项目采用 Apache 2.0 许可证。

## 贡献

欢迎提交 Issue 和 Pull Request！

---

**注意**: 本项目目前处于开发阶段，不建议直接用于生产环境。
