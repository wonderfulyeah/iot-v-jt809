package org.iot.v.jt809.example;

import lombok.extern.slf4j.Slf4j;
import org.iot.v.jt809.client.JT809Client;
import org.iot.v.jt809.client.config.ClientProperties;
import org.iot.v.jt809.core.constant.MessageType;
import org.iot.v.jt809.core.message.base.BaseMessage;
import org.iot.v.jt809.core.message.upstream.vehicle.VehicleAlarmMsg;
import org.iot.v.jt809.core.message.upstream.vehicle.VehicleDynamicMsg;
import org.iot.v.jt809.core.session.Session;
import org.iot.v.jt809.core.session.SessionManager;
import org.iot.v.jt809.handler.HandlerChain;
import org.iot.v.jt809.handler.annotation.JT809Handler;
import org.iot.v.jt809.handler.builtin.AbstractMessageHandler;
import org.iot.v.jt809.handler.context.MessageContext;
import org.iot.v.jt809.server.JT809Server;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

/**
 * JT809 Spring Boot 集成测试
 * 使用 @JT809Handler 注解的处理器处理消息
 *
 * @author haye
 * @date 2026-03-26
 */
@Slf4j
@SpringBootTest(classes = ExampleApplication.class)
@Import(JT809SpringBootIntegrationTest.TestHandlerConfiguration.class)
@ActiveProfiles("integration")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class JT809SpringBootIntegrationTest {

    @Autowired
    private JT809Server server;

    @Autowired
    private SessionManager sessionManager;

    @Autowired
    private HandlerChain handlerChain;

    private static JT809Client client;
    private static final long PLATFORM_ID = 1000001L;
    private static final String PASSWORD = "123456";

    // 静态计数器，用于测试处理器验证
    private static final AtomicInteger locationMessageCount = new AtomicInteger(0);
    private static final AtomicInteger alarmMessageCount = new AtomicInteger(0);
    private static final AtomicReference<BaseMessage> lastLocationMessage = new AtomicReference<>();
    private static final AtomicReference<BaseMessage> lastAlarmMessage = new AtomicReference<>();

    /**
     * 测试配置类 - 注册测试用的处理器
     */
    @TestConfiguration
    static class TestHandlerConfiguration {

        /**
         * 测试用车辆定位消息处理器
         */
        @Bean
        public TestLocationHandler testLocationHandler() {
            return new TestLocationHandler();
        }

        /**
         * 测试用车辆报警消息处理器
         */
        @Bean
        public TestAlarmHandler testAlarmHandler() {
            return new TestAlarmHandler();
        }
    }

    /**
     * 测试用车辆定位消息处理器
     */
    @Slf4j
    @JT809Handler(messageTypes = {MessageType.VEHICLE_LOCATION}, order = 1)
    static class TestLocationHandler extends AbstractMessageHandler {

        @Override
        protected boolean doHandle(MessageContext context, BaseMessage message) {
            int count = locationMessageCount.incrementAndGet();
            lastLocationMessage.set(message);
            
            VehicleDynamicMsg locationMsg = (VehicleDynamicMsg) message;
            VehicleDynamicMsg.Body body = (VehicleDynamicMsg.Body) locationMsg.getBody();
            
            log.info("[TestLocationHandler] 处理车辆定位消息 #{}: 车牌={}, msgId=0x{}",
                count, 
                body != null ? body.getVehicleNo() : "N/A",
                Integer.toHexString(message.getMsgId()));
            
            return true;
        }

        @Override
        public int[] supportedMessageTypes() {
            return new int[]{MessageType.VEHICLE_LOCATION};
        }
    }

    /**
     * 测试用车辆报警消息处理器
     */
    @Slf4j
    @JT809Handler(messageTypes = {MessageType.VEHICLE_ALARM}, order = 1)
    static class TestAlarmHandler extends AbstractMessageHandler {

        @Override
        protected boolean doHandle(MessageContext context, BaseMessage message) {
            int count = alarmMessageCount.incrementAndGet();
            lastAlarmMessage.set(message);
            
            VehicleAlarmMsg alarmMsg = (VehicleAlarmMsg) message;
            VehicleAlarmMsg.Body body = (VehicleAlarmMsg.Body) alarmMsg.getBody();
            
            log.info("[TestAlarmHandler] 处理车辆报警消息 #{}: 车牌={}, msgId=0x{}",
                count,
                body != null ? body.getVehicleNo() : "N/A",
                Integer.toHexString(message.getMsgId()));
            
            return true;
        }

        @Override
        public int[] supportedMessageTypes() {
            return new int[]{MessageType.VEHICLE_ALARM};
        }
    }

    @BeforeAll
    static void setUp() {
        log.info("=== 开始 Spring Boot 集成测试 ===");
        // 重置计数器
        locationMessageCount.set(0);
        alarmMessageCount.set(0);
    }

    @AfterAll
    static void tearDown() {
        log.info("=== 结束 Spring Boot 集成测试 ===");

        if (client != null) {
            log.info("停止客户端...");
            client.stop();
        }
    }

    @Test
    @Order(1)
    @DisplayName("验证 HandlerChain 已注册处理器")
    void testHandlerChainConfigured() {
        assertNotNull(handlerChain, "HandlerChain 应该被 Spring 容器注入");
        assertTrue(handlerChain.size() > 0, "HandlerChain 应该包含处理器");
        log.info("HandlerChain 包含 {} 个处理器", handlerChain.size());
        
        handlerChain.getHandlers().forEach(handler -> 
            log.info("已注册处理器: {}, order={}", handler.getName(), handler.getOrder()));
    }

    @Test
    @Order(2)
    @DisplayName("服务端状态检查")
    void testServerIsRunning() {
        assertNotNull(server, "服务端应该被Spring容器注入");
        assertTrue(server.isRunning(), "服务端应该处于运行状态");
        log.info("服务端运行状态: OK, 端口配置读取自application-integration.yml");
    }

    @Test
    @Order(3)
    @DisplayName("创建并连接客户端")
    void testClientConnect() throws Exception {
        // 创建客户端配置
        ClientProperties clientProps = createClientProperties();
        client = new JT809Client(clientProps, sessionManager);

        log.info("启动客户端...");
        client.start();

        // 等待客户端连接完成和登录
        TimeUnit.SECONDS.sleep(3);

        assertTrue(client.isConnected(), "客户端应该已连接");
        log.info("客户端连接状态: OK, 登录状态: {}", client.isLoginSuccess());
    }

    @Test
    @Order(4)
    @DisplayName("会话创建检查")
    void testSessionCreated() {
        // 等待会话创建
        awaitCondition(() -> sessionManager.getSessionCount() > 0, 5);

        int sessionCount = sessionManager.getSessionCount();
        log.info("当前会话数量: {}", sessionCount);

        assertTrue(sessionCount > 0, "应该至少有一个会话");

        for (Session s : sessionManager.getAllSessions()) {
            log.info("会话信息: sessionId={}, platformId={}, state={}",
                s.getSessionId(), s.getPlatformId(), s.getState());
        }
    }

    @Test
    @Order(5)
    @DisplayName("通过 @JT809Handler 处理车辆定位消息")
    void testVehicleLocationHandlerInvoked() throws Exception {
        // 重置计数器
        locationMessageCount.set(0);

        // 创建车辆定位消息
        VehicleDynamicMsg message = createVehicleLocationMessage();

        log.info("[客户端] 发送车辆定位消息...");
        client.send(message);

        // 等待处理器被调用
        TimeUnit.SECONDS.sleep(2);

        int count = locationMessageCount.get();
        log.info("TestLocationHandler 被调用次数: {}", count);

        assertTrue(count >= 1, "@JT809Handler 处理器应该被调用");

        BaseMessage received = lastLocationMessage.get();
        assertNotNull(received, "收到的消息不应为空");
        assertEquals(MessageType.VEHICLE_LOCATION, received.getMsgId(), "消息ID应该匹配");

        log.info("[测试] @JT809Handler 车辆定位消息处理器测试通过!");
    }

    @Test
    @Order(6)
    @DisplayName("通过 @JT809Handler 处理车辆报警消息")
    void testVehicleAlarmHandlerInvoked() throws Exception {
        // 重置计数器
        alarmMessageCount.set(0);

        // 创建车辆报警消息
        VehicleAlarmMsg message = createVehicleAlarmMessage();

        log.info("[客户端] 发送车辆报警消息...");
        client.send(message);

        // 等待处理器被调用
        TimeUnit.SECONDS.sleep(2);

        int count = alarmMessageCount.get();
        log.info("TestAlarmHandler 被调用次数: {}", count);

        assertTrue(count >= 1, "@JT809Handler 处理器应该被调用");

        BaseMessage received = lastAlarmMessage.get();
        assertNotNull(received, "收到的消息不应为空");
        assertEquals(MessageType.VEHICLE_ALARM, received.getMsgId(), "消息ID应该匹配");

        VehicleAlarmMsg alarmMsg = (VehicleAlarmMsg) received;
        VehicleAlarmMsg.Body body = (VehicleAlarmMsg.Body) alarmMsg.getBody();
        assertNotNull(body, "消息体不应为空");
        assertEquals("京A12345", body.getVehicleNo(), "车牌号应该匹配");

        log.info("[测试] @JT809Handler 车辆报警消息处理器测试通过!");
    }

    /**
     * 创建客户端配置
     */
    private ClientProperties createClientProperties() {
        ClientProperties props = new ClientProperties();
        props.setServerIp("127.0.0.1");
        props.setServerPort(19000);
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
     * 创建车辆定位消息
     */
    private VehicleDynamicMsg createVehicleLocationMessage() {
        VehicleDynamicMsg message = new VehicleDynamicMsg();
        VehicleDynamicMsg.Body body = new VehicleDynamicMsg.Body();

        body.setVehicleNo("京A12345");
        body.setVehicleColor(1);
        body.setSubBusinessType(0x1202);

        VehicleDynamicMsg.LocationData locationData = new VehicleDynamicMsg.LocationData();

        LocalDateTime now = LocalDateTime.now();
        locationData.setTime(String.format("%02d%02d%02d%02d%02d%02d",
            now.getYear() % 100, now.getMonthValue(), now.getDayOfMonth(),
            now.getHour(), now.getMinute(), now.getSecond()));

        locationData.setLongitude(116397499);
        locationData.setLatitude(39909356);
        locationData.setSpeed(600);
        locationData.setTachoSpeed(600);
        locationData.setDirection(90);
        locationData.setAltitude(50);
        locationData.setVehicleStatus(0);
        locationData.setAlarmStatus(0);

        body.setLocationData(locationData);
        body.setSubsequentDataLength(28);

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

        body.setVehicleNo("京A12345");
        body.setVehicleColor(1);
        body.setSubBusinessType(0x1401);

        VehicleAlarmMsg.AlarmData alarmData = new VehicleAlarmMsg.AlarmData();

        alarmData.setAlarmSource(1);
        alarmData.setAlarmType(0x00000001L);

        LocalDateTime now = LocalDateTime.now();
        alarmData.setAlarmTime(String.format("%02d%02d%02d%02d%02d%02d",
            now.getYear() % 100, now.getMonthValue(), now.getDayOfMonth(),
            now.getHour(), now.getMinute(), now.getSecond()));

        alarmData.setLongitude(116397499);
        alarmData.setLatitude(39909356);
        alarmData.setSpeed(800);
        alarmData.setDirection(90);
        alarmData.setAltitude(50);
        alarmData.setVehicleStatus(0);
        alarmData.setAlarmDesc("车辆超速报警");

        body.setAlarmData(alarmData);

        message.setBody(body);
        message.getHead().setPlatformId(PLATFORM_ID);

        return message;
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
