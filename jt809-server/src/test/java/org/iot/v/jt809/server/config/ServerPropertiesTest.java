package org.iot.v.jt809.server.config;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * ServerProperties 单元测试
 *
 * @author haye
 * @date 2026-03-24
 */
@DisplayName("服务端配置测试")
class ServerPropertiesTest {

    @Test
    @DisplayName("默认配置值")
    void testDefaultValues() {
        ServerProperties props = new ServerProperties();
        
        assertEquals(9000, props.getPort());
        assertEquals(1, props.getBossThreads());
        assertTrue(props.getWorkerThreads() > 0);
        assertTrue(props.getBusinessThreads() > 0);
        assertEquals(1024, props.getSoBacklog());
        assertTrue(props.isSoReuseaddr());
        assertTrue(props.isTcpNodelay());
        assertTrue(props.isSoKeepalive());
        assertEquals(128 * 1024, props.getSoRcvbuf());
        assertEquals(128 * 1024, props.getSoSndbuf());
        assertEquals(180, props.getIdleTimeout());
        assertFalse(props.isEnableLog());
        assertNotNull(props.getPlatforms());
    }

    @Test
    @DisplayName("设置端口")
    void testSetPort() {
        ServerProperties props = new ServerProperties();
        props.setPort(8080);
        
        assertEquals(8080, props.getPort());
    }

    @Test
    @DisplayName("设置线程数")
    void testSetThreads() {
        ServerProperties props = new ServerProperties();
        
        props.setBossThreads(2);
        props.setWorkerThreads(4);
        props.setBusinessThreads(8);
        
        assertEquals(2, props.getBossThreads());
        assertEquals(4, props.getWorkerThreads());
        assertEquals(8, props.getBusinessThreads());
    }

    @Test
    @DisplayName("设置TCP参数")
    void testSetTcpOptions() {
        ServerProperties props = new ServerProperties();
        
        props.setSoBacklog(2048);
        props.setSoReuseaddr(false);
        props.setTcpNodelay(false);
        props.setSoKeepalive(false);
        props.setSoRcvbuf(65536);
        props.setSoSndbuf(65536);
        
        assertEquals(2048, props.getSoBacklog());
        assertFalse(props.isSoReuseaddr());
        assertFalse(props.isTcpNodelay());
        assertFalse(props.isSoKeepalive());
        assertEquals(65536, props.getSoRcvbuf());
        assertEquals(65536, props.getSoSndbuf());
    }

    @Test
    @DisplayName("设置空闲超时")
    void testSetIdleTimeout() {
        ServerProperties props = new ServerProperties();
        
        props.setIdleTimeout(300);
        
        assertEquals(300, props.getIdleTimeout());
    }

    @Test
    @DisplayName("设置平台配置")
    void testSetPlatforms() {
        ServerProperties props = new ServerProperties();
        
        List<ServerProperties.PlatformConfig> platforms = new ArrayList<>();
        
        ServerProperties.PlatformConfig config1 = new ServerProperties.PlatformConfig();
        config1.setPlatformId(12345678L);
        config1.setPassword("password1");
        config1.setVersion("1.0");
        config1.setEnabled(true);
        
        ServerProperties.PlatformConfig config2 = new ServerProperties.PlatformConfig();
        config2.setPlatformId(87654321L);
        config2.setPassword("password2");
        config2.setVersion("2.0");
        config2.setEnabled(false);
        
        platforms.add(config1);
        platforms.add(config2);
        props.setPlatforms(platforms);
        
        assertEquals(2, props.getPlatforms().size());
        assertEquals(12345678L, props.getPlatforms().get(0).getPlatformId());
        assertEquals(87654321L, props.getPlatforms().get(1).getPlatformId());
    }

    @Test
    @DisplayName("平台配置默认值")
    void testPlatformConfigDefaults() {
        ServerProperties.PlatformConfig config = new ServerProperties.PlatformConfig();
        
        assertEquals(0L, config.getPlatformId());
        assertNull(config.getPassword());
        assertEquals("1.0", config.getVersion());
        assertTrue(config.isEnabled());
    }
}
