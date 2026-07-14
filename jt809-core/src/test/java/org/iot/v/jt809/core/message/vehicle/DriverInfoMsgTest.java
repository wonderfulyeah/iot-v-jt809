package org.iot.v.jt809.core.message.vehicle;

import org.iot.v.jt809.core.codec.MessageTypeRegistry;
import org.iot.v.jt809.core.constant.MessageType;
import org.iot.v.jt809.core.message.downstream.vehicle.DownVehicleDynamicMsg;
import org.iot.v.jt809.core.message.upstream.vehicle.VehicleDynamicMsg;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 驾驶员身份信息消息测试。
 *
 * @author haye
 */
@DisplayName("驾驶员身份信息消息测试")
class DriverInfoMsgTest {

    @Test
    @DisplayName("120A 上报驾驶员身份信息应答编码解码")
    void testDriverInfoAckEncodeDecode() {
        VehicleDynamicMsg.Body body = new VehicleDynamicMsg.Body();
        body.setVehicleNo("浙A12345");
        body.setVehicleColor(2);
        body.setSubBusinessType(VehicleDynamicMsg.SUB_BUSINESS_TYPE_120A);

        VehicleDynamicMsg.DriverInfoAckData data = new VehicleDynamicMsg.DriverInfoAckData();
        data.setSourceSubBusinessType(DownVehicleDynamicMsg.SUB_BUSINESS_TYPE_920A);
        data.setSourceMsgSn(123456789L);
        data.setDriverName("张三");
        data.setDriverId("D123456789012345678");
        data.setLicence("LICENCE-1234567890");
        data.setOrgName("杭州交通运输管理局");
        data.setValidDate(1798761600L);
        body.setDriverInfoAckData(data);

        byte[] encoded = body.encode();
        assertEquals(28 + 2 + 4 + 16 + 20 + 40 + 200 + 8, encoded.length);

        VehicleDynamicMsg.Body decoded = new VehicleDynamicMsg.Body();
        decoded.decode(encoded);

        assertEquals("浙A12345", decoded.getVehicleNo());
        assertEquals(2, decoded.getVehicleColor());
        assertEquals(VehicleDynamicMsg.SUB_BUSINESS_TYPE_120A, decoded.getSubBusinessType());
        assertEquals(2 + 4 + 16 + 20 + 40 + 200 + 8, decoded.getSubsequentDataLength());
        assertNotNull(decoded.getDriverInfoAckData());
        assertEquals(DownVehicleDynamicMsg.SUB_BUSINESS_TYPE_920A,
                decoded.getDriverInfoAckData().getSourceSubBusinessType());
        assertEquals(123456789L, decoded.getDriverInfoAckData().getSourceMsgSn());
        assertEquals("张三", decoded.getDriverInfoAckData().getDriverName());
        assertEquals("D123456789012345678", decoded.getDriverInfoAckData().getDriverId());
        assertEquals("LICENCE-1234567890", decoded.getDriverInfoAckData().getLicence());
        assertEquals("杭州交通运输管理局", decoded.getDriverInfoAckData().getOrgName());
        assertEquals(1798761600L, decoded.getDriverInfoAckData().getValidDate());
    }

    @Test
    @DisplayName("120C 主动上报驾驶员身份信息编码解码")
    void testDriverInfoReportEncodeDecode() {
        VehicleDynamicMsg.Body body = new VehicleDynamicMsg.Body();
        body.setVehicleNo("浙A12345");
        body.setVehicleColor(2);
        body.setSubBusinessType(VehicleDynamicMsg.SUB_BUSINESS_TYPE_120C);

        VehicleDynamicMsg.DriverInfoReportData data = new VehicleDynamicMsg.DriverInfoReportData();
        data.setDriverName("李四");
        data.setDriverId("D223456789012345678");
        data.setLicence("LICENCE-2234567890");
        data.setOrgName("宁波交通运输管理局");
        data.setValidDate(1830297600L);
        body.setDriverInfoReportData(data);

        byte[] encoded = body.encode();
        assertEquals(28 + 16 + 20 + 20 + 200 + 8, encoded.length);

        VehicleDynamicMsg.Body decoded = new VehicleDynamicMsg.Body();
        decoded.decode(encoded);

        assertEquals("浙A12345", decoded.getVehicleNo());
        assertEquals(2, decoded.getVehicleColor());
        assertEquals(VehicleDynamicMsg.SUB_BUSINESS_TYPE_120C, decoded.getSubBusinessType());
        assertEquals(16 + 20 + 20 + 200 + 8, decoded.getSubsequentDataLength());
        assertNotNull(decoded.getDriverInfoReportData());
        assertEquals("李四", decoded.getDriverInfoReportData().getDriverName());
        assertEquals("D223456789012345678", decoded.getDriverInfoReportData().getDriverId());
        assertEquals("LICENCE-2234567890", decoded.getDriverInfoReportData().getLicence());
        assertEquals("宁波交通运输管理局", decoded.getDriverInfoReportData().getOrgName());
        assertEquals(1830297600L, decoded.getDriverInfoReportData().getValidDate());
    }

    @Test
    @DisplayName("920A 上报驾驶员身份信息请求编码解码")
    void testDriverInfoRequestEncodeDecode() {
        DownVehicleDynamicMsg.Body body = new DownVehicleDynamicMsg.Body();
        body.setVehicleNo("浙A12345");
        body.setVehicleColor(2);
        body.setSubBusinessType(DownVehicleDynamicMsg.SUB_BUSINESS_TYPE_920A);

        DownVehicleDynamicMsg.DriverInfoRequestData data = new DownVehicleDynamicMsg.DriverInfoRequestData();
        data.setFlag(1);
        body.setDriverInfoRequestData(data);

        byte[] encoded = body.encode();
        assertEquals(29, encoded.length);

        DownVehicleDynamicMsg.Body decoded = new DownVehicleDynamicMsg.Body();
        decoded.decode(encoded);

        assertEquals("浙A12345", decoded.getVehicleNo());
        assertEquals(2, decoded.getVehicleColor());
        assertEquals(DownVehicleDynamicMsg.SUB_BUSINESS_TYPE_920A, decoded.getSubBusinessType());
        assertEquals(1, decoded.getSubsequentDataLength());
        assertNotNull(decoded.getDriverInfoRequestData());
        assertEquals(1, decoded.getDriverInfoRequestData().getFlag());
    }

    @Test
    @DisplayName("9200 从链路动态信息交换消息已注册")
    void testDownVehicleDynamicMsgRegistered() {
        assertTrue(MessageTypeRegistry.isRegistered(MessageType.DOWN_EXG_MSG));
        assertInstanceOf(DownVehicleDynamicMsg.class, MessageTypeRegistry.createMessage(MessageType.DOWN_EXG_MSG));
    }
}
