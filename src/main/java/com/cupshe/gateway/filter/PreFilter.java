package com.cupshe.gateway.filter;

import com.cupshe.gateway.config.properties.RestGatewayProperties;
import com.cupshe.gateway.constant.Headers;
import com.cupshe.gateway.constant.Ordered;
import com.cupshe.gateway.util.Attributes;
import org.springframework.core.annotation.Order;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

/**
 * PreFilter
 * <p>Cached request data of headers
 *
 * @author zxy
 */
@Component
@Order(Ordered.FOURTH)
public class PreFilter implements WebFilter {

    public PreFilter(RestGatewayProperties properties) {
        Headers.Ignores.HEADERS.addAll(properties.getFilterHeaders());
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        return doFilter(exchange, chain);
    }

    private Mono<Void> doFilter(ServerWebExchange exchange, WebFilterChain chain) {
        Attributes attr = getAttributesOf(exchange, Filters.getTraceId());
        exchange.getAttributes().put(Filters.ATTRIBUTES_CACHE_KEY, attr);
        return chain.filter(exchange);
    }

    private Attributes getAttributesOf(ServerWebExchange exchange, String traceId) {
        ServerHttpRequest clientReq = exchange.getRequest();
        return Attributes.attributeBuilder()
                .setId(traceId)
                .setMethod(clientReq.getMethod())
                .setContentType(clientReq.getHeaders().getContentType())
                .setHost(exchange.getAttribute(Headers.X_ORIGIN_IP))
                .setQueryParams(clientReq.getQueryParams())
                .setHeaders(Filters.httpHeaders(clientReq.getHeaders()))
                .setCookies(clientReq.getCookies())
                .setBody(clientReq.getBody())
                .build();
    }
}
