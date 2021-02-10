package com.cupshe.gateway.filter;

import com.cupshe.ak.common.BaseConstant;
import com.cupshe.ak.text.StringUtils;
import com.cupshe.gateway.core.HostStatus;
import com.cupshe.gateway.util.Attributes;
import org.slf4j.MDC;
import org.springframework.web.server.ServerWebExchange;

/**
 * FilterContext
 *
 * @author zxy
 */
public class FilterContext {

    private static final ThreadLocal<String> TRACE_ID_CACHE = new ThreadLocal<>();

    private static final ThreadLocal<String> REMOTE_HOST_CACHE = new ThreadLocal<>();

    private static final ThreadLocal<Attributes> ATTRIBUTES_CACHE = new ThreadLocal<>();

    private static final String HOST_STATUS_CACHE_KEY = "$HOST_STATUS";

    private static final String TRACE_ID_CACHE_KEY = "$TRACE_ID";

    public static void clearAll() {
        removeTraceId();
        removeRemoteHost();
        removeAttributes();
    }

    public static String getTraceId(ServerWebExchange exchange) {
        return StringUtils.defaultIfBlank(TRACE_ID_CACHE.get(),
                (String) exchange.getAttributes().get(TRACE_ID_CACHE_KEY));
    }

    public static void setTraceId(ServerWebExchange exchange, String traceId) {
        exchange.getAttributes().put(TRACE_ID_CACHE_KEY, traceId);
        TRACE_ID_CACHE.set(traceId);
        MDC.put(BaseConstant.MDC_SESSION_KEY, traceId);
    }

    public static void removeTraceId() {
        TRACE_ID_CACHE.remove();
        MDC.remove(BaseConstant.MDC_SESSION_KEY);
    }

    public static String getRemoteHost() {
        return REMOTE_HOST_CACHE.get();
    }

    public static void setRemoteHost(String traceId) {
        REMOTE_HOST_CACHE.set(traceId);
    }

    public static void removeRemoteHost() {
        REMOTE_HOST_CACHE.remove();
    }

    public static Attributes getAttributes() {
        return ATTRIBUTES_CACHE.get();
    }

    public static void setAttributes(Attributes attr) {
        ATTRIBUTES_CACHE.set(attr);
    }

    public static void removeAttributes() {
        ATTRIBUTES_CACHE.remove();
    }

    public static void setHostStatus(ServerWebExchange exchange, HostStatus hostStatus) {
        exchange.getAttributes().put(HOST_STATUS_CACHE_KEY, hostStatus);
    }

    public static HostStatus getHostStatus(ServerWebExchange exchange) {
        return exchange.getAttribute(HOST_STATUS_CACHE_KEY);
    }
}
