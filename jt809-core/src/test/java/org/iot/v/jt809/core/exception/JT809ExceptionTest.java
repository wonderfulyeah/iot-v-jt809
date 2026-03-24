package org.iot.v.jt809.core.exception;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * JT809Exception 单元测试
 *
 * @author haye
 * @date 2026-03-24
 */
@DisplayName("JT809异常测试")
class JT809ExceptionTest {

    @Test
    @DisplayName("无参构造")
    void testNoArgConstructor() {
        JT809Exception ex = new JT809Exception();
        
        assertNull(ex.getMessage());
        assertEquals(0, ex.getErrorCode());
    }

    @Test
    @DisplayName("带消息构造")
    void testMessageConstructor() {
        String message = "Test error message";
        JT809Exception ex = new JT809Exception(message);
        
        assertEquals(message, ex.getMessage());
        assertEquals(0, ex.getErrorCode());
    }

    @Test
    @DisplayName("带消息和原因构造")
    void testMessageAndCauseConstructor() {
        String message = "Test error message";
        Throwable cause = new RuntimeException("Root cause");
        
        JT809Exception ex = new JT809Exception(message, cause);
        
        assertEquals(message, ex.getMessage());
        assertSame(cause, ex.getCause());
        assertEquals(0, ex.getErrorCode());
    }

    @Test
    @DisplayName("带原因构造")
    void testCauseConstructor() {
        Throwable cause = new RuntimeException("Root cause");
        
        JT809Exception ex = new JT809Exception(cause);
        
        assertSame(cause, ex.getCause());
        assertEquals(0, ex.getErrorCode());
    }

    @Test
    @DisplayName("带错误码和消息构造")
    void testErrorCodeAndMessageConstructor() {
        String message = "Invalid message format";
        int errorCode = JT809Exception.ERR_INVALID_MESSAGE;
        
        JT809Exception ex = new JT809Exception(errorCode, message);
        
        assertEquals(message, ex.getMessage());
        assertEquals(errorCode, ex.getErrorCode());
    }

    @Test
    @DisplayName("带错误码、消息和原因构造")
    void testErrorCodeMessageAndCauseConstructor() {
        String message = "Decode failed";
        int errorCode = JT809Exception.ERR_DECODE_FAILED;
        Throwable cause = new RuntimeException("Parse error");
        
        JT809Exception ex = new JT809Exception(errorCode, message, cause);
        
        assertEquals(message, ex.getMessage());
        assertEquals(errorCode, ex.getErrorCode());
        assertSame(cause, ex.getCause());
    }

    @Test
    @DisplayName("错误码常量验证")
    void testErrorCodes() {
        assertEquals(1001, JT809Exception.ERR_INVALID_MESSAGE);
        assertEquals(1002, JT809Exception.ERR_DECODE_FAILED);
        assertEquals(1003, JT809Exception.ERR_ENCODE_FAILED);
        assertEquals(2001, JT809Exception.ERR_UNAUTHORIZED);
        assertEquals(2002, JT809Exception.ERR_AUTH_FAILED);
        assertEquals(3001, JT809Exception.ERR_SESSION_NOT_FOUND);
        assertEquals(3002, JT809Exception.ERR_SESSION_EXPIRED);
    }
}
