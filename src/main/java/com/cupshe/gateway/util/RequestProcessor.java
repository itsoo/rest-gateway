package com.cupshe.gateway.util;

import com.cupshe.ak.text.StringUtils;
import com.cupshe.gateway.constant.Headers;
import com.cupshe.gateway.constant.Symbols;
import com.cupshe.gateway.core.HostStatus;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpHeaders;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.net.InetSocketAddress;
import java.net.URI;
import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.springframework.web.reactive.function.client.WebClient.RequestBodySpec;

/**
 * RequestProcessor
 *
 * @author zxy
 */
public class RequestProcessor {

    public static HttpHeaders getHttpHeadersOf(HttpHeaders headers) {
        HttpHeaders result = new HttpHeaders();
        headers.forEach((k, v) -> {
            if (Headers.Ignores.nonContains(k)) {
                result.addAll(k, v
                        .parallelStream()
                        .filter(StringUtils::isNotBlank)
                        .collect(Collectors.toList()));
            }
        });

        return result;
    }

    public static String getRealOriginIpOf(ServerWebExchange exchange) {
        String result = exchange.getAttribute(Headers.ORIGIN_IP);
        if (StringUtils.isNotBlank(result)) {
            return result;
        }

        ServerHttpRequest req = exchange.getRequest();
        InetSocketAddress isa = req.getRemoteAddress();
        if (Objects.nonNull(isa) && Objects.nonNull(isa.getAddress())) {
            result = isa.getAddress().getHostAddress();
        }

        String host = req.getHeaders().getFirst(Headers.X_FORWARDED_FOR);
        if (StringUtils.isNotBlank(host)) {
            result = Arrays.stream(Optional
                    .ofNullable(StringUtils.split(host, Symbols.COMMA))
                    .orElse(new String[0]))
                    .filter(Objects::nonNull)
                    .findFirst()
                    .orElse(Symbols.EMPTY)
                    .trim();
        }

        if (StringUtils.isBlank(result)) {
            return Headers.Values.UNKNOWN;
        }

        return Headers.Values.BINARY_ADDRESS.equals(result)
                ? Headers.Values.LOOP_BACK
                : result;
    }

    @SuppressWarnings("unchecked")
    public static RequestBodySpec getRemoteRequestOf(WebClient webClient, Attributes attr, URI url) {
        RequestBodySpec result = webClient
                .method(attr.getMethod())
                .uri(url)
                .cookies(c -> attr.getCookies().forEach(c::addAll))
                .headers(h -> {
                    attr.getHeaders().forEach((k, v) -> {
                        if (Headers.Ignores.nonContains(k)) {
                            h.put(k, v);
                        }
                    });
                    // set remote-host
                    if (!attr.getHost().contains(Symbols.COLON)) {
                        h.set(Headers.ORIGIN_IP, attr.getHost());
                        h.set(Headers.X_ORIGIN_IP, attr.getHost());
                        h.set(Headers.X_FORWARDED_FOR, attr.getHost());
                    }
                    // set call-source
                    h.set(Headers.X_CALL_SOURCE, Headers.Values.X_CALL_SOURCE);
                });

        if (Objects.nonNull(attr.getContentType())) {
            result.accept(attr.getContentType());
        }

        if (attr.getBody() instanceof Flux) {
            result.body(BodyInserters.fromDataBuffers((Flux<DataBuffer>) attr.getBody()));
        } else if (attr.getBody() instanceof String) {
            result.body(Mono.just((String) attr.getBody()), String.class);
        } else if (Objects.nonNull(attr.getBody())) {
            result.bodyValue(attr.getBody());
        }

        return result;
    }

    public static String getRequestUrlOf(HostStatus hostStatus, String reqPath) {
        String path = StringUtils.appendWithoutStartsWith(reqPath, Symbols.FORWARD_SLASH);
        return hostStatus.getHost().startsWith(Symbols.HTTP_PROTOCOL)
                ? (hostStatus.getHost() + path)
                : (Symbols.HTTP_PROTOCOL_PREFIX + hostStatus.getHost() + path);
    }
}
