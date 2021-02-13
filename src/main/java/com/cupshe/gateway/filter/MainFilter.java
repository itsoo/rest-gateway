package com.cupshe.gateway.filter;

import com.cupshe.ak.net.UuidUtils;
import com.cupshe.gateway.util.RequestProcessor;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;

/**
 * MainFilter
 * <p>Cached 'trace-id' and 'remote-host' in serverWebExchange
 *
 * @author zxy
 */
@Primary
@Component
public class MainFilter extends AbstractFilter {

    private final AbstractFilter next;

    public MainFilter(LimiterFilter nextFilter) {
        this.next = nextFilter;
    }

    @Override
    public AbstractFilter next() {
        return next;
    }

    @Override
    public void filter(ServerWebExchange exchange) {
        FilterContext.setTraceId(exchange, UuidUtils.createUuid());
        FilterContext.setRemoteHost(RequestProcessor.getRealOriginIpOf(exchange));
    }
}
