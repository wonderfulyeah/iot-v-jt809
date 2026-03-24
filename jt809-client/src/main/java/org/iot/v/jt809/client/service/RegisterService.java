package org.iot.v.jt809.client.service;

import org.iot.v.jt809.core.session.Session;

/**
 * 注册服务接口
 *
 * @author haye
 * @date 2026-03-24
 */
public interface RegisterService {
    
    /**
     * 执行登录
     *
     * @return 登录是否成功
     */
    boolean login();
    
    /**
     * 执行登出
     */
    void logout();
    
    /**
     * 检查是否已登录
     *
     * @return true-已登录, false-未登录
     */
    boolean isLoggedIn();
    
    /**
     * 获取当前会话
     *
     * @return 会话
     */
    Session getSession();
}
