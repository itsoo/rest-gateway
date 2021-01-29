package com.cupshe.gateway.filter;

import com.cupshe.gateway.config.properties.RestGatewayProperties;
import com.cupshe.gateway.constant.Ordered;
import com.cupshe.gateway.exception.UnavailableException;
import com.cupshe.gateway.log.Logging;
import com.google.common.util.concurrent.RateLimiter;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

/**
 * LimiterFilter
 * <p>If the request rate reaches the threshold, a failure response will be triggered(code 503)
 *
 * @author zxy
 */
@Component
@Order(Ordered.PRE_FIRST)
public class LimiterFilter implements WebFilter {

    private final RateLimiter limiter;

    public LimiterFilter(RestGatewayProperties properties) {
        limiter = RateLimiter.create(properties.getRateLimiter());
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        return doFilter(exchange, chain);
    }

    private Mono<Void> doFilter(ServerWebExchange exchange, WebFilterChain chain) {
        if (limiter.tryAcquire()) {
            return chain.filter(exchange);
        }

        Logging.writeRequestRateLimiter(exchange.getRequest());

        throw new UnavailableException();
    }
}
