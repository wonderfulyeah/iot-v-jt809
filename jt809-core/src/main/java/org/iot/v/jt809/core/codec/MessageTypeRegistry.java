package org.iot.v.jt809.core.codec;

import org.iot.v.jt809.core.constant.MessageType;
import org.iot.v.jt809.core.message.base.BaseMessage;
import org.iot.v.jt809.core.message.upstream.*;
import org.iot.v.jt809.core.message.upstream.vehicle.*;
import org.iot.v.jt809.core.message.downstream.*;
import org.iot.v.jt809.core.message.downstream.vehicle.*;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 消息类型注册器
 * 用于根据消息ID获取对应的消息类
 *
 * @author haye
 * @date 2026-03-24
 */
public class MessageTypeRegistry {
    
    /**
     * 消息类型映射表
     */
    private static final Map<Integer, Class<? extends BaseMessage>> MESSAGE_TYPES = new ConcurrentHashMap<>();
    
    static {
        // 注册上行消息
        register(MessageType.UP_CONNECT_REQ, UpConnectReq.class);
        register(MessageType.UP_CONNECT_RESP, UpConnectResp.class);
        register(MessageType.UP_DISCONNECT_REQ, UpDisconnectReq.class);
        register(MessageType.UP_DISCONNECT_RESP, UpDisconnectResp.class);
        register(MessageType.UP_LINK_TEST_REQ, UpLinkTestReq.class);
        register(MessageType.UP_LINK_TEST_RESP, UpLinkTestResp.class);
        
        // 注册上行车辆消息
        register(MessageType.VEHICLE_LOCATION, VehicleDynamicMsg.class);
        register(MessageType.VEHICLE_ALARM, VehicleAlarmMsg.class);
        register(MessageType.VEHICLE_REGISTER, VehicleRegisterMsg.class);
        
        // 注册下行消息
        register(MessageType.DOWN_CONNECT_REQ, DownConnectReq.class);
        register(MessageType.DOWN_CONNECT_RESP, DownConnectResp.class);
        register(MessageType.DOWN_DISCONNECT_REQ, DownDisconnectReq.class);
        register(MessageType.DOWN_DISCONNECT_RESP, DownDisconnectResp.class);
        register(MessageType.DOWN_LINK_TEST_REQ, DownLinkTestReq.class);
        register(MessageType.DOWN_LINK_TEST_RESP, DownLinkTestResp.class);
        
        // 注册下行车辆消息
        register(MessageType.VEHICLE_LOCATION_QUERY, VehicleLocationQueryReq.class);
        register(MessageType.VEHICLE_CONTROL, VehicleControlReq.class);
    }
    
    /**
     * 注册消息类型
     *
     * @param msgId 消息ID
     * @param msgClass 消息类
     */
    public static void register(int msgId, Class<? extends BaseMessage> msgClass) {
        MESSAGE_TYPES.put(msgId, msgClass);
    }
    
    /**
     * 注销消息类型
     *
     * @param msgId 消息ID
     */
    public static void unregister(int msgId) {
        MESSAGE_TYPES.remove(msgId);
    }
    
    /**
     * 根据消息ID获取消息类
     *
     * @param msgId 消息ID
     * @return 消息类
     */
    public static Class<? extends BaseMessage> getMessageClass(int msgId) {
        return MESSAGE_TYPES.get(msgId);
    }
    
    /**
     * 创建消息实例
     *
     * @param msgId 消息ID
     * @return 消息实例
     */
    public static BaseMessage createMessage(int msgId) {
        Class<? extends BaseMessage> msgClass = getMessageClass(msgId);
        
        if (msgClass == null) {
            throw new IllegalArgumentException("Unknown message type: 0x" + 
                Integer.toHexString(msgId).toUpperCase());
        }
        
        try {
            return msgClass.newInstance();
        } catch (InstantiationException | IllegalAccessException e) {
            throw new RuntimeException("Create message instance failed", e);
        }
    }
    
    /**
     * 检查消息类型是否已注册
     *
     * @param msgId 消息ID
     * @return true-已注册, false-未注册
     */
    public static boolean isRegistered(int msgId) {
        return MESSAGE_TYPES.containsKey(msgId);
    }
    
    /**
     * 获取所有已注册的消息类型
     *
     * @return 消息类型集合
     */
    public static Map<Integer, Class<? extends BaseMessage>> getAllMessageTypes() {
        return new ConcurrentHashMap<>(MESSAGE_TYPES);
    }
}
