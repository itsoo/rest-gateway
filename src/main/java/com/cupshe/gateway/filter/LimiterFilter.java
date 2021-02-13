package com.cupshe.gateway.filter;

import com.cupshe.gateway.config.properties.RestGatewayProperties;
import com.cupshe.gateway.exception.UnavailableException;
import com.cupshe.gateway.log.Logging;
import com.google.common.util.concurrent.RateLimiter;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;

/**
 * LimiterFilter
 * <p>If the request rate reaches the threshold, a failure response will be triggered(code 503)
 *
 * @author zxy
 */
@Component
public class LimiterFilter extends AbstractFilter {

    private final RateLimiter limiter;

    private final AbstractFilter next;

    public LimiterFilter(RestGatewayProperties properties, SupportFilter nextFilter) {
        this.limiter = RateLimiter.create(properties.getRateLimiter());
        this.next = nextFilter;
    }

    @Override
    public AbstractFilter next() {
        return next;
    }

    @Override
    public void filter(ServerWebExchange exchange) {
        if (limiter.tryAcquire()) {
            return;
        }

        Logging.writeRequestRateLimiter(exchange.getRequest());
        throw new UnavailableException();
    }
}
