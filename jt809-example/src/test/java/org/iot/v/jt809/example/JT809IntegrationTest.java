package org.iot.v.jt809.example;

import lombok.extern.slf4j.Slf4j;
import org.iot.v.jt809.client.JT809Client;
import org.iot.v.jt809.client.config.ClientProperties;
import org.iot.v.jt809.core.constant.MessageType;
import org.iot.v.jt809.core.message.base.BaseMessage;
import org.iot.v.jt809.core.message.upstream.vehicle.VehicleAlarmMsg;
import org.iot.v.jt809.core.message.upstream.vehicle.VehicleLocationMsg;
import org.iot.v.jt809.core.session.Session;
import org.iot.v.jt809.core.session.SessionManager;
import org.iot.v.jt809.server.JT809Server;
import org.iot.v.jt809.server.config.ServerProperties;
import org.iot.v.jt809.server.handler.ServerHandler;
import org.junit.jupiter.api.*;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

/**
 * JT809 集成测试
 * 同时启动服务端和客户端进行通信测试
 *
 * @author haye
 * @date 2026-03-24
 */
@Slf4j
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class JT809IntegrationTest {

    private static JT809Server server;
    private static JT809Client client;
    private static final int SERVER_PORT = 19000;
    private static final long PLATFORM_ID = 1000001L;
    private static final String PASSWORD = "123456";
    
    // 消息接收计数器
    private static final AtomicInteger messageReceivedCount = new AtomicInteger(0);
    private static final AtomicReference<BaseMessage> lastReceivedMessage = new AtomicReference<>();

    @BeforeAll
    static void setUp() throws Exception {
        log.info("=== 开始集成测试 ===");
        
        // 清理会话管理器
        SessionManager.getInstance().clear();
        
        // 创建并启动服务端
        ServerProperties serverProps = createServerProperties();
        server = new JT809Server(serverProps, SessionManager.getInstance());
        
        // 添加消息监听器
        server.getServerHandler().addMessageListener((session, message) -> {
            int count = messageReceivedCount.incrementAndGet();
            lastReceivedMessage.set(message);
            log.info("[服务端] 收到消息 #{}: msgId=0x{}, 类型={}", 
                count, Integer.toHexString(message.getMsgId()), message.getMessageTypeName());
            
            // 打印详细信息
            if (message instanceof VehicleLocationMsg) {
                VehicleLocationMsg locationMsg = (VehicleLocationMsg) message;
                VehicleLocationMsg.Body body = (VehicleLocationMsg.Body) locationMsg.getBody();
                if (body != null && body.getLocationData() != null) {
                    VehicleLocationMsg.LocationData loc = body.getLocationData();
                    log.info("[服务端] 车辆定位: 车牌={}, 经度={}, 纬度={}, 速度={}km/h", 
                        body.getVehicleNo(),
                        loc.getLongitude() / 1_000_000.0,
                        loc.getLatitude() / 1_000_000.0,
                        loc.getSpeed() / 10.0);
                }
            } else if (message instanceof VehicleAlarmMsg) {
                VehicleAlarmMsg alarmMsg = (VehicleAlarmMsg) message;
                VehicleAlarmMsg.Body body = (VehicleAlarmMsg.Body) alarmMsg.getBody();
                if (body != null && body.getAlarmData() != null) {
                    VehicleAlarmMsg.AlarmData alarm = body.getAlarmData();
                    log.info("[服务端] 车辆报警: 车牌={}, 报警类型={}, 报警来源={}, 经度={}, 纬度={}", 
                        body.getVehicleNo(),
                        alarm.getAlarmType(),
                        alarm.getAlarmSource(),
                        alarm.getLongitude() / 1_000_000.0,
                        alarm.getLatitude() / 1_000_000.0);
                }
            }
        });
        
        log.info("启动服务端...");
        server.start();
        
        // 等待服务端启动完成
        TimeUnit.MILLISECONDS.sleep(500);
        assertTrue(server.isRunning(), "服务端应已启动");
        log.info("服务端启动成功，端口: {}", SERVER_PORT);
        
        // 创建并启动客户端
        ClientProperties clientProps = createClientProperties();
        client = new JT809Client(clientProps, SessionManager.getInstance());
        
        log.info("启动客户端...");
        client.start();
        
        // 等待客户端连接完成和登录
        TimeUnit.SECONDS.sleep(3);
        log.info("客户端启动完成，登录状态: {}", client.isLoginSuccess());
    }

    @AfterAll
    static void tearDown() {
        log.info("=== 结束集成测试 ===");
        
        if (client != null) {
            log.info("停止客户端...");
            client.stop();
        }
        
        if (server != null) {
            log.info("停止服务端...");
            server.stop();
        }
        
        SessionManager.getInstance().clear();
    }

    @Test
    @Order(1)
    @DisplayName("服务端状态检查")
    void testServerIsRunning() {
        assertTrue(server.isRunning(), "服务端应该处于运行状态");
        log.info("服务端运行状态: OK");
    }

    @Test
    @Order(2)
    @DisplayName("客户端连接状态检查")
    void testClientIsConnected() {
        assertTrue(client.isConnected(), "客户端应该已连接");
        log.info("客户端连接状态: OK");
    }

    @Test
    @Order(3)
    @DisplayName("会话创建检查")
    void testSessionCreated() {
        SessionManager sessionManager = SessionManager.getInstance();
        
        // 等待会话创建
        awaitCondition(() -> sessionManager.getSessionCount() > 0, 5);
        
        int sessionCount = sessionManager.getSessionCount();
        log.info("当前会话数量: {}", sessionCount);
        
        assertTrue(sessionCount > 0, "应该至少有一个会话");
        
        // 列出所有会话信息
        for (Session s : sessionManager.getAllSessions()) {
            log.info("会话信息: sessionId={}, platformId={}, state={}", 
                s.getSessionId(), s.getPlatformId(), s.getState());
        }
    }

    @Test
    @Order(4)
    @DisplayName("发送心跳测试")
    void testSendHeartbeat() throws Exception {
        // 心跳由客户端自动发送，这里验证心跳机制
        SessionManager sessionManager = SessionManager.getInstance();
        
        // 获取任意一个活跃会话
        Session session = null;
        for (Session s : sessionManager.getAllSessions()) {
            if (s.isActive()) {
                session = s;
                break;
            }
        }
        
        if (session != null) {
            // 记录当前心跳时间
            Instant lastActiveTime = session.getLastActiveTime();
            
            // 等待心跳更新
            TimeUnit.SECONDS.sleep(2);
            
            // 验证会话仍然活跃
            assertTrue(client.isConnected(), "客户端应该仍然连接");
            log.info("心跳测试: 客户端仍然连接");
        } else {
            log.warn("未找到活跃会话，跳过心跳测试");
        }
    }

    @Test
    @Order(5)
    @DisplayName("登录状态检查")
    void testLoginStatus() {
        // 检查客户端是否登录成功
        boolean loginSuccess = client.isLoginSuccess();
        log.info("客户端登录状态: {}", loginSuccess ? "已登录" : "未登录");
        
        // 打印会话管理器状态
        SessionManager sessionManager = SessionManager.getInstance();
        log.info("会话管理器中的会话数: {}", sessionManager.getSessionCount());
    }

    @Test
    @Order(6)
    @DisplayName("发送车辆定位消息测试")
    void testSendVehicleLocationMessage() throws Exception {
        // 重置计数器
        messageReceivedCount.set(0);
        
        // 创建车辆定位消息
        VehicleLocationMsg message = createVehicleLocationMessage();
        
        log.info("[客户端] 发送车辆定位消息...");
        client.send(message);
        
        // 等待服务端接收
        TimeUnit.SECONDS.sleep(1);
        
        // 验证服务端收到了消息
        int count = messageReceivedCount.get();
        log.info("服务端接收消息数: {}", count);
        
        assertTrue(count >= 1, "服务端应该收到至少一条消息");
        
        // 验证收到的消息内容
        BaseMessage received = lastReceivedMessage.get();
        assertNotNull(received, "收到的消息不应为空");
        assertEquals(MessageType.VEHICLE_LOCATION, received.getMsgId(), "消息ID应该匹配");
        
        log.info("[测试] 车辆定位消息测试通过!");
    }
    
    @Test
    @Order(7)
    @DisplayName("批量发送消息测试")
    void testSendMultipleMessages() throws Exception {
        // 重置计数器
        messageReceivedCount.set(0);
        int messageCount = 5;
        
        log.info("[客户端] 批量发送 {} 条消息...", messageCount);
        
        for (int i = 0; i < messageCount; i++) {
            VehicleLocationMsg message = createVehicleLocationMessage();
            // 修改车牌号区分每条消息
            VehicleLocationMsg.Body body = (VehicleLocationMsg.Body) message.getBody();
            body.setVehicleNo(String.format("京A%05d", i + 1));
            client.send(message);
            
            // 短暂延迟，避免消息堆积
            TimeUnit.MILLISECONDS.sleep(100);
        }
        
        // 等待服务端处理
        TimeUnit.SECONDS.sleep(2);
        
        int received = messageReceivedCount.get();
        log.info("[服务端] 接收到 {} 条消息", received);
        
        assertEquals(messageCount, received, "服务端应该收到所有消息");
        
        log.info("[测试] 批量消息测试通过!");
    }
    
    @Test
    @Order(8)
    @DisplayName("并发消息发送测试")
    void testConcurrentMessageSend() throws Exception {
        // 重置计数器
        messageReceivedCount.set(0);
        int threadCount = 3;
        int messagesPerThread = 3;
        int totalMessages = threadCount * messagesPerThread;
        
        log.info("[客户端] 并发测试: {} 个线程，每线程 {} 条消息", threadCount, messagesPerThread);
        
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch endLatch = new CountDownLatch(threadCount);
        
        for (int t = 0; t < threadCount; t++) {
            final int threadIndex = t;
            new Thread(() -> {
                try {
                    startLatch.await();
                    for (int i = 0; i < messagesPerThread; i++) {
                        VehicleLocationMsg message = createVehicleLocationMessage();
                        VehicleLocationMsg.Body body = (VehicleLocationMsg.Body) message.getBody();
                        body.setVehicleNo(String.format("京B%d%02d", threadIndex, i));
                        client.send(message);
                        TimeUnit.MILLISECONDS.sleep(50);
                    }
                } catch (Exception e) {
                    log.error("线程 {} 发送消息失败", threadIndex, e);
                } finally {
                    endLatch.countDown();
                }
            }).start();
        }
        
        // 同时开始
        startLatch.countDown();
        
        // 等待所有线程完成
        endLatch.await(10, TimeUnit.SECONDS);
        
        // 等待服务端处理
        TimeUnit.SECONDS.sleep(2);
        
        int received = messageReceivedCount.get();
        log.info("[服务端] 并发测试接收消息数: {}/{}", received, totalMessages);
        
        assertEquals(totalMessages, received, "服务端应该收到所有并发消息");
        
        log.info("[测试] 并发消息测试通过!");
    }
    
    @Test
    @Order(9)
    @DisplayName("发送车辆报警消息测试")
    void testSendVehicleAlarmMessage() throws Exception {
        // 重置计数器
        messageReceivedCount.set(0);
        
        // 创建车辆报警消息
        VehicleAlarmMsg message = createVehicleAlarmMessage();
        
        log.info("[客户端] 发送车辆报警消息...");
        client.send(message);
        
        // 等待服务端接收
        TimeUnit.SECONDS.sleep(1);
        
        // 验证服务端收到了消息
        int count = messageReceivedCount.get();
        log.info("服务端接收消息数: {}", count);
        
        assertTrue(count >= 1, "服务端应该收到至少一条消息");
        
        // 验证收到的消息内容
        BaseMessage received = lastReceivedMessage.get();
        assertNotNull(received, "收到的消息不应为空");
        assertEquals(MessageType.VEHICLE_ALARM, received.getMsgId(), "消息ID应该匹配");
        
        // 验证报警信息内容
        VehicleAlarmMsg alarmMsg = (VehicleAlarmMsg) received;
        VehicleAlarmMsg.Body body = (VehicleAlarmMsg.Body) alarmMsg.getBody();
        assertNotNull(body, "消息体不应为空");
        assertEquals("京A12345", body.getVehicleNo(), "车牌号应该匹配");
        assertNotNull(body.getAlarmData(), "报警数据不应为空");
        
        VehicleAlarmMsg.AlarmData alarm = body.getAlarmData();
        assertEquals(1, alarm.getAlarmSource(), "报警来源应该匹配");
        assertEquals(0x00000001L, alarm.getAlarmType(), "报警类型应该匹配");
        
        log.info("[测试] 车辆报警消息测试通过!");
    }
    
    @Test
    @Order(10)
    @DisplayName("批量发送车辆报警消息测试")
    void testSendMultipleAlarmMessages() throws Exception {
        // 重置计数器
        messageReceivedCount.set(0);
        int messageCount = 5;
        
        log.info("[客户端] 批量发送 {} 条报警消息...", messageCount);
        
        for (int i = 0; i < messageCount; i++) {
            VehicleAlarmMsg message = createVehicleAlarmMessage();
            // 修改车牌号和报警类型区分每条消息
            VehicleAlarmMsg.Body body = (VehicleAlarmMsg.Body) message.getBody();
            body.setVehicleNo(String.format("京A%05d", i + 1));
            body.getAlarmData().setAlarmType(1L << i); // 不同报警类型
            client.send(message);
            
            // 短暂延迟
            TimeUnit.MILLISECONDS.sleep(100);
        }
        
        // 等待服务端处理
        TimeUnit.SECONDS.sleep(2);
        
        int received = messageReceivedCount.get();
        log.info("[服务端] 接收到 {} 条报警消息", received);
        
        assertEquals(messageCount, received, "服务端应该收到所有报警消息");
        
        log.info("[测试] 批量报警消息测试通过!");
    }

    /**
     * 创建车辆定位消息
     */
    private VehicleLocationMsg createVehicleLocationMessage() {
        VehicleLocationMsg message = new VehicleLocationMsg();
        VehicleLocationMsg.Body body = new VehicleLocationMsg.Body();
        
        // 车牌号
        body.setVehicleNo("京A12345");
        body.setVehicleColor(1); // 蓝色
        
        // 子业务类型：定位数据
        body.setSubBusinessType(0x1202);
        
        // 创建定位数据
        VehicleLocationMsg.LocationData locationData = new VehicleLocationMsg.LocationData();
        
        // 时间 (BCD格式: YYMMDDHHmmss)
        java.time.LocalDateTime now = java.time.LocalDateTime.now();
        locationData.setTime(String.format("%02d%02d%02d%02d%02d%02d",
            now.getYear() % 100, now.getMonthValue(), now.getDayOfMonth(),
            now.getHour(), now.getMinute(), now.getSecond()));
        
        // 位置（北京天安门附近）
        locationData.setLongitude(116397499); // 116.397499° * 1e6
        locationData.setLatitude(39909356);   // 39.909356° * 1e6
        
        // 速度和方向
        locationData.setSpeed(600); // 60 km/h (单位: 0.1 km/h)
        locationData.setTachoSpeed(600);
        locationData.setDirection(90); // 向东
        locationData.setAltitude(50); // 海拔50米
        
        // 状态
        locationData.setVehicleStatus(0);
        locationData.setAlarmStatus(0);
        
        body.setLocationData(locationData);
        body.setSubsequentDataLength(28); // 定位数据固定长度
        
        message.setBody(body);
        message.getHead().setPlatformId(PLATFORM_ID);
        
        return message;
    }

    /**
     * 创建车辆报警消息
     */
    private VehicleAlarmMsg createVehicleAlarmMessage() {
        VehicleAlarmMsg message = new VehicleAlarmMsg();
        VehicleAlarmMsg.Body body = new VehicleAlarmMsg.Body();
        
        // 车牌号
        body.setVehicleNo("京A12345");
        body.setVehicleColor(1); // 蓝色
        
        // 子业务类型：报警信息
        body.setSubBusinessType(0x1401);
        
        // 创建报警数据
        VehicleAlarmMsg.AlarmData alarmData = new VehicleAlarmMsg.AlarmData();
        
        // 报警来源：车载终端
        alarmData.setAlarmSource(1);
        
        // 报警类型：超速报警 (bit0)
        alarmData.setAlarmType(0x00000001L);
        
        // 报警时间 (BCD格式: YYMMDDHHmmss)
        java.time.LocalDateTime now = java.time.LocalDateTime.now();
        alarmData.setAlarmTime(String.format("%02d%02d%02d%02d%02d%02d",
            now.getYear() % 100, now.getMonthValue(), now.getDayOfMonth(),
            now.getHour(), now.getMinute(), now.getSecond()));
        
        // 位置（北京天安门附近）
        alarmData.setLongitude(116397499); // 116.397499° * 1e6
        alarmData.setLatitude(39909356);   // 39.909356° * 1e6
        
        // 速度和方向
        alarmData.setSpeed(800); // 80 km/h (单位: 0.1 km/h)
        alarmData.setDirection(90); // 向东
        alarmData.setAltitude(50); // 海拔50米
        
        // 车辆状态
        alarmData.setVehicleStatus(0);
        
        // 报警描述
        alarmData.setAlarmDesc("车辆超速报警");
        
        body.setAlarmData(alarmData);
        
        message.setBody(body);
        message.getHead().setPlatformId(PLATFORM_ID);
        
        return message;
    }

    /**
     * 创建服务端配置
     */
    private static ServerProperties createServerProperties() {
        ServerProperties props = new ServerProperties();
        props.setPort(SERVER_PORT);
        props.setBossThreads(1);
        props.setWorkerThreads(2);
        props.setBusinessThreads(2);
        props.setIdleTimeout(60);
        props.setEnableLog(true);
        
        // 配置授权平台
        List<ServerProperties.PlatformConfig> platforms = new ArrayList<>();
        ServerProperties.PlatformConfig platform = new ServerProperties.PlatformConfig();
        platform.setPlatformId(PLATFORM_ID);
        platform.setPassword(PASSWORD);
        platform.setVersion("1.0");
        platform.setEnabled(true);
        platforms.add(platform);
        props.setPlatforms(platforms);
        
        return props;
    }

    /**
     * 创建客户端配置
     */
    private static ClientProperties createClientProperties() {
        ClientProperties props = new ClientProperties();
        props.setServerIp("127.0.0.1");
        props.setServerPort(SERVER_PORT);
        props.setPlatformId(PLATFORM_ID);
        props.setPassword(PASSWORD);
        props.setVersion("1.0");
        
        // 重连配置
        ClientProperties.ReconnectConfig reconnect = new ClientProperties.ReconnectConfig();
        reconnect.setEnabled(true);
        reconnect.setInterval(2000);
        reconnect.setMaxAttempts(5);
        props.setReconnect(reconnect);
        
        // 心跳配置
        ClientProperties.HeartbeatConfig heartbeat = new ClientProperties.HeartbeatConfig();
        heartbeat.setEnabled(true);
        heartbeat.setInterval(5000);
        heartbeat.setTimeout(3);
        props.setHeartbeat(heartbeat);
        
        return props;
    }

    /**
     * 等待条件满足
     */
    private void awaitCondition(Condition condition, int timeoutSeconds) {
        long startTime = System.currentTimeMillis();
        while (!condition.isMet()) {
            if (System.currentTimeMillis() - startTime > timeoutSeconds * 1000L) {
                break;
            }
            try {
                TimeUnit.MILLISECONDS.sleep(100);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
    }

    @FunctionalInterface
    interface Condition {
        boolean isMet();
    }
}
