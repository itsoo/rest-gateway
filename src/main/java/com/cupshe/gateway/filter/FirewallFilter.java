package com.cupshe.gateway.filter;

import com.cupshe.gateway.config.properties.RestGatewayProperties;
import com.cupshe.gateway.constant.Headers;
import com.cupshe.gateway.constant.Ordered;
import com.cupshe.gateway.exception.ForbiddenException;
import com.cupshe.gateway.log.Logging;
import com.cupshe.gateway.util.InetIpUtils;
import com.google.common.hash.BloomFilter;
import com.google.common.hash.Funnel;
import com.google.common.hash.Funnels;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;

/**
 * FirewallFilter
 * <p>Block the request(by IP) of black-list, there may be false positives(less 1%)
 *
 * @author zxy
 */
@Component
@Order(Ordered.SECOND)
public class FirewallFilter implements WebFilter {

    private final Funnel<CharSequence> f = Funnels.stringFunnel(StandardCharsets.UTF_8);

    private final BloomFilter<String> filter = BloomFilter.create(f, 1 << 27, 0.01);

    private final boolean blackEnable;

    public FirewallFilter(RestGatewayProperties properties) {
        blackEnable = properties.isBlackEnable();
        initial(properties);
    }

    private void initial(RestGatewayProperties properties) {
        for (String ip : InetIpUtils.filters(properties.getBlackList())) {
            InetIpUtils.rangeIps(ip).forEach(filter::put);
        }
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        return doFilter(exchange, chain);
    }

    private Mono<Void> doFilter(ServerWebExchange exchange, WebFilterChain chain) {
        String originIp = exchange.getAttribute(Headers.X_ORIGIN_IP);
        Assert.notNull(originIp, "'originIp' cannot be null.");
        if (blackEnable && filter.mightContain(originIp)) {
            Logging.writeRequestBlacklist(exchange.getRequest(), originIp);
            throw new ForbiddenException();
        }

        return chain.filter(exchange);
    }
}
