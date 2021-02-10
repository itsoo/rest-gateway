package com.cupshe.gateway.filter;

import com.cupshe.gateway.util.Attributes;
import com.cupshe.gateway.util.RequestProcessor;
import com.cupshe.gateway.util.ResponseProcessor;
import lombok.SneakyThrows;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;
import org.springframework.web.reactive.function.BodyExtractors;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import reactor.netty.ByteBufMono;

import java.net.URI;
import java.util.Objects;

/**
 * Filters
 *
 * @author zxy
 */
@Component
public class Filters {

    private final WebClient webClient;

    public Filters(WebClient webClient) {
        this.webClient = webClient;
    }

    public Mono<Mono<DataBuffer>> getModel(ServerHttpResponse resp, String message) {
        DataBuffer buffer = resp.bufferFactory().allocateBuffer().write(message.getBytes());
        return Mono.just(ByteBufMono.just(buffer));
    }

    public static String getPath(ServerWebExchange exchange) {
        return getPath(exchange.getRequest());
    }

    public static String getPath(ServerHttpRequest req) {
        return req.getURI().getPath();
    }

    public void filterChain(AbstractFilter mainFilter, ServerWebExchange exchange) {
        AbstractFilter filter = mainFilter;
        while (filter.hasNext()) {
            filter.filter(exchange);
            filter = filter.next();
        }
    }

    @SneakyThrows
    public Mono<Void> requestAndResponse(ServerHttpResponse clientResp, String url) {
        Attributes attr = FilterContext.getAttributes();
        Assert.notNull(attr, "'attributes' cannot be null.");
        return RequestProcessor.getRemoteRequestOf(webClient, attr, new URI(url))
                .exchange()
                .name(attr.getId())
                .doOnError(ResponseProcessor::rethrow)
                .flatMap(r -> convertAndResponse(r, clientResp));
    }

    private Mono<Void> convertAndResponse(ClientResponse remoteResp, ServerHttpResponse clientResp) {
        if (Objects.isNull(clientResp)) {
            return Mono.empty();
        }

        ResponseProcessor.resetServerHttpResponse(clientResp, remoteResp);
        return clientResp
                .writeWith(remoteResp.body(BodyExtractors.toDataBuffers()))
                .doOnError(t -> ResponseProcessor.cleanup(remoteResp))
                .doOnCancel(() -> ResponseProcessor.cleanup(remoteResp));
    }
}
