package org.iot.v.jt809.core.message.base;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 协议版本号
 *
 * @author haye
 * @date 2026-03-24
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProtocolVersion {
    
    /**
     * 主版本号
     */
    private byte major = 1;
    
    /**
     * 次版本号
     */
    private byte minor = 0;
    
    /**
     * 获取版本字符串
     *
     * @return 版本字符串（如 "1.0"）
     */
    public String getVersion() {
        return major + "." + minor;
    }
    
    /**
     * 解析版本字符串
     *
     * @param version 版本字符串（如 "1.0"）
     * @return ProtocolVersion对象
     */
    public static ProtocolVersion parse(String version) {
        if (version == null || version.isEmpty()) {
            return new ProtocolVersion();
        }
        
        String[] parts = version.split("\\.");
        if (parts.length == 2) {
            try {
                byte major = Byte.parseByte(parts[0]);
                byte minor = Byte.parseByte(parts[1]);
                return new ProtocolVersion(major, minor);
            } catch (NumberFormatException e) {
                return new ProtocolVersion();
            }
        }
        
        return new ProtocolVersion();
    }
}
