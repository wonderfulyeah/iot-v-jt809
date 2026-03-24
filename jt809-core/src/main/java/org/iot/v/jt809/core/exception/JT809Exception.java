package org.iot.v.jt809.core.exception;

/**
 * JT809基础异常
 *
 * @author haye
 * @date 2026-03-24
 */
public class JT809Exception extends RuntimeException {
    
    // 错误码常量
    public static final int ERR_INVALID_MESSAGE = 1001;
    public static final int ERR_DECODE_FAILED = 1002;
    public static final int ERR_ENCODE_FAILED = 1003;
    public static final int ERR_UNAUTHORIZED = 2001;
    public static final int ERR_AUTH_FAILED = 2002;
    public static final int ERR_SESSION_NOT_FOUND = 3001;
    public static final int ERR_SESSION_EXPIRED = 3002;
    
    private int errorCode;
    
    public JT809Exception() {
        super();
    }
    
    public JT809Exception(String message) {
        super(message);
    }
    
    public JT809Exception(String message, Throwable cause) {
        super(message, cause);
    }
    
    public JT809Exception(Throwable cause) {
        super(cause);
    }
    
    public JT809Exception(int errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }
    
    public JT809Exception(int errorCode, String message, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
    }
    
    public int getErrorCode() {
        return errorCode;
    }
}
