package com.cupshe.gateway.log;

import com.cupshe.ak.common.BaseConstant;
import com.cupshe.ak.text.StringUtils;
import com.cupshe.gateway.core.HostStatus;
import com.cupshe.gateway.filter.Filters;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.http.server.reactive.ServerHttpRequest;

/**
 * Logging
 *
 * @author zxy
 */
@Slf4j
public class Logging {

    public static void writeRequestPayload(ServerHttpRequest req, String url) {
        log.info(StringUtils.getFormatString("Rest-gateway forwarding <{} {},{}>",
                req.getMethodValue(), url, req.getHeaders()));
    }

    public static void writeRequestRateLimiter(ServerHttpRequest req) {
        log.info("Rest-gateway rate-limiters [{}].", Filters.getPath(req));
    }

    public static void writeRequestUnsupported(ServerHttpRequest req) {
        log.warn("Rest-gateway unsupported [{}].", Filters.getPath(req));
    }

    public static void writeRequestNotFound(ServerHttpRequest req) {
        log.warn("Rest-gateway not-found [{}].", Filters.getPath(req));
    }

    public static void writeRequestBlacklist(ServerHttpRequest req, String originIp) {
        log.warn("Rest-gateway [{}] black-list [{}].", Filters.getPath(req), originIp);
    }

    public static void writeRequestUnauthorized(ServerHttpRequest req) {
        log.info("Rest-gateway unauthorized [{}].", Filters.getPath(req));
    }

    public static void writeRequestTimeoutBreaker(ServerHttpRequest req, HostStatus hs, String traceId) {
        MDC.put(BaseConstant.MDC_SESSION_KEY, traceId);
        log.error("Rest-gateway [{}] timeout-breaker [{}].", Filters.getPath(req), hs.getHost());
    }

    public static void writeResponseFailure(Throwable t, String traceId) {
        MDC.put(BaseConstant.MDC_SESSION_KEY, traceId);
        log.error("Gateway error: {}", t.getMessage(), t);
    }
}
