package com.cupshe.gateway.core;

import com.cupshe.ak.common.BaseConstant;
import com.cupshe.gateway.config.properties.RestGatewayProperties;
import com.cupshe.gateway.log.Logging;
import com.google.common.util.concurrent.RateLimiter;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;

import java.util.Timer;
import java.util.TimerTask;

/**
 * Breaker
 *
 * @author zxy
 */
@Component
public class Breaker {

    private static final long TIMER_TRIGGER = 15 * 1000L;

    private final RateLimiter limiter;

    public Breaker(RestGatewayProperties properties) {
        this.limiter = RateLimiter.create(properties.getRateFailure());
    }

    public void execute(ServerWebExchange exchange, HostStatus hostStatus) {
        System.out.println(hostStatus);

        if (!limiter.tryAcquire() && hostStatus != null) {
            String traceId = exchange.getAttribute(BaseConstant.TRACE_ID_KEY);
            Logging.writeRequestTimeoutBreaker(exchange.getRequest(), hostStatus, traceId);
            resetHostStatusAfterTimer(hostStatus);
        }
    }

    private void resetHostStatusAfterTimer(HostStatus hostStatus) {
        hostStatus.setStatus(Boolean.FALSE);
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                hostStatus.setStatus(Boolean.TRUE);
            }
        }, TIMER_TRIGGER);
    }
}
