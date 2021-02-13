package com.cupshe.gateway.core;

import com.cupshe.gateway.config.properties.RestGatewayProperties;
import com.cupshe.gateway.filter.FilterContext;
import com.cupshe.gateway.log.Logging;
import com.google.common.collect.Maps;
import com.google.common.util.concurrent.RateLimiter;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;

import java.util.Map;

/**
 * Breaker
 *
 * @author zxy
 */
@Component
public class Breaker {

    private final int defaultDelay;

    private final double rateFailure;

    private final TimerTask<HostStatus> timerTask;

    private final Map<HostStatus, RateLimiter> failureLimiter;

    public Breaker(RestGatewayProperties properties) {
        this.defaultDelay = properties.getDelayFailure();
        this.rateFailure = properties.getRateFailure();
        // 3600s = 60min = 1h
        this.timerTask = new TimerTask<>(3600, t -> t.setStatus(true));
        this.failureLimiter = Maps.newConcurrentMap();
    }

    public void execute(ServerWebExchange exchange, HostStatus hostStatus) {
        if (HostStatus.isNotSupport(hostStatus)) {
            return;
        }

        RateLimiter rl = failureLimiter.computeIfAbsent(hostStatus, k -> RateLimiter.create(rateFailure));
        if (rl.tryAcquire()) {
            return;
        }

        String traceId = FilterContext.getTraceId(exchange);
        Logging.writeRequestTimeoutBreaker(exchange.getRequest(), hostStatus, traceId);
        timerTask.push(hostStatus.setStatus(false), defaultDelay);
    }
}
