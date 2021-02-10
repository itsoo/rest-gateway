package com.cupshe.gateway.filter;

import com.cupshe.gateway.config.properties.RestGatewayProperties;
import com.cupshe.gateway.constant.Headers;
import com.cupshe.gateway.util.Attributes;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;

/**
 * PreFilter
 * <p>Cached headers data of request-context
 *
 * @author zxy
 */
@Component
public class PreFilter extends AbstractFilter {

    private final AbstractFilter next;

    public PreFilter(RestGatewayProperties properties, AuthFilter authFilter) {
        Headers.Ignores.addAll(properties.getFilterHeaders());
        this.next = authFilter;
    }

    @Override
    public AbstractFilter next() {
        return next;
    }

    @Override
    public void filter(ServerWebExchange exchange) {
        String traceId = FilterContext.getTraceId(exchange);
        FilterContext.setAttributes(getAttributesOf(exchange, traceId));
    }

    private Attributes getAttributesOf(ServerWebExchange exchange, String traceId) {
        ServerHttpRequest clientReq = exchange.getRequest();
        return Attributes.attributeBuilder()
                .setId(traceId)
                .setMethod(clientReq.getMethod())
                .setContentType(clientReq.getHeaders().getContentType())
                .setHost(FilterContext.getRemoteHost())
                .setQueryParams(clientReq.getQueryParams())
                .setHeaders(Filters.httpHeaders(clientReq.getHeaders()))
                .setCookies(clientReq.getCookies())
                .setBody(clientReq.getBody())
                .build();
    }
}
