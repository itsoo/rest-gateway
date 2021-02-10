package com.cupshe.gateway.controller;

import com.cupshe.gateway.core.HostStatus;
import com.cupshe.gateway.core.RequestCaller;
import com.cupshe.gateway.exception.UnavailableException;
import com.cupshe.gateway.filter.FilterContext;
import com.cupshe.gateway.filter.Filters;
import com.cupshe.gateway.filter.MainFilter;
import com.cupshe.gateway.log.Logging;
import com.cupshe.gateway.util.RequestProcessor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * MainController
 *
 * @author zxy
 */
@RestController
@RequestMapping
public class MainController {

    private final RequestCaller caller;

    private final MainFilter mainFilter;

    private final Filters filters;

    public MainController(RequestCaller caller, MainFilter mainFilter, Filters filters) {
        this.caller = caller;
        this.mainFilter = mainFilter;
        this.filters = filters;
    }

    @GetMapping("/**")
    public Mono<Void> main(ServerWebExchange exchange) {
        filters.filterChain(mainFilter, exchange);
        // pre request
        String reqPath = Filters.getPath(exchange);
        String url = RequestProcessor.getRequestUrl(getRemoteHost(exchange, reqPath), reqPath);
        Logging.writeRequestPayload(exchange.getRequest(), url);

        try {
            return filters.requestAndResponse(exchange.getResponse(), url);
        } finally {
            FilterContext.clearAll();
        }
    }

    private HostStatus getRemoteHost(ServerWebExchange exchange, String reqPath) {
        HostStatus result = caller.next(reqPath);
        FilterContext.setHostStatus(exchange, result);
        if (HostStatus.isNotSupport(result)) {
            Logging.writeRequestUnsupported(exchange.getRequest());
            throw new UnavailableException();
        }

        return result;
    }
}
