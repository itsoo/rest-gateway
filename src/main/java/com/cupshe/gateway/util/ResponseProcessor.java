package com.cupshe.gateway.util;

import com.cupshe.ak.exception.ExceptionUtils;
import com.cupshe.gateway.constant.Cors;
import com.cupshe.gateway.constant.Headers;
import com.cupshe.gateway.exception.TimeoutException;
import com.google.common.collect.ImmutableSet;
import io.netty.channel.ConnectTimeoutException;
import io.netty.handler.timeout.ReadTimeoutException;
import io.netty.handler.timeout.WriteTimeoutException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.web.reactive.function.client.ClientResponse;

import java.net.ConnectException;
import java.util.Set;

/**
 * ResponseProcessor
 *
 * @author zxy
 */
public class ResponseProcessor {

    private static final Set<Class<? extends Exception>> TIMEOUT_ERRORS = ImmutableSet.of(
            ConnectException.class,
            ConnectTimeoutException.class,
            ReadTimeoutException.class,
            WriteTimeoutException.class);

    private static final TimeoutException TIMEOUT_EXCEPTION = new TimeoutException();

    public static void rethrow(Throwable t) {
        for (Class<? extends Exception> errorClass : TIMEOUT_ERRORS) {
            if (errorClass.isAssignableFrom(t.getClass())) {
                ExceptionUtils.rethrow(TIMEOUT_EXCEPTION);
            }
        }

        ExceptionUtils.rethrow(t);
    }

    public static void resetServerHttpResponse(ServerHttpResponse clientResp, ClientResponse remoteResp) {
        clientResp.setStatusCode(remoteResp.statusCode());
        HttpHeaders clientRespHeaders = clientResp.getHeaders();
        HttpHeaders remoteRespHeaders = remoteResp.headers().asHttpHeaders();
        // reset response headers
        remoteRespHeaders.forEach((k, v) -> {
            if (Headers.Ignores.nonContains(k)) {
                if (!clientRespHeaders.containsKey(k)) {
                    clientRespHeaders.put(k, v);
                } else if (Cors.nonContains(k)) {
                    clientRespHeaders.put(k, v);
                }
            }
        });
    }

    public static void cleanup(ClientResponse clientResp) {
        if (clientResp != null) {
            clientResp.bodyToMono(Void.class).subscribe();
        }
    }
}
