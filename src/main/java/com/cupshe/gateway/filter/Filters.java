package com.cupshe.gateway.filter;

import com.cupshe.ak.common.BaseConstant;
import com.cupshe.gateway.constant.Headers;
import org.slf4j.MDC;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpHeaders;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import reactor.netty.ByteBufMono;

/**
 * Filters
 *
 * @author zxy
 */
public class Filters {

    public static final String ATTRIBUTES_CACHE_KEY = "$ATTRIBUTES";

    public static final String REMOTE_HOST_CACHE_KEY = "$REMOTE_HOST";

    public static Mono<Mono<DataBuffer>> getModel(ServerHttpResponse resp, String message) {
        DataBuffer buffer = resp.bufferFactory().allocateBuffer().write(message.getBytes());
        return Mono.just(ByteBufMono.just(buffer));
    }

    public static HttpHeaders httpHeaders(HttpHeaders headers) {
        HttpHeaders result = new HttpHeaders();
        headers.forEach((k, v) -> {
            if (!Headers.Ignores.contains(k)) {
                result.addAll(k, v);
            }
        });

        return result;
    }

    public static String getPath(ServerWebExchange exchange) {
        return getPath(exchange.getRequest());
    }

    public static String getPath(ServerHttpRequest req) {
        return req.getURI().getPath();
    }

    public static String getTraceId() {
        return MDC.get(BaseConstant.MDC_SESSION_KEY);
    }

    public static void setTraceId(String traceId) {
        MDC.put(BaseConstant.MDC_SESSION_KEY, traceId);
    }

    public static void clearTraceId() {
        MDC.remove(BaseConstant.MDC_SESSION_KEY);
    }
}
