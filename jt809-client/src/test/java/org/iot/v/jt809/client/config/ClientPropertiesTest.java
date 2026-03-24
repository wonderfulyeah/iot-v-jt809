package org.iot.v.jt809.client.config;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * ClientProperties 单元测试
 *
 * @author haye
 * @date 2026-03-24
 */
@DisplayName("客户端配置测试")
class ClientPropertiesTest {

    @Test
    @DisplayName("默认配置值")
    void testDefaultValues() {
        ClientProperties props = new ClientProperties();
        
        assertNull(props.getServerIp());
        assertEquals(0, props.getServerPort());
        assertEquals(0L, props.getPlatformId());
        assertNull(props.getPassword());
        assertEquals("1.0", props.getVersion());
        assertNull(props.getLocalIp());
        assertEquals(0, props.getLocalPort());
        assertTrue(props.getWorkerThreads() > 0);
        assertTrue(props.getBusinessThreads() > 0);
        assertTrue(props.isTcpNodelay());
        assertTrue(props.isSoKeepalive());
        assertEquals(10000, props.getConnectTimeout());
        assertEquals(32 * 1024, props.getSoRcvbuf());
        assertEquals(32 * 1024, props.getSoSndbuf());
        assertFalse(props.isEnableLog());
        
        // 重连配置
        assertNotNull(props.getReconnect());
        assertTrue(props.getReconnect().isEnabled());
        assertEquals(5000, props.getReconnect().getInterval());
        assertEquals(10, props.getReconnect().getMaxAttempts());
        assertEquals(1.5, props.getReconnect().getBackoffMultiplier());
        
        // 心跳配置
        assertNotNull(props.getHeartbeat());
        assertTrue(props.getHeartbeat().isEnabled());
        assertEquals(60000, props.getHeartbeat().getInterval());
        assertEquals(3, props.getHeartbeat().getTimeout());
    }

    @Test
    @DisplayName("设置服务器地址")
    void testSetServerAddress() {
        ClientProperties props = new ClientProperties();
        
        props.setServerIp("192.168.1.100");
        props.setServerPort(9000);
        
        assertEquals("192.168.1.100", props.getServerIp());
        assertEquals(9000, props.getServerPort());
    }

    @Test
    @DisplayName("设置平台信息")
    void testSetPlatformInfo() {
        ClientProperties props = new ClientProperties();
        
        props.setPlatformId(12345678L);
        props.setPassword("testPassword");
        props.setVersion("2.0");
        
        assertEquals(12345678L, props.getPlatformId());
        assertEquals("testPassword", props.getPassword());
        assertEquals("2.0", props.getVersion());
    }

    @Test
    @DisplayName("设置连接参数")
    void testSetConnectionOptions() {
        ClientProperties props = new ClientProperties();
        
        props.setConnectTimeout(5000);
        props.setTcpNodelay(false);
        props.setSoKeepalive(false);
        props.setSoRcvbuf(65536);
        props.setSoSndbuf(65536);
        
        assertEquals(5000, props.getConnectTimeout());
        assertFalse(props.isTcpNodelay());
        assertFalse(props.isSoKeepalive());
        assertEquals(65536, props.getSoRcvbuf());
        assertEquals(65536, props.getSoSndbuf());
    }

    @Test
    @DisplayName("设置重连配置")
    void testSetReconnectConfig() {
        ClientProperties props = new ClientProperties();
        
        ClientProperties.ReconnectConfig reconnect = props.getReconnect();
        reconnect.setEnabled(false);
        reconnect.setInterval(3000);
        reconnect.setMaxAttempts(5);
        reconnect.setBackoffMultiplier(2.0);
        
        assertFalse(props.getReconnect().isEnabled());
        assertEquals(3000, props.getReconnect().getInterval());
        assertEquals(5, props.getReconnect().getMaxAttempts());
        assertEquals(2.0, props.getReconnect().getBackoffMultiplier());
    }

    @Test
    @DisplayName("设置心跳配置")
    void testSetHeartbeatConfig() {
        ClientProperties props = new ClientProperties();
        
        ClientProperties.HeartbeatConfig heartbeat = props.getHeartbeat();
        heartbeat.setEnabled(false);
        heartbeat.setInterval(30000);
        heartbeat.setTimeout(5);
        
        assertFalse(props.getHeartbeat().isEnabled());
        assertEquals(30000, props.getHeartbeat().getInterval());
        assertEquals(5, props.getHeartbeat().getTimeout());
    }
}
