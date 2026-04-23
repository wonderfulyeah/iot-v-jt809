package org.iot.v.jt809.core.constant;

/**
 * JT809消息类型定义
 *
 * @author haye
 * @date 2026-03-24
 */
public interface MessageType {
    
    // ==================== 上行消息（下级→上级） ====================
    
    /**
     * 上行连接请求
     */
    int UP_CONNECT_REQ = 0x1001;
    
    /**
     * 上行连接响应
     */
    int UP_CONNECT_RESP = 0x1002;
    
    /**
     * 上行断开请求
     */
    int UP_DISCONNECT_REQ = 0x1003;
    
    /**
     * 上行断开响应
     */
    int UP_DISCONNECT_RESP = 0x1004;
    
    /**
     * 链路保持请求（心跳）
     */
    int UP_LINK_TEST_REQ = 0x1005;
    
    /**
     * 链路保持响应（心跳）
     */
    int UP_LINK_TEST_RESP = 0x1006;
    
    /**
     * 车辆定位信息
     */
    int VEHICLE_LOCATION = 0x1200;
    
    /**
     * 车辆定位数据（子业务类型）
     */
    int VEHICLE_LOCATION_DATA = 0x1202;
    
    /**
     * 主链路报警信息交互消息
     */
    int ALARM_INFO_INTERACTION = 0x1400;
    
    /**
     * 上报报警信息消息（子业务类型）
     */
    int ALARM_REPORT_INFO = 0x1402;
    
    /**
     * 主动上报报警处理结果消息（子业务类型）
     */
    int ALARM_REPORT_RESULT = 0x1412;

    /**
     * 主链路车辆监管消息
     */
    int VEHICLE_MONITOR = 0x1500;

    /**
     * 车辆拍照应答（子业务类型）
     */
    int PHOTO_RESPONSE = 0x1502;


    /**
     * 车辆注册信息
     */
    int VEHICLE_REGISTER = 0x1601;
    
    // ==================== 下行消息（上级→下级） ====================
    
    /**
     * 下行连接请求
     */
    int DOWN_CONNECT_REQ = 0x9001;
    
    /**
     * 下行连接响应
     */
    int DOWN_CONNECT_RESP = 0x9002;
    
    /**
     * 下行断开请求
     */
    int DOWN_DISCONNECT_REQ = 0x9003;
    
    /**
     * 下行断开响应
     */
    int DOWN_DISCONNECT_RESP = 0x9004;
    
    /**
     * 链路保持请求（心跳）
     */
    int DOWN_LINK_TEST_REQ = 0x9005;
    
    /**
     * 链路保持响应（心跳）
     */
    int DOWN_LINK_TEST_RESP = 0x9006;
    
    /**
     * 车辆定位信息查询
     */
    int VEHICLE_LOCATION_QUERY = 0x9201;
    
    /**
     * 车辆控制请求
     */
    int VEHICLE_CONTROL = 0x9301;
}
