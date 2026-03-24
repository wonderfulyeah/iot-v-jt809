package org.iot.v.jt809.core.message.upstream;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.iot.v.jt809.core.constant.JT809Constant;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * UpConnectResp 单元测试
 *
 * @author haye
 * @date 2026-03-24
 */
@DisplayName("上行连接响应消息测试")
class UpConnectRespTest {

    @Test
    @DisplayName("创建消息")
    void testCreateMessage() {
        UpConnectResp msg = new UpConnectResp();
        
        assertNotNull(msg.getHead());
        assertNotNull(msg.getBody());
        assertEquals("上行连接响应", msg.getMessageTypeName());
    }

    @Test
    @DisplayName("编解码往返测试-成功响应")
    void testEncodeDecodeRoundTripSuccess() {
        UpConnectResp original = new UpConnectResp();
        UpConnectResp.Body body = (UpConnectResp.Body) original.getBody();
        
        body.setResult(JT809Constant.LOGIN_SUCCESS);
        body.setVerifyCode(12345678L);
        
        // 编码
        byte[] encoded = body.encode();
        
        // 解码
        UpConnectResp decoded = new UpConnectResp();
        UpConnectResp.Body decodedBody = (UpConnectResp.Body) decoded.getBody();
        decodedBody.decode(encoded);
        
        assertEquals(body.getResult(), decodedBody.getResult());
        assertEquals(body.getVerifyCode(), decodedBody.getVerifyCode());
    }

    @Test
    @DisplayName("编解码往返测试-失败响应")
    void testEncodeDecodeRoundTripFailure() {
        UpConnectResp original = new UpConnectResp();
        UpConnectResp.Body body = (UpConnectResp.Body) original.getBody();
        
        body.setResult(JT809Constant.LOGIN_FAIL_PASSWORD);
        body.setVerifyCode(0L);
        
        // 编码
        byte[] encoded = body.encode();
        
        // 解码
        UpConnectResp decoded = new UpConnectResp();
        UpConnectResp.Body decodedBody = (UpConnectResp.Body) decoded.getBody();
        decodedBody.decode(encoded);
        
        assertEquals(JT809Constant.LOGIN_FAIL_PASSWORD, decodedBody.getResult());
        assertEquals(0L, decodedBody.getVerifyCode());
    }

    @Test
    @DisplayName("消息体长度验证")
    void testBodyLength() {
        UpConnectResp msg = new UpConnectResp();
        UpConnectResp.Body body = (UpConnectResp.Body) msg.getBody();
        
        body.setResult((byte) 0);
        body.setVerifyCode(12345L);
        
        byte[] encoded = body.encode();
        
        // result(1) + verifyCode(4) = 5字节
        assertEquals(5, encoded.length);
    }

    @Test
    @DisplayName("验证所有登录结果码")
    void testAllResultCodes() {
        byte[] resultCodes = {
            JT809Constant.LOGIN_SUCCESS,
            JT809Constant.LOGIN_FAIL_IP,
            JT809Constant.LOGIN_FAIL_CODE,
            JT809Constant.LOGIN_FAIL_USER,
            JT809Constant.LOGIN_FAIL_PASSWORD,
            JT809Constant.LOGIN_FAIL_OTHER
        };
        
        for (byte code : resultCodes) {
            UpConnectResp msg = new UpConnectResp();
            UpConnectResp.Body body = (UpConnectResp.Body) msg.getBody();
            
            body.setResult(code);
            body.setVerifyCode(0L);
            
            byte[] encoded = body.encode();
            
            UpConnectResp decoded = new UpConnectResp();
            UpConnectResp.Body decodedBody = (UpConnectResp.Body) decoded.getBody();
            decodedBody.decode(encoded);
            
            assertEquals(code, decodedBody.getResult());
        }
    }
}
