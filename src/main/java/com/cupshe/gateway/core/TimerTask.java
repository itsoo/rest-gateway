package com.cupshe.gateway.core;

import java.util.Arrays;
import java.util.Objects;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

/**
 * TimerTask
 * <p>抽象的时间片，可用于延迟执行任务，时间单位（秒）
 *
 * @author zxy
 */
@SuppressWarnings("all")
public class TimerTask<T> {

    private final AtomicInteger i;

    private final Queue<T>[] queues;

    private final ScheduledExecutorService executor;

    private final Consumer<T> consumer;

    public TimerTask(Consumer<T> consumer) {
        this.i = new AtomicInteger(-1);
        this.queues = new Queue[60];
        this.executor = new ScheduledThreadPoolExecutor(1);
        this.consumer = consumer;
        this.initial();
    }

    private void initial() {
        // init queues
        for (int j = 0; j < queues.length; j++) {
            queues[j] = new ConcurrentLinkedQueue<>();
        }

        startLearnServersListener();
    }

    private void startLearnServersListener() {
        executor.scheduleWithFixedDelay(() -> {
            increment();
            T t;
            while (Objects.nonNull((t = poll()))) {
                consumer.accept(t);
            }
        }, 1L, 1L, TimeUnit.SECONDS);
    }

    public void push(T hostStatus, int delay) {
        int curr = (i.get() + delay) % queues.length;
        queues[curr].add(hostStatus);
    }

    public T poll() {
        int curr = i.get();
        return queues[curr].poll();
    }

    private void increment() {
        int curr, next;

        do {
            curr = i.get();
            next = (curr + 1) % queues.length;
        } while (!i.compareAndSet(curr, next));
    }

    @Override
    public String toString() {
        return "TimerTask(" +
                "i=" + i +
                ", queues=" + Arrays.toString(queues) +
                ')';
    }
}
