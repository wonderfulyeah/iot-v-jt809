package org.iot.v.jt809.client.reconnect;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * ExponentialBackoffStrategy 单元测试
 *
 * @author haye
 * @date 2026-03-24
 */
@DisplayName("指数退避重连策略测试")
class ExponentialBackoffStrategyTest {

    private ExponentialBackoffStrategy strategy;

    @BeforeEach
    void setUp() {
        strategy = new ExponentialBackoffStrategy(1000, 60000, 2.0, 5);
    }

    @Test
    @DisplayName("获取延迟-首次重连")
    void testGetFirstDelay() {
        long delay = strategy.getNextDelay(1);
        
        assertEquals(1000, delay);
    }

    @Test
    @DisplayName("获取延迟-指数增长")
    void testGetDelayExponentialGrowth() {
        long delay1 = strategy.getNextDelay(1);
        long delay2 = strategy.getNextDelay(2);
        long delay3 = strategy.getNextDelay(3);
        
        assertEquals(1000, delay1);
        assertEquals(2000, delay2);
        assertEquals(4000, delay3);
    }

    @Test
    @DisplayName("获取延迟-不超过最大延迟")
    void testMaxDelay() {
        ExponentialBackoffStrategy smallMaxStrategy = 
            new ExponentialBackoffStrategy(1000, 3000, 2.0, -1);
        
        smallMaxStrategy.getNextDelay(1); // 1000
        smallMaxStrategy.getNextDelay(2); // 2000
        long delay3 = smallMaxStrategy.getNextDelay(3); // 应该是 4000，但被限制为 3000
        
        assertEquals(3000, delay3);
    }

    @Test
    @DisplayName("是否应重试-超过最大次数")
    void testShouldRetryExceedMaxAttempts() {
        // maxAttempts = 5
        assertTrue(strategy.shouldRetry(0));
        assertTrue(strategy.shouldRetry(4));
        assertFalse(strategy.shouldRetry(5));
        assertFalse(strategy.shouldRetry(10));
    }

    @Test
    @DisplayName("是否应重试-无限重连")
    void testShouldRetryUnlimited() {
        ExponentialBackoffStrategy unlimited = 
            new ExponentialBackoffStrategy(1000, 60000, 2.0, -1);
        
        assertTrue(unlimited.shouldRetry(0));
        assertTrue(unlimited.shouldRetry(100));
        assertTrue(unlimited.shouldRetry(10000));
    }

    @Test
    @DisplayName("重置策略")
    void testReset() {
        strategy.getNextDelay(1); // 1000
        strategy.getNextDelay(2); // 2000
        
        strategy.reset();
        
        // 重置后应该从头开始
        long delay = strategy.getNextDelay(1);
        assertEquals(1000, delay);
    }

    @Test
    @DisplayName("超过最大次数返回-1")
    void testExceedMaxAttemptsReturnsNegative() {
        // 获取5次延迟
        for (int i = 1; i <= 5; i++) {
            strategy.getNextDelay(i);
        }
        
        // 第6次应该返回-1
        long delay = strategy.getNextDelay(6);
        assertEquals(-1, delay);
    }

    @Test
    @DisplayName("创建默认策略")
    void testCreateDefault() {
        ExponentialBackoffStrategy defaultStrategy = 
            ExponentialBackoffStrategy.createDefault();
        
        assertNotNull(defaultStrategy);
        assertTrue(defaultStrategy.shouldRetry(10000)); // 无限重连
    }

    @Test
    @DisplayName("创建有限次数策略")
    void testCreateLimited() {
        ExponentialBackoffStrategy limited = 
            ExponentialBackoffStrategy.createLimited(3);
        
        assertNotNull(limited);
        assertTrue(limited.shouldRetry(2));
        assertFalse(limited.shouldRetry(3));
    }

    @Test
    @DisplayName("默认构造函数")
    void testDefaultConstructor() {
        ExponentialBackoffStrategy defaultStrategy = new ExponentialBackoffStrategy();
        
        assertNotNull(defaultStrategy);
        assertTrue(defaultStrategy.shouldRetry(0));
        
        long delay = defaultStrategy.getNextDelay(1);
        assertEquals(1000, delay);
    }
}
