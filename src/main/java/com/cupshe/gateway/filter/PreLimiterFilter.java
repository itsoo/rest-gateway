package com.cupshe.gateway.filter;

import com.cupshe.ak.common.BaseConstant;
import com.cupshe.ak.net.UuidUtils;
import com.cupshe.gateway.constant.Headers;
import com.cupshe.gateway.constant.Ordered;
import com.cupshe.gateway.util.RequestProcessor;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import java.util.Map;

/**
 * PreLimiterFilter
 * <p>Cached 'trace-id' and 'remote-host' in serverWebExchange
 *
 * @author zxy
 */
@Component
@Order(Ordered.NEXT_HIGHEST)
public class PreLimiterFilter implements WebFilter {

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        return doFilter(exchange, chain);
    }

    private Mono<Void> doFilter(ServerWebExchange exchange, WebFilterChain chain) {
        Map<String, Object> attr = exchange.getAttributes();
        // set trace-id
        String traceId = UuidUtils.createUuid();
        Filters.setTraceId(traceId);
        attr.put(BaseConstant.TRACE_ID_KEY, traceId);
        // set remote-host
        String originIp = RequestProcessor.getRealOriginIp(exchange);
        attr.put(Headers.ORIGIN_IP, originIp);
        attr.put(Headers.X_ORIGIN_IP, originIp);
        attr.put(Headers.X_FORWARDED_FOR, originIp);
        return chain.filter(exchange);
    }
}
