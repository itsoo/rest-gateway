package com.cupshe.gateway.handler;

import com.cupshe.ak.common.BaseConstant;
import com.cupshe.gateway.constant.Ordered;
import com.cupshe.gateway.core.Breaker;
import com.cupshe.gateway.exception.GatewayException;
import com.cupshe.gateway.exception.TimeoutException;
import com.cupshe.gateway.filter.Filters;
import com.cupshe.gateway.log.Logging;
import io.netty.channel.ConnectTimeoutException;
import io.netty.handler.timeout.ReadTimeoutException;
import io.netty.handler.timeout.WriteTimeoutException;
import org.springframework.boot.web.reactive.error.ErrorWebExceptionHandler;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.net.ConnectException;
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
@Order(Ordered.HIGHEST)
public class GlobalExceptionHandler implements ErrorWebExceptionHandler {

    private final Map<Class<?>, GatewayException> errorMap;

    private final Breaker breaker;

    public GlobalExceptionHandler(List<? extends GatewayException> errorList, Breaker breaker) {
        Map<Class<?>, GatewayException> map = errorList
                .parallelStream()
                // we need throw error for repeated key
                .collect(Collectors.toMap(GatewayException::getClass, t -> t));
        initial(map);
        this.errorMap = Collections.unmodifiableMap(map);
        this.breaker = breaker;
    }

    private void initial(Map<Class<?>, GatewayException> map) {
        GatewayException e = map.get(TimeoutException.class);
        map.put(ConnectException.class, e);
        map.put(ConnectTimeoutException.class, e);
        map.put(ReadTimeoutException.class, e);
        map.put(WriteTimeoutException.class, e);
    }

    @Override
    public Mono<Void> handle(ServerWebExchange exchange, Throwable t) {
        if (t instanceof TimeoutException) {
            breaker.execute(exchange, exchange.getAttribute(Filters.REMOTE_HOST_CACHE_KEY));
        }

        try {
            return doHandle(exchange, t);
        } finally {
            Filters.clearTraceId();
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
        return resp.writeAndFlushWith(Filters.getModel(resp, e.getMessage()));
    }

    private Mono<Void> defaultResponse(ServerWebExchange exchange, Throwable t) {
        Logging.writeResponseFailure(t, exchange.getAttribute(BaseConstant.TRACE_ID_KEY));
        ServerHttpResponse resp = exchange.getResponse();
        HttpStatus error500 = HttpStatus.INTERNAL_SERVER_ERROR;
        resp.getHeaders().setContentType(MediaType.TEXT_PLAIN);
        resp.setStatusCode(error500);
        return resp.writeAndFlushWith(Filters.getModel(resp, error500.getReasonPhrase()));
    }
}
