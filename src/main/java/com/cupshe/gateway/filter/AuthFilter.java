package com.cupshe.gateway.filter;

import com.cupshe.gateway.config.properties.RestGatewayProperties;
import com.cupshe.gateway.constant.Auth;
import com.cupshe.gateway.core.Router;
import com.cupshe.gateway.exception.UnauthorizedException;
import com.cupshe.gateway.log.Logging;
import com.cupshe.gateway.util.Attributes;
import com.google.common.collect.Sets;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;
import org.springframework.web.server.ServerWebExchange;

import java.util.List;
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
public class AuthFilter extends AbstractFilter {

    private final Set<String> noAuthPrefix = Sets.newHashSet();

    public AuthFilter(RestGatewayProperties properties) {
        this.initial(properties.getNonAuth(), properties.getRouters());
    }

    private void initial(Set<String> nonAuth, List<Router> routers) {
        Map<String, String> map = routers
                .parallelStream()
                .collect(Collectors.toMap(Router::getName, Router::getPrefix));
        nonAuth.forEach(k -> noAuthPrefix.add(map.get(k)));
    }

    @Override
    public AbstractFilter next() {
        return null;
    }

    @Override
    public void filter(ServerWebExchange exchange) {
        String reqPath = Filters.getPath(exchange);
        for (String prefix : noAuthPrefix) {
            if (reqPath.startsWith(prefix)) {
                return;
            }
        }

        Attributes attr = FilterContext.getAttributes();
        Assert.notNull(attr, "'attributes' cannot be null.");
        for (String k : attr.getHeaders().keySet()) {
            if (Auth.contains(k)) {
                return;
            }
        }

        Logging.writeRequestUnauthorized(exchange.getRequest());
        throw new UnauthorizedException();
    }
}
