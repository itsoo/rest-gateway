package com.cupshe.gateway.core;

import java.util.Arrays;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

/**
 * TimerTask
 * <p>Abstract time slice, which can be used to delay the execution of tasks, time unit (seconds)
 *
 * @author zxy
 */
@SuppressWarnings("all")
public class TimerTask<T> {

    /*** pointer */
    private final AtomicInteger i;

    /*** timer-task */
    private final Queue<T>[] timerTask;

    /*** timer-executor */
    private final ScheduledExecutorService executor;

    /*** consumer function */
    private final Consumer<T> consumer;

    public TimerTask(Consumer<T> consumer) {
        this.i = new AtomicInteger(-1);
        this.timerTask = new Queue[60];
        this.executor = new ScheduledThreadPoolExecutor(1);
        this.consumer = consumer;
        this.initial();
    }

    private void initial() {
        // init queues for timer-task
        for (int j = 0; j < timerTask.length; j++) {
            timerTask[j] = new ConcurrentLinkedQueue<>();
        }

        startLearnServersListener();
    }

    private void startLearnServersListener() {
        executor.scheduleWithFixedDelay(() -> {
            increment();
            // consume timer-task
            for (T t = poll(); t != null; t = poll()) {
                consumer.accept(t);
            }
        }, 1L, 1L, TimeUnit.SECONDS);
    }

    public void push(T hostStatus, int delay) {
        int curr = (i.get() + delay) % timerTask.length;
        timerTask[curr].add(hostStatus);
    }

    public T poll() {
        int curr = i.get();
        return timerTask[curr].poll();
    }

    private void increment() {
        int curr, next;

        do {
            curr = i.get();
            next = (curr + 1) % timerTask.length;
        } while (!i.compareAndSet(curr, next));
    }

    @Override
    public String toString() {
        return "TimerTask(" +
                "i=" + i +
                ", queues=" + Arrays.toString(timerTask) +
                ')';
    }
}
