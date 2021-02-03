package com.cupshe.gateway.util;

import com.cupshe.gateway.constant.Headers;
import org.springframework.http.HttpHeaders;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.web.reactive.function.client.ClientResponse;

/**
 * ResponseProcessor
 *
 * @author zxy
 */
public class ResponseProcessor {

    public static void resetServerHttpResponse(ServerHttpResponse clientResp, ClientResponse remoteResp) {
        clientResp.setStatusCode(remoteResp.statusCode());
        HttpHeaders clientRespHeaders = clientResp.getHeaders();
        HttpHeaders remoteRespHeaders = remoteResp.headers().asHttpHeaders();
        // reset response headers
        remoteRespHeaders.forEach((k, v) -> {
            if (Headers.Ignores.contains(k)) {
                return;
            }

            if (!clientRespHeaders.containsKey(k)) {
                clientRespHeaders.put(k, v);
            } else if (!Headers.Cors.contains(k)) {
                clientRespHeaders.put(k, v);
            }
        });
    }

    public static void cleanup(ClientResponse clientResp) {
        if (clientResp != null) {
            clientResp.bodyToMono(Void.class).subscribe();
        }
    }
}
