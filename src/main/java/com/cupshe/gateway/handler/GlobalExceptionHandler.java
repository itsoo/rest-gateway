package com.cupshe.gateway.handler;

import com.cupshe.gateway.core.Breaker;
import com.cupshe.gateway.exception.GatewayException;
import com.cupshe.gateway.exception.TimeoutException;
import com.cupshe.gateway.filter.FilterContext;
import com.cupshe.gateway.filter.Filters;
import com.cupshe.gateway.log.Logging;
import org.springframework.boot.web.reactive.error.ErrorWebExceptionHandler;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * GlobalExceptionHandler
 *
 * @author zxy
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class GlobalExceptionHandler implements ErrorWebExceptionHandler {

    private final Map<Class<? extends GatewayException>, GatewayException> errorMap;

    private final Breaker breaker;

    private final Filters filters;

    public GlobalExceptionHandler(List<? extends GatewayException> errorList, Breaker breaker, Filters filters) {
        Map<Class<? extends GatewayException>, GatewayException> map = errorList
                .parallelStream()
                // we need throw error for duplicate key
                .collect(Collectors.toMap(GatewayException::getClass, t -> t));
        this.errorMap = Collections.unmodifiableMap(map);
        this.breaker = breaker;
        this.filters = filters;
    }

    @Override
    public Mono<Void> handle(ServerWebExchange exchange, Throwable t) {
        if (t instanceof TimeoutException) {
            breaker.execute(exchange, FilterContext.getHostStatus(exchange));
        }

        try {
            return doHandle(exchange, t);
        } finally {
            FilterContext.clearAll();
        }
    }

    private Mono<Void> doHandle(ServerWebExchange exchange, Throwable t) {
        return errorMap.containsKey(t.getClass())
                ? gatewayResponse(exchange, errorMap.get(t.getClass()))
                : defaultResponse(exchange, t);
    }

    private Mono<Void> gatewayResponse(ServerWebExchange exchange, GatewayException e) {
        ServerHttpResponse resp = exchange.getResponse();
        resp.getHeaders().setContentType(MediaType.APPLICATION_JSON);
        resp.setStatusCode(e.getHttpStatus());
        return resp.writeAndFlushWith(filters.getModel(resp, e.getMessage()));
    }

    private Mono<Void> defaultResponse(ServerWebExchange exchange, Throwable t) {
        Logging.writeResponseFailure(t, FilterContext.getTraceId(exchange));
        ServerHttpResponse resp = exchange.getResponse();
        HttpStatus error500 = HttpStatus.INTERNAL_SERVER_ERROR;
        resp.getHeaders().setContentType(MediaType.TEXT_PLAIN);
        resp.setStatusCode(error500);
        return resp.writeAndFlushWith(filters.getModel(resp, error500.getReasonPhrase()));
    }
}
