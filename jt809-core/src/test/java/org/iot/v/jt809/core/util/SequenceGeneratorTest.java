package org.iot.v.jt809.core.util;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

/**
 * SequenceGenerator 单元测试
 *
 * @author haye
 * @date 2026-03-24
 */
@DisplayName("流水号生成器测试")
class SequenceGeneratorTest {

    private SequenceGenerator generator;

    @BeforeEach
    void setUp() {
        generator = SequenceGenerator.getInstance();
        generator.reset();
    }

    @Test
    @DisplayName("获取下一个流水号")
    void testNext() {
        int sn1 = generator.next();
        int sn2 = generator.next();
        
        assertTrue(sn2 > sn1, "流水号应递增");
    }

    @Test
    @DisplayName("获取当前流水号")
    void testCurrent() {
        generator.next();
        int current = generator.current();
        
        assertTrue(current > 0, "当前流水号应大于0");
    }

    @Test
    @DisplayName("重置流水号")
    void testReset() {
        generator.next();
        generator.next();
        generator.next();
        
        generator.reset();
        
        assertEquals(0, generator.current(), "重置后流水号应为0");
    }

    @Test
    @DisplayName("流水号唯一性")
    void testUniqueness() {
        Set<Integer> sequenceSet = new HashSet<>();
        int count = 1000;
        
        for (int i = 0; i < count; i++) {
            int sn = generator.next();
            assertTrue(sequenceSet.add(sn), "流水号应唯一: " + sn);
        }
        
        assertEquals(count, sequenceSet.size());
    }

    @Test
    @DisplayName("并发获取流水号-唯一性")
    void testConcurrentUniqueness() throws InterruptedException {
        int threadCount = 10;
        int iterationsPerThread = 1000;
        Set<Integer> sequenceSet = new HashSet<>();
        AtomicInteger duplicateCount = new AtomicInteger(0);
        
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);
        
        for (int i = 0; i < threadCount; i++) {
            executor.submit(() -> {
                try {
                    for (int j = 0; j < iterationsPerThread; j++) {
                        int sn = generator.next();
                        synchronized (sequenceSet) {
                            if (!sequenceSet.add(sn)) {
                                duplicateCount.incrementAndGet();
                            }
                        }
                    }
                } finally {
                    latch.countDown();
                }
            });
        }
        
        latch.await();
        executor.shutdown();
        
        assertEquals(0, duplicateCount.get(), "不应有重复的流水号");
    }

    @Test
    @DisplayName("单例模式")
    void testSingleton() {
        SequenceGenerator instance1 = SequenceGenerator.getInstance();
        SequenceGenerator instance2 = SequenceGenerator.getInstance();
        
        assertSame(instance1, instance2, "应返回同一个实例");
    }

    @Test
    @DisplayName("流水号递增连续性")
    void testSequentialIncrement() {
        generator.reset();
        
        int prev = generator.next();
        for (int i = 0; i < 100; i++) {
            int current = generator.next();
            assertEquals(prev + 1, current, "流水号应连续递增");
            prev = current;
        }
    }
}
