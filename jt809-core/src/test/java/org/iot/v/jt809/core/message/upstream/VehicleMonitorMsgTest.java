package org.iot.v.jt809.core.message.upstream;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.buffer.Unpooled;
import io.netty.channel.embedded.EmbeddedChannel;
import org.iot.v.jt809.core.codec.JT809Decoder;
import org.iot.v.jt809.core.constant.JT809Constant;
import org.iot.v.jt809.core.constant.MessageType;
import org.iot.v.jt809.core.message.base.BaseMessage;
import org.iot.v.jt809.core.message.upstream.vehicle.entity.VehicleLocationInfo;
import org.iot.v.jt809.core.message.upstream.vehicle.VehicleMonitorMsg;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 0x1500 车辆监管消息 - 0x1502 拍照应答测试
 *
 * @author haye
 */
@DisplayName("0x1500 车辆监管消息 - 0x1502 拍照应答测试")
class VehicleMonitorMsgTest {

    private EmbeddedChannel channel;

    private static String HEX_DATA;

    /**
     * 消息体在hex中的起始偏移量：起始符(1) + 消息头(30) = 31
     */
    private static final int BODY_OFFSET = 1 + JT809Constant.MESSAGE_HEAD_LENGTH;

    @BeforeEach
    void setUp() {
        channel = new EmbeddedChannel(new JT809Decoder());
    }

    @BeforeAll
    static void loadTestData() throws IOException {
        StringBuilder sb = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new FileReader("src/test/resources/hex/message/1500"))) {
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
        }
        HEX_DATA = sb.toString().trim();
    }

    @Test
    @DisplayName("消息基本信息验证")
    void testMessageBasicInfo() {
        VehicleMonitorMsg msg = new VehicleMonitorMsg();
        assertEquals(MessageType.VEHICLE_MONITOR, msg.getMsgId());
        assertEquals("主链路车辆监管消息", msg.getMessageTypeName());
        assertNotNull(msg.getBody());
    }

    @Test
    @DisplayName("解码hex测试文件-完整流程")
    void testDecodeFromHexFile() {
        byte[] rawBytes = hexToBytes(HEX_DATA);

        // 跳过消息头，只解码消息体
        byte[] bodyBytes = extractBody(rawBytes);

        VehicleMonitorMsg msg = new VehicleMonitorMsg();
        VehicleMonitorMsg.Body body = (VehicleMonitorMsg.Body) msg.getBody();
        body.decode(bodyBytes);

        // === 消息体基本字段 ===
        assertNotNull(body.getVehicleNo());
        assertEquals(2, body.getVehicleColor());
        assertEquals(0x1502, body.getSubBusinessType());

        // === PhotoResponseData 字段 ===
        VehicleMonitorMsg.PhotoResponseData photoData = body.getPhotoResponseData();
        assertNotNull(photoData);

        assertTrue(photoData.getSourceSubBusinessType() >= 0);
        assertTrue(photoData.getSourceSeqNo() >= 0);
        assertTrue(photoData.getPhotoResponseFlag() >= 0);

        // === VehicleLocationInfo (复用验证) ===
        VehicleLocationInfo loc = photoData.getLocationInfo();
        assertNotNull(loc);
        assertNotNull(loc.getAlarmFlag());
        assertNotNull(loc.getStatusFlag());

        // === 图片相关字段 ===
        assertTrue(photoData.getCameraId() >= 0);
        assertTrue(photoData.getImageLength() >= 0);
        assertTrue(photoData.getImageFormat() >= 0);

        // === 核心验证：图片数据以十六进制存储 ===
        String imageDataHex = photoData.getImageDataHex();
        assertNotNull(imageDataHex);
    }

    @Test
    @DisplayName("数据解码测试1502")
    void decode1412() throws Exception {
        String hex = HEX_DATA;
        ByteBuf buf = Unpooled.wrappedBuffer(ByteBufUtil.decodeHexDump(hex));
        channel.writeInbound(buf);
        BaseMessage decoded = channel.readInbound();
        assertNotNull(decoded);
    }

    @Test
    @DisplayName("编码往返测试")
    void testEncodeDecodeRoundTrip() {
        // 先从hex文件解码
        byte[] rawBytes = hexToBytes(HEX_DATA);
        byte[] bodyBytes = extractBody(rawBytes);

        VehicleMonitorMsg original = new VehicleMonitorMsg();
        VehicleMonitorMsg.Body originalBody = (VehicleMonitorMsg.Body) original.getBody();
        originalBody.decode(bodyBytes);

        // 编码
        byte[] encoded = originalBody.encode();
        assertNotNull(encoded);
        assertTrue(encoded.length > 0);

        // 验证编码后的基本结构
        assertTrue(encoded.length >= 28); // 21(vehicleNo) + 1(color) + 2(subBiz) + 4(dataLen)
    }

    /**
     * 从原始消息中提取消息体（跳过起始符和消息头）
     */
    private byte[] extractBody(byte[] rawBytes) {
        int bodyLength = rawBytes.length - BODY_OFFSET - JT809Constant.CRC_LENGTH - 1; // -CRC(2) -结束符(1)
        byte[] bodyBytes = new byte[bodyLength];
        System.arraycopy(rawBytes, BODY_OFFSET, bodyBytes, 0, bodyLength);
        return bodyBytes;
    }

    /**
     * 将紧凑格式的十六进制字符串转换为字节数组（支持有空格和无空格格式）
     */
    private static byte[] hexToBytes(String hex) {
        if (hex == null || hex.isEmpty()) {
            return new byte[0];
        }
        // 移除所有空格
        hex = hex.replaceAll("\\s+", "");
        int len = hex.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(hex.charAt(i), 16) << 4)
                    + Character.digit(hex.charAt(i + 1), 16));
        }
        return data;
    }
}
