package org.iot.v.jt809.core.exception;

/**
 * 会话异常
 *
 * @author haye
 * @date 2026-03-24
 */
public class SessionException extends JT809Exception {
    
    public SessionException() {
        super();
    }
    
    public SessionException(String message) {
        super(message);
    }
    
    public SessionException(String message, Throwable cause) {
        super(message, cause);
    }
    
    public SessionException(int errorCode, String message) {
        super(errorCode, message);
    }
    
    public SessionException(int errorCode, String message, Throwable cause) {
        super(errorCode, message, cause);
    }
    
    /**
     * 创建会话未找到异常
     */
    public static SessionException notFound(String sessionId) {
        return new SessionException(ERR_SESSION_NOT_FOUND, 
            "Session not found: " + sessionId);
    }
    
    /**
     * 创建会话已过期异常
     */
    public static SessionException expired(String sessionId) {
        return new SessionException(ERR_SESSION_EXPIRED, 
            "Session expired: " + sessionId);
    }
    
    /**
     * 创建未认证异常
     */
    public static SessionException unauthorized() {
        return new SessionException(ERR_UNAUTHORIZED, 
            "Session not authenticated");
    }
}
