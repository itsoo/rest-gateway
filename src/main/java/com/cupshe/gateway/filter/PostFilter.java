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

        // TODO 重点研究 WebClient 的问题：
        //  1. 网络请求未释放（猜测）导致线程过多 OOM ！！！！
        //  2. 还有并发下的负载均衡的问题！！！！

        String reqPath = Filters.getPath(exchange);
        HostStatus remoteHost = getRemoteHost(exchange, reqPath);
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
        Logging.writeRequestPayload(exchange.getRequest(), url);
        final Attributes attr = exchange.getAttribute(Filters.ATTRIBUTES_CACHE_KEY);
        Assert.notNull(attr, "'attributes' cannot be null.");
        return RequestProcessor.getRemoteRequestOf(webClient, attr, new URI(url))
                .exchange()
                .name(attr.getId())
                .doOnError(ResponseProcessor::rethrow)
                .flatMap(r -> convertAndResponse(r, exchange));
    }

    private HostStatus getRemoteHost(ServerWebExchange exchange, String reqPath) {
        HostStatus result = caller.next(reqPath);
        if (HostStatus.isNotSupport(result)) {
            Logging.writeRequestUnsupported(exchange.getRequest());
            throw new UnavailableException();
        }

        return result;
    }

    private Mono<? extends Void> convertAndResponse(ClientResponse remoteResp, ServerWebExchange exchange) {
        ServerHttpResponse clientResp = exchange.getResponse();
        ResponseProcessor.resetServerHttpResponse(clientResp, remoteResp);
        return clientResp
                .writeWith(remoteResp.body(BodyExtractors.toDataBuffers()))
                .doOnError(t -> ResponseProcessor.cleanup(remoteResp))
                .doOnCancel(() -> ResponseProcessor.cleanup(remoteResp));
    }
}
