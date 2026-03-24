package org.iot.v.jt809.server.service;

import org.iot.v.jt809.core.message.upstream.UpConnectReq;
import org.iot.v.jt809.core.session.Session;

/**
 * 认证服务接口
 *
 * @author haye
 * @date 2026-03-24
 */
public interface AuthService {
    
    /**
     * 验证登录请求
     *
     * @param connectReq 连接请求
     * @return 验证结果码
     *         0x00: 成功
     *         0x01: IP地址不正确
     *         0x02: 用户名/密码错误
     *         0x03: 其他原因
     */
    int authenticate(UpConnectReq connectReq);
    
    /**
     * 检查平台是否已授权
     *
     * @param platformId 平台ID
     * @return true-已授权, false-未授权
     */
    boolean isPlatformAuthorized(long platformId);
    
    /**
     * 获取平台密码
     *
     * @param platformId 平台ID
     * @return 密码
     */
    String getPlatformPassword(long platformId);
}
