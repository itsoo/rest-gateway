package com.cupshe.gateway.filter;

import com.cupshe.gateway.core.RequestCaller;
import com.cupshe.gateway.core.Router;
import com.cupshe.gateway.exception.NotFoundException;
import com.cupshe.gateway.exception.UnavailableException;
import com.cupshe.gateway.log.Logging;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;

/**
 * SupportFilter
 * <p>Whether the service is supported or not(code 503 or 404)
 *
 * @author zxy
 */
@Component
public class SupportFilter extends AbstractFilter {

    private final RequestCaller caller;

    private final AbstractFilter next;

    public SupportFilter(RequestCaller caller, FirewallFilter nextFilter) {
        this.caller = caller;
        this.next = nextFilter;
    }

    @Override
    public AbstractFilter next() {
        return next;
    }

    @Override
    public void filter(ServerWebExchange exchange) {
        String reqPath = Filters.getPath(exchange);
        for (Router r : caller.getRouters()) {
            if (reqPath.startsWith(r.getPrefix())) {
                if (r.isStatus()) {
                    return;
                }

                Logging.writeRequestUnsupported(exchange.getRequest());
                throw new UnavailableException();
            }
        }

        Logging.writeRequestNotFound(exchange.getRequest());
        throw new NotFoundException();
    }
}
