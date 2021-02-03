package com.cupshe.gateway.filter;

import com.cupshe.gateway.config.properties.RestGatewayProperties;
import com.cupshe.gateway.constant.Headers;
import com.cupshe.gateway.constant.Ordered;
import com.cupshe.gateway.core.Router;
import com.cupshe.gateway.exception.UnauthorizedException;
import com.cupshe.gateway.log.Logging;
import com.cupshe.gateway.util.Attributes;
import com.google.common.collect.Sets;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * AuthFilter
 * <ol>
 *   <li>If the request is in the non-auth list, it will be released directly</li>
 *   <li>The interface that needs authentication will verify the validity of the header information</li>
 *   <li>An invalid request will raise {@link UnauthorizedException}, code is 401</li>
 * </ol>
 *
 * @author zxy
 */
@Component
@Order(Ordered.THIRD)
public class AuthFilter implements WebFilter {

    private final Set<String> noAuthPrefix = Sets.newHashSet();

    public AuthFilter(RestGatewayProperties properties) {
        initial(properties);
    }

    private void initial(RestGatewayProperties properties) {
        Map<String, String> map = properties
                .getRouters()
                .stream()
                .collect(Collectors.toMap(Router::getName, Router::getPrefix));
        properties.getNonAuth().forEach(k -> noAuthPrefix.add(map.get(k)));
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        return doFilter(exchange, chain);
    }

    private Mono<Void> doFilter(ServerWebExchange exchange, WebFilterChain chain) {
        String reqPath = Filters.getPath(exchange);
        for (String prefix : noAuthPrefix) {
            if (reqPath.startsWith(prefix)) {
                return chain.filter(exchange);
            }
        }

        Attributes attr = exchange.getAttribute(Filters.ATTRIBUTES_CACHE_KEY);
        Assert.notNull(attr, "'attributes' cannot be null.");
        for (String k : attr.getHeaders().keySet()) {
            if (Headers.Auth.contains(k)) {
                return chain.filter(exchange);
            }
        }

        Logging.writeRequestUnauthorized(exchange.getRequest());

        throw new UnauthorizedException();
    }
}
