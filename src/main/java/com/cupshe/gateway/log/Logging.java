package com.cupshe.gateway.log;

import com.cupshe.ak.common.BaseConstant;
import com.cupshe.ak.text.StringUtils;
import com.cupshe.gateway.core.HostStatus;
import com.cupshe.gateway.filter.Filters;
import com.cupshe.gateway.util.Attributes;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.http.HttpHeaders;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.util.MultiValueMap;
import org.springframework.web.server.ServerWebExchange;

import java.util.Objects;
import java.util.StringJoiner;

/**
 * Logging
 *
 * @author zxy
 */
@Slf4j
public class Logging {

    public static void writeRequestPayload(ServerWebExchange exchange, String url) {
        Attributes attr = exchange.getAttribute(Filters.ATTRIBUTES_CACHE_KEY);
        ServerHttpRequest req = exchange.getRequest();
        HttpHeaders headers = getHeaders(attr, req);
        String queryParams = getQueryParams(attr, req);
        log.info(StringUtils.getFormatString("Rest-gateway forwarding <{} {}{},{} #body>",
                req.getMethodValue(), url, queryParams, headers));
    }

    public static void writeRequestRateLimiter(ServerHttpRequest req) {
        log.info("Rest-gateway request [{}] rate-limiters.", Filters.getPath(req));
    }

    public static void writeRequestUnsupported(ServerHttpRequest req) {
        log.warn("Rest-gateway request [{}] unsupported.", Filters.getPath(req));
    }

    public static void writeRequestBlacklist(ServerHttpRequest req, String originIp) {
        log.warn("Rest-gateway request [{}] black-list [{}].", Filters.getPath(req), originIp);
    }

    public static void writeRequestUnauthorized(ServerHttpRequest req) {
        log.info("Rest-gateway request [{}] unauthorized.", Filters.getPath(req));
    }

    public static void writeRequestTimeoutBreaker(ServerHttpRequest req, HostStatus hostStatus, String traceId) {
        MDC.put(BaseConstant.MDC_SESSION_KEY, traceId);
        log.error("Rest-gateway request [{}] timeout-breaker [{}].", Filters.getPath(req), hostStatus.getHost());
    }

    public static void writeResponseFailure(Throwable t, String traceId) {
        MDC.put(BaseConstant.MDC_SESSION_KEY, traceId);
        log.error("Gateway error: {}", t.getMessage(), t);
    }

    private static HttpHeaders getHeaders(Attributes attr, ServerHttpRequest clientReq) {
        return Objects.nonNull(attr) ? attr.getHeaders() : clientReq.getHeaders();
    }

    private static String getQueryParams(Attributes attr, ServerHttpRequest clientReq) {
        MultiValueMap<String, String> queryParams = Objects.nonNull(attr)
                ? attr.getQueryParams()
                : clientReq.getQueryParams();
        StringJoiner joiner = new StringJoiner("&");
        queryParams.forEach((k, v) -> joiner.add(k + '=' + v.toString()));
        return joiner.toString();
    }
}
