package com.cupshe.gateway.filter;

import com.cupshe.gateway.constant.Ordered;
import com.cupshe.gateway.core.RequestCaller;
import com.cupshe.gateway.exception.UnavailableException;
import com.cupshe.gateway.log.Logging;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import java.util.Map;
import java.util.Objects;

/**
 * SupportFilter
 * <p>Whether the service is degraded or not
 *
 * @author zxy
 */
@Component
@Order(Ordered.FIRST)
public class SupportFilter implements WebFilter {

    private final Map<String, Boolean> callStatus;

    public SupportFilter(RequestCaller caller) {
        // init Boolean = null -> true
        caller.getCallStatus().forEach((k, v) -> v = Objects.isNull(v) || v);
        this.callStatus = caller.getCallStatus();
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        return doFilter(exchange, chain);
    }

    private Mono<Void> doFilter(ServerWebExchange exchange, WebFilterChain chain) {
        String reqPath = Filters.getPath(exchange);
        for (Map.Entry<String, Boolean> me : callStatus.entrySet()) {
            if (reqPath.startsWith(me.getKey()) && me.getValue()) {
                return chain.filter(exchange);
            }
        }

        Logging.writeRequestUnsupported(exchange.getRequest());

        throw new UnavailableException();
    }
}
