package org.iot.v.jt809.handler;

import org.iot.v.jt809.core.constant.MessageType;
import org.iot.v.jt809.core.message.base.BaseMessage;
import org.iot.v.jt809.core.message.upstream.UpLinkTestReq;
import org.iot.v.jt809.handler.context.MessageContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * HandlerChain 单元测试
 *
 * @author haye
 * @date 2026-03-24
 */
@DisplayName("处理器链测试")
class HandlerChainTest {

    private HandlerChain handlerChain;

    @BeforeEach
    void setUp() {
        handlerChain = new HandlerChain();
        handlerChain.clear();
    }

    @Test
    @DisplayName("添加处理器")
    void testAddHandler() {
        MessageHandler handler = createTestHandler(new int[]{0x1005}, 0);
        
        handlerChain.addHandler(handler);
        
        assertEquals(1, handlerChain.size());
    }

    @Test
    @DisplayName("添加null处理器")
    void testAddNullHandler() {
        handlerChain.addHandler(null);
        
        assertEquals(0, handlerChain.size());
    }

    @Test
    @DisplayName("移除处理器")
    void testRemoveHandler() {
        MessageHandler handler = createTestHandler(new int[]{0x1005}, 0);
        
        handlerChain.addHandler(handler);
        handlerChain.removeHandler(handler);
        
        assertEquals(0, handlerChain.size());
    }

    @Test
    @DisplayName("处理器按顺序执行")
    void testHandlerOrder() {
        StringBuilder sb = new StringBuilder();
        
        MessageHandler handler1 = createOrderedHandler(new int[]{0x1005}, 2, "H2", sb);
        MessageHandler handler2 = createOrderedHandler(new int[]{0x1005}, 1, "H1", sb);
        MessageHandler handler3 = createOrderedHandler(new int[]{0x1005}, 3, "H3", sb);
        
        handlerChain.addHandler(handler1);
        handlerChain.addHandler(handler2);
        handlerChain.addHandler(handler3);
        
        // 处理消息
        BaseMessage msg = new UpLinkTestReq();
        MessageContext ctx = new MessageContext();
        handlerChain.process(ctx, msg);
        
        // 验证执行顺序
        assertEquals("H1H2H3", sb.toString());
    }

    @Test
    @DisplayName("处理器终止处理链")
    void testHandlerStopChain() {
        StringBuilder sb = new StringBuilder();
        
        MessageHandler handler1 = createOrderedHandler(new int[]{0x1005}, 1, "H1", sb);
        MessageHandler handler2 = createStoppingHandler(new int[]{0x1005}, 2, "H2", sb);
        MessageHandler handler3 = createOrderedHandler(new int[]{0x1005}, 3, "H3", sb);
        
        handlerChain.addHandler(handler1);
        handlerChain.addHandler(handler2);
        handlerChain.addHandler(handler3);
        
        BaseMessage msg = new UpLinkTestReq();
        MessageContext ctx = new MessageContext();
        handlerChain.process(ctx, msg);
        
        assertEquals("H1H2", sb.toString());
    }

    @Test
    @DisplayName("只处理支持的消息类型")
    void testSupportedMessageType() {
        StringBuilder sb = new StringBuilder();
        
        MessageHandler handler = createOrderedHandler(new int[]{0x1001}, 1, "H1", sb);
        
        handlerChain.addHandler(handler);
        
        // 发送不同类型的消息
        BaseMessage msg = new UpLinkTestReq(); // 0x1005
        MessageContext ctx = new MessageContext();
        handlerChain.process(ctx, msg);
        
        // 处理器不支持0x1005，不应执行
        assertEquals("", sb.toString());
    }

    @Test
    @DisplayName("空处理器链处理消息")
    void testEmptyHandlerChain() {
        BaseMessage msg = new UpLinkTestReq();
        MessageContext ctx = new MessageContext();
        
        // 不应抛出异常
        assertDoesNotThrow(() -> handlerChain.process(ctx, msg));
    }

    @Test
    @DisplayName("清空处理器链")
    void testClear() {
        handlerChain.addHandler(createTestHandler(new int[]{0x1005}, 0));
        handlerChain.addHandler(createTestHandler(new int[]{0x1005}, 1));
        
        handlerChain.clear();
        
        assertEquals(0, handlerChain.size());
    }

    @Test
    @DisplayName("获取处理器列表")
    void testGetHandlers() {
        MessageHandler h1 = createTestHandler(new int[]{0x1005}, 1);
        MessageHandler h2 = createTestHandler(new int[]{0x1005}, 2);
        
        handlerChain.addHandler(h1);
        handlerChain.addHandler(h2);
        
        assertEquals(2, handlerChain.getHandlers().size());
    }

    @Test
    @DisplayName("处理器异常后继续执行")
    void testHandlerExceptionContinues() {
        StringBuilder sb = new StringBuilder();
        
        MessageHandler handler1 = createFailingHandler(new int[]{0x1005}, 1, "H1", sb);
        MessageHandler handler2 = createOrderedHandler(new int[]{0x1005}, 2, "H2", sb);
        
        handlerChain.addHandler(handler1);
        handlerChain.addHandler(handler2);
        
        BaseMessage msg = new UpLinkTestReq();
        MessageContext ctx = new MessageContext();
        handlerChain.process(ctx, msg);
        
        // 第二个处理器应该继续执行
        assertEquals("H1H2", sb.toString());
    }

    // ==================== Helper Methods ====================

    private MessageHandler createTestHandler(int[] supportedTypes, int order) {
        return new MessageHandler() {
            @Override
            public boolean handle(MessageContext context, BaseMessage message) {
                return true;
            }

            @Override
            public int[] supportedMessageTypes() {
                return supportedTypes;
            }

            @Override
            public int getOrder() {
                return order;
            }
        };
    }

    private MessageHandler createOrderedHandler(int[] supportedTypes, int order, String name, StringBuilder sb) {
        return new MessageHandler() {
            @Override
            public boolean handle(MessageContext context, BaseMessage message) {
                sb.append(name);
                return true;
            }

            @Override
            public int[] supportedMessageTypes() {
                return supportedTypes;
            }

            @Override
            public int getOrder() {
                return order;
            }

            @Override
            public String getName() {
                return name;
            }
        };
    }

    private MessageHandler createStoppingHandler(int[] supportedTypes, int order, String name, StringBuilder sb) {
        return new MessageHandler() {
            @Override
            public boolean handle(MessageContext context, BaseMessage message) {
                sb.append(name);
                return false; // 终止处理链
            }

            @Override
            public int[] supportedMessageTypes() {
                return supportedTypes;
            }

            @Override
            public int getOrder() {
                return order;
            }

            @Override
            public String getName() {
                return name;
            }
        };
    }

    private MessageHandler createFailingHandler(int[] supportedTypes, int order, String name, StringBuilder sb) {
        return new MessageHandler() {
            @Override
            public boolean handle(MessageContext context, BaseMessage message) {
                sb.append(name);
                throw new RuntimeException("Test exception");
            }

            @Override
            public int[] supportedMessageTypes() {
                return supportedTypes;
            }

            @Override
            public int getOrder() {
                return order;
            }

            @Override
            public String getName() {
                return name;
            }
        };
    }
}
