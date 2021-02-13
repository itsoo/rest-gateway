package com.cupshe.gateway.filter;

import com.cupshe.gateway.config.properties.RestGatewayProperties;
import com.cupshe.gateway.exception.ForbiddenException;
import com.cupshe.gateway.log.Logging;
import com.cupshe.gateway.util.InetIpUtils;
import com.google.common.hash.BloomFilter;
import com.google.common.hash.Funnel;
import com.google.common.hash.Funnels;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;
import org.springframework.web.server.ServerWebExchange;

import java.nio.charset.StandardCharsets;

/**
 * FirewallFilter
 * <p>Block the request(by IP) of black-list, there may be false positives(less 1%)
 *
 * @author zxy
 */
@Component
public class FirewallFilter extends AbstractFilter {

    private final Funnel<CharSequence> f = Funnels.stringFunnel(StandardCharsets.UTF_8);

    private final BloomFilter<String> filter = BloomFilter.create(f, 1 << 27, 0.01);

    private final boolean blackEnable;

    private final AbstractFilter next;

    public FirewallFilter(RestGatewayProperties properties, PreFilter nextFilter) {
        this.initial(properties);
        this.blackEnable = properties.isBlackEnable();
        this.next = nextFilter;
    }

    private void initial(RestGatewayProperties properties) {
        for (String ip : InetIpUtils.filters(properties.getBlackList())) {
            InetIpUtils.rangeIps(ip).forEach(filter::put);
        }
    }

    @Override
    public AbstractFilter next() {
        return next;
    }

    @Override
    public void filter(ServerWebExchange exchange) {
        String originIp = FilterContext.getRemoteHost();
        Assert.notNull(originIp, "'originIp' cannot be null.");
        if (blackEnable && filter.mightContain(originIp)) {
            Logging.writeRequestBlacklist(exchange.getRequest(), originIp);
            throw new ForbiddenException();
        }
    }
}
