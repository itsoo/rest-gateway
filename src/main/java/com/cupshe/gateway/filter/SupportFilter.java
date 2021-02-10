package com.cupshe.gateway.filter;

import com.cupshe.gateway.core.RequestCaller;
import com.cupshe.gateway.core.Router;
import com.cupshe.gateway.exception.UnavailableException;
import com.cupshe.gateway.log.Logging;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;

/**
 * SupportFilter
 * <p>Whether the service is degraded or not
 *
 * @author zxy
 */
@Component
public class SupportFilter extends AbstractFilter {

    private final RequestCaller requestCaller;

    private final AbstractFilter next;

    public SupportFilter(RequestCaller requestCaller, FirewallFilter firewallFilter) {
        this.requestCaller = requestCaller;
        this.next = firewallFilter;
    }

    @Override
    public AbstractFilter next() {
        return next;
    }

    @Override
    public void filter(ServerWebExchange exchange) {
        String reqPath = Filters.getPath(exchange);
        for (Router r : requestCaller.getRouters()) {
            if (reqPath.startsWith(r.getPrefix()) && r.isStatus()) {
                return;
            }
        }

        Logging.writeRequestUnsupported(exchange.getRequest());
        throw new UnavailableException();
    }
}
