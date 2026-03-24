package org.iot.v.jt809.core.exception;

/**
 * 编解码异常
 *
 * @author haye
 * @date 2026-03-24
 */
public class CodecException extends JT809Exception {
    
    public CodecException() {
        super();
    }
    
    public CodecException(String message) {
        super(message);
    }
    
    public CodecException(String message, Throwable cause) {
        super(message, cause);
    }
    
    public CodecException(Throwable cause) {
        super(cause);
    }
}
