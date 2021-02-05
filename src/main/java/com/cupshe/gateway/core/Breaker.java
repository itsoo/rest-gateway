package com.cupshe.gateway.core;

import com.cupshe.ak.common.BaseConstant;
import com.cupshe.gateway.config.properties.RestGatewayProperties;
import com.cupshe.gateway.log.Logging;
import com.google.common.util.concurrent.RateLimiter;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;

import java.util.Objects;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

/**
 * Breaker
 *
 * @author zxy
 */
@Component
public class Breaker {

    private final int defaultDelay;

    private final RateLimiter limiter;

    private final TimerTask<HostStatus> timerTask;

    public Breaker(RestGatewayProperties properties) {
        this.defaultDelay = properties.getDelayFailure();
        this.limiter = RateLimiter.create(properties.getRateFailure(), 5, TimeUnit.SECONDS);
        this.timerTask = new TimerTask<>(t -> t.setStatus(true));
    }

    public void execute(ServerWebExchange exchange, HostStatus hostStatus) {
        if (HostStatus.isNotSupport(hostStatus)) {
            return;
        }

        if (!limiter.tryAcquire()) {
            String traceId = exchange.getAttribute(BaseConstant.TRACE_ID_KEY);
            Logging.writeRequestTimeoutBreaker(exchange.getRequest(), hostStatus, traceId);
            timerTask.push(hostStatus.setStatus(false), defaultDelay);
        }
    }

    /**
     * TimerTask
     * 抽象的时间片，可用于延迟执行任务，时间单位（秒）
     */
    @SuppressWarnings("all")
    public static class TimerTask<T> {

        private final AtomicInteger i = new AtomicInteger(-1);

        private final Queue<T>[] queues = new Queue[60];

        private final ScheduledExecutorService executor = new ScheduledThreadPoolExecutor(1);

        private final Consumer<T> consumer;

        public TimerTask(Consumer<T> consumer) {
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
    }
}
