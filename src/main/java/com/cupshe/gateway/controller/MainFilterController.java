package com.cupshe.gateway.controller;

import com.cupshe.gateway.core.HostStatus;
import com.cupshe.gateway.core.RequestCaller;
import com.cupshe.gateway.exception.UnavailableException;
import com.cupshe.gateway.filter.AbstractFilter;
import com.cupshe.gateway.filter.FilterContext;
import com.cupshe.gateway.filter.Filters;
import com.cupshe.gateway.log.Logging;
import com.cupshe.gateway.util.RequestProcessor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * MainFilterController
 *
 * @author zxy
 */
@RestController
@RequestMapping
public class MainFilterController {

    private final RequestCaller caller;

    private final AbstractFilter nextFilter;

    private final Filters filters;

    public MainFilterController(RequestCaller caller, AbstractFilter nextFilter, Filters filters) {
        this.caller = caller;
        this.nextFilter = nextFilter;
        this.filters = filters;
    }

    @GetMapping("/**")
    public Mono<Void> main(ServerWebExchange exchange) {
        filters.chain(nextFilter, exchange);
        // pre request
        String reqPath = Filters.getPath(exchange);
        HostStatus remoteHost = getRemoteHost(exchange, reqPath);
        String url = RequestProcessor.getRequestUrlOf(remoteHost, reqPath);
        Logging.writeRequestPayload(exchange.getRequest(), url);

        try {
            FilterContext.setHostStatus(exchange, remoteHost);
            return filters.requestAndResponse(exchange.getResponse(), url);
        } finally {
            FilterContext.clearAll();
        }
    }

    private HostStatus getRemoteHost(ServerWebExchange exchange, String reqPath) {
        HostStatus result = caller.next(reqPath);
        if (HostStatus.isNotSupport(result)) {
            Logging.writeRequestUnsupported(exchange.getRequest());
            throw new UnavailableException();
        }

        return result;
    }
}
