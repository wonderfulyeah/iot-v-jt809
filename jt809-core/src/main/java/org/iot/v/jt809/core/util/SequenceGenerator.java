package org.iot.v.jt809.core.util;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * 消息流水号生成器
 * 线程安全，循环累加
 *
 * @author haye
 * @date 2026-03-24
 */
public class SequenceGenerator {
    
    /**
     * 流水号最大值
     */
    private static final int MAX_SEQUENCE = Integer.MAX_VALUE;
    
    /**
     * 流水号计数器
     */
    private final AtomicInteger sequence = new AtomicInteger(0);
    
    /**
     * 单例实例
     */
    private static final SequenceGenerator INSTANCE = new SequenceGenerator();
    
    private SequenceGenerator() {
    }
    
    /**
     * 获取单例实例
     *
     * @return SequenceGenerator实例
     */
    public static SequenceGenerator getInstance() {
        return INSTANCE;
    }
    
    /**
     * 获取下一个流水号
     *
     * @return 流水号
     */
    public int next() {
        int current = sequence.incrementAndGet();
        
        // 防止溢出，到达最大值后重置为0
        if (current < 0 || current > MAX_SEQUENCE) {
            synchronized (this) {
                // 双重检查
                if (sequence.get() < 0 || sequence.get() > MAX_SEQUENCE) {
                    sequence.set(0);
                }
                current = sequence.incrementAndGet();
            }
        }
        
        return current;
    }
    
    /**
     * 获取当前流水号
     *
     * @return 当前流水号
     */
    public int current() {
        return sequence.get();
    }
    
    /**
     * 重置流水号
     */
    public void reset() {
        sequence.set(0);
    }
}
