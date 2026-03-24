package org.iot.v.jt809.core.exception;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * SessionException 单元测试
 *
 * @author haye
 * @date 2026-03-24
 */
@DisplayName("会话异常测试")
class SessionExceptionTest {

    @Test
    @DisplayName("无参构造")
    void testNoArgConstructor() {
        SessionException ex = new SessionException();
        
        assertNull(ex.getMessage());
        assertEquals(0, ex.getErrorCode());
    }

    @Test
    @DisplayName("带消息构造")
    void testMessageConstructor() {
        String message = "Session error";
        SessionException ex = new SessionException(message);
        
        assertEquals(message, ex.getMessage());
    }

    @Test
    @DisplayName("带消息和原因构造")
    void testMessageAndCauseConstructor() {
        String message = "Session error";
        Throwable cause = new RuntimeException("Connection closed");
        
        SessionException ex = new SessionException(message, cause);
        
        assertEquals(message, ex.getMessage());
        assertSame(cause, ex.getCause());
    }

    @Test
    @DisplayName("带错误码和消息构造")
    void testErrorCodeAndMessageConstructor() {
        String message = "Session not found";
        int errorCode = JT809Exception.ERR_SESSION_NOT_FOUND;
        
        SessionException ex = new SessionException(errorCode, message);
        
        assertEquals(message, ex.getMessage());
        assertEquals(errorCode, ex.getErrorCode());
    }

    @Test
    @DisplayName("创建会话未找到异常")
    void testNotFound() {
        String sessionId = "session-123";
        
        SessionException ex = SessionException.notFound(sessionId);
        
        assertTrue(ex.getMessage().contains(sessionId));
        assertEquals(JT809Exception.ERR_SESSION_NOT_FOUND, ex.getErrorCode());
    }

    @Test
    @DisplayName("创建会话已过期异常")
    void testExpired() {
        String sessionId = "session-456";
        
        SessionException ex = SessionException.expired(sessionId);
        
        assertTrue(ex.getMessage().contains(sessionId));
        assertEquals(JT809Exception.ERR_SESSION_EXPIRED, ex.getErrorCode());
    }

    @Test
    @DisplayName("创建未认证异常")
    void testUnauthorized() {
        SessionException ex = SessionException.unauthorized();
        
        assertNotNull(ex.getMessage());
        assertEquals(JT809Exception.ERR_UNAUTHORIZED, ex.getErrorCode());
    }

    @Test
    @DisplayName("继承自JT809Exception")
    void testInheritance() {
        SessionException ex = new SessionException("test");
        
        assertInstanceOf(JT809Exception.class, ex);
        assertInstanceOf(RuntimeException.class, ex);
    }
}
