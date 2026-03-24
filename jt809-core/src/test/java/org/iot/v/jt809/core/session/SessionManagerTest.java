package org.iot.v.jt809.core.session;

import io.netty.channel.embedded.EmbeddedChannel;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * SessionManager 单元测试
 *
 * @author haye
 * @date 2026-03-24
 */
@DisplayName("会话管理器测试")
class SessionManagerTest {

    private SessionManager sessionManager;

    @BeforeEach
    void setUp() {
        sessionManager = SessionManager.getInstance();
        sessionManager.clear();
    }
    
    @AfterEach
    void tearDown() {
        sessionManager.clear();
    }

    @Test
    @DisplayName("单例模式")
    void testSingleton() {
        SessionManager instance1 = SessionManager.getInstance();
        SessionManager instance2 = SessionManager.getInstance();
        
        assertSame(instance1, instance2);
    }

    @Test
    @DisplayName("添加会话")
    void testAddSession() {
        Session session = createUniqueSession();
        
        sessionManager.addSession(session);
        
        assertEquals(1, sessionManager.getSessionCount());
        assertTrue(sessionManager.containsSession(session.getSessionId()));
    }

    @Test
    @DisplayName("添加null会话")
    void testAddNullSession() {
        sessionManager.addSession(null);
        
        assertEquals(0, sessionManager.getSessionCount());
    }

    @Test
    @DisplayName("移除会话")
    void testRemoveSession() {
        Session session = createUniqueSession();
        
        sessionManager.addSession(session);
        sessionManager.removeSession(session.getSessionId());
        
        assertEquals(0, sessionManager.getSessionCount());
        assertFalse(sessionManager.containsSession(session.getSessionId()));
    }

    @Test
    @DisplayName("移除不存在的会话")
    void testRemoveNonExistentSession() {
        sessionManager.removeSession("non-existent-id");
        
        assertEquals(0, sessionManager.getSessionCount());
    }

    @Test
    @DisplayName("根据ID获取会话")
    void testGetSessionById() {
        Session session = createUniqueSession();
        
        sessionManager.addSession(session);
        
        Session retrieved = sessionManager.getSession(session.getSessionId());
        assertSame(session, retrieved);
    }

    @Test
    @DisplayName("根据平台ID获取会话")
    void testGetSessionByPlatformId() {
        Session session = createUniqueSession();
        session.setPlatformId(12345L);
        
        sessionManager.addSession(session);
        
        Session retrieved = sessionManager.getSessionByPlatformId(12345L);
        assertSame(session, retrieved);
    }

    @Test
    @DisplayName("获取所有会话")
    void testGetAllSessions() {
        Session session1 = createUniqueSession();
        Session session2 = createUniqueSession();
        
        sessionManager.addSession(session1);
        sessionManager.addSession(session2);
        
        assertEquals(2, sessionManager.getAllSessions().size());
    }

    @Test
    @DisplayName("获取会话数量")
    void testGetSessionCount() {
        assertEquals(0, sessionManager.getSessionCount());
        
        Session session = createUniqueSession();
        sessionManager.addSession(session);
        
        assertEquals(1, sessionManager.getSessionCount());
    }

    @Test
    @DisplayName("清空所有会话")
    void testClear() {
        Session session1 = createUniqueSession();
        Session session2 = createUniqueSession();
        sessionManager.addSession(session1);
        sessionManager.addSession(session2);
        
        sessionManager.clear();
        
        assertEquals(0, sessionManager.getSessionCount());
    }

    @Test
    @DisplayName("检查会话是否存在")
    void testContainsSession() {
        Session session = createUniqueSession();
        
        assertFalse(sessionManager.containsSession(session.getSessionId()));
        
        sessionManager.addSession(session);
        
        assertTrue(sessionManager.containsSession(session.getSessionId()));
    }

    @Test
    @DisplayName("检查平台是否在线")
    void testIsPlatformOnline() {
        Session session = createUniqueSession();
        session.setPlatformId(12345L);
        
        assertFalse(sessionManager.isPlatformOnline(12345L));
        
        sessionManager.addSession(session);
        
        assertTrue(sessionManager.isPlatformOnline(12345L));
        
        session.close();
        
        assertFalse(sessionManager.isPlatformOnline(12345L));
    }

    @Test
    @DisplayName("多个会话管理")
    void testMultipleSessions() {
        Session session1 = createUniqueSession();
        session1.setPlatformId(1001L);
        
        Session session2 = createUniqueSession();
        session2.setPlatformId(1002L);
        
        Session session3 = createUniqueSession();
        session3.setPlatformId(1003L);
        
        sessionManager.addSession(session1);
        sessionManager.addSession(session2);
        sessionManager.addSession(session3);
        
        assertEquals(3, sessionManager.getSessionCount());
        
        assertSame(session1, sessionManager.getSessionByPlatformId(1001L));
        assertSame(session2, sessionManager.getSessionByPlatformId(1002L));
        assertSame(session3, sessionManager.getSessionByPlatformId(1003L));
        
        sessionManager.removeSession(session2.getSessionId());
        
        assertEquals(2, sessionManager.getSessionCount());
        assertNull(sessionManager.getSessionByPlatformId(1002L));
    }

    @Test
    @DisplayName("会话平台ID更新后映射正确")
    void testPlatformIdUpdateMapping() {
        Session session = createUniqueSession();
        
        // 先添加会话（无平台ID）
        sessionManager.addSession(session);
        
        // 设置平台ID后，应该能够通过平台ID找到
        session.setPlatformId(9999L);
        
        // 手动触发重新映射（实际使用中，登录成功后会重新添加）
        sessionManager.addSession(session);
        
        Session retrieved = sessionManager.getSessionByPlatformId(9999L);
        assertSame(session, retrieved);
    }
    
    /**
     * 创建具有唯一sessionId的Session
     */
    private Session createUniqueSession() {
        EmbeddedChannel channel = new EmbeddedChannel();
        Session session = new Session(channel);
        // 确保sessionId唯一
        session.setSessionId(UUID.randomUUID().toString());
        return session;
    }
}
