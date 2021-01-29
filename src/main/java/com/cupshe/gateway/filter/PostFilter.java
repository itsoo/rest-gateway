package com.cupshe.gateway.filter;

import com.cupshe.gateway.constant.Symbols;
import com.cupshe.gateway.core.HostStatus;
import com.cupshe.gateway.core.RequestCaller;
import com.cupshe.gateway.exception.UnavailableException;
import com.cupshe.gateway.log.Logging;
import com.cupshe.gateway.util.Attributes;
import com.cupshe.gateway.util.RequestProcessor;
import com.cupshe.gateway.util.ResponseProcessor;
import lombok.SneakyThrows;
import org.springframework.core.annotation.Order;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;
import org.springframework.web.reactive.function.BodyExtractors;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.util.Objects;

/**
 * PostFilter
 * <p>Forward the request and return the response
 *
 * @author zxy
 */
@Component
@Order
public class PostFilter implements WebFilter {

    private final WebClient webClient;

    private final RequestCaller caller;

    public PostFilter(WebClient webClient, RequestCaller caller) {
        this.webClient = webClient;
        this.caller = caller;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        String reqPath = Filters.getPath(exchange);
        HostStatus remoteHost = caller.next(reqPath);
        if (Objects.isNull(remoteHost) || !remoteHost.getStatus()) {
            throw new UnavailableException();
        }

        exchange.getAttributes().put(Filters.REMOTE_HOST_CACHE_KEY, remoteHost);
        String url = Symbols.HTTP_PROTOCOL_PREFIX + remoteHost.getHost() + reqPath;

        try {
            return doFilter(exchange, url);
        } finally {
            Filters.clearTraceId();
        }
    }

    @SneakyThrows
    private Mono<Void> doFilter(ServerWebExchange exchange, String url) {
        Logging.writeRequestPayload(exchange, url);
        final Attributes attr = exchange.getAttribute(Filters.ATTRIBUTES_CACHE_KEY);
        Assert.notNull(attr, "'attributes' cannot be null.");
        return RequestProcessor.getRemoteRequestOf(webClient, attr, new URI(url))
                .exchange()
                .name(attr.getId())
                .doOnError(ResponseProcessor::rethrow)
                .flatMap(r -> convertAndResponse(r, exchange));
    }

    private Mono<? extends Void> convertAndResponse(ClientResponse remoteResp, ServerWebExchange exchange) {
        ServerHttpResponse clientResp = exchange.getResponse();
        ResponseProcessor.resetServerHttpResponse(clientResp, remoteResp);
        return clientResp
                .writeWith(remoteResp.body(BodyExtractors.toDataBuffers()))
                .doOnError(ResponseProcessor::rethrow)
                .doOnCancel(() -> ResponseProcessor.cleanup(remoteResp));
    }
}
