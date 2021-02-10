package com.cupshe.gateway.core;

import com.cupshe.gateway.config.properties.RestGatewayProperties;
import com.cupshe.gateway.filter.FilterContext;
import com.cupshe.gateway.log.Logging;
import com.google.common.util.concurrent.RateLimiter;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;

import java.util.concurrent.TimeUnit;

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
            String traceId = FilterContext.getTraceId(exchange);
            Logging.writeRequestTimeoutBreaker(exchange.getRequest(), hostStatus, traceId);
            timerTask.push(hostStatus.setStatus(false), defaultDelay);
        }
    }
}
