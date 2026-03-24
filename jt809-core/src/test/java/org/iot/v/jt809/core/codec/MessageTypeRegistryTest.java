package org.iot.v.jt809.core.codec;

import org.iot.v.jt809.core.constant.MessageType;
import org.iot.v.jt809.core.message.base.BaseMessage;
import org.iot.v.jt809.core.message.upstream.UpConnectReq;
import org.iot.v.jt809.core.message.upstream.UpConnectResp;
import org.iot.v.jt809.core.message.upstream.UpDisconnectReq;
import org.iot.v.jt809.core.message.upstream.UpLinkTestReq;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * MessageTypeRegistry 单元测试
 *
 * @author haye
 * @date 2026-03-24
 */
@DisplayName("消息类型注册器测试")
class MessageTypeRegistryTest {

    @Test
    @DisplayName("获取已注册的消息类")
    void testGetMessageClass() {
        Class<? extends BaseMessage> msgClass = MessageTypeRegistry.getMessageClass(MessageType.UP_CONNECT_REQ);
        
        assertNotNull(msgClass);
        assertEquals(UpConnectReq.class, msgClass);
    }

    @Test
    @DisplayName("获取未注册的消息类返回null")
    void testGetUnregisteredMessageClass() {
        Class<? extends BaseMessage> msgClass = MessageTypeRegistry.getMessageClass(0xFFFF);
        
        assertNull(msgClass);
    }

    @Test
    @DisplayName("创建消息实例")
    void testCreateMessage() {
        BaseMessage message = MessageTypeRegistry.createMessage(MessageType.UP_CONNECT_REQ);
        
        assertNotNull(message);
        assertInstanceOf(UpConnectReq.class, message);
    }

    @Test
    @DisplayName("创建未注册消息实例抛出异常")
    void testCreateUnregisteredMessage() {
        assertThrows(IllegalArgumentException.class, () -> {
            MessageTypeRegistry.createMessage(0xFFFF);
        });
    }

    @Test
    @DisplayName("检查消息类型是否已注册")
    void testIsRegistered() {
        assertTrue(MessageTypeRegistry.isRegistered(MessageType.UP_CONNECT_REQ));
        assertTrue(MessageTypeRegistry.isRegistered(MessageType.UP_CONNECT_RESP));
        assertTrue(MessageTypeRegistry.isRegistered(MessageType.UP_DISCONNECT_REQ));
        assertFalse(MessageTypeRegistry.isRegistered(0xFFFF));
    }

    @Test
    @DisplayName("动态注册消息类型")
    void testDynamicRegister() {
        int customMsgId = 0x9999;
        
        // 注册前检查
        assertFalse(MessageTypeRegistry.isRegistered(customMsgId));
        
        // 注册
        MessageTypeRegistry.register(customMsgId, UpConnectReq.class);
        
        // 验证注册成功
        assertTrue(MessageTypeRegistry.isRegistered(customMsgId));
        assertEquals(UpConnectReq.class, MessageTypeRegistry.getMessageClass(customMsgId));
        
        // 清理
        MessageTypeRegistry.unregister(customMsgId);
        assertFalse(MessageTypeRegistry.isRegistered(customMsgId));
    }

    @Test
    @DisplayName("注销消息类型")
    void testUnregister() {
        int customMsgId = 0x8888;
        
        // 先注册
        MessageTypeRegistry.register(customMsgId, UpConnectReq.class);
        assertTrue(MessageTypeRegistry.isRegistered(customMsgId));
        
        // 注销
        MessageTypeRegistry.unregister(customMsgId);
        assertFalse(MessageTypeRegistry.isRegistered(customMsgId));
    }

    @Test
    @DisplayName("获取所有消息类型")
    void testGetAllMessageTypes() {
        Map<Integer, Class<? extends BaseMessage>> allTypes = MessageTypeRegistry.getAllMessageTypes();
        
        assertNotNull(allTypes);
        assertFalse(allTypes.isEmpty());
        
        // 验证包含已知的消息类型
        assertTrue(allTypes.containsKey(MessageType.UP_CONNECT_REQ));
        assertTrue(allTypes.containsKey(MessageType.UP_CONNECT_RESP));
    }
}
