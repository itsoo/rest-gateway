package com.cupshe.gateway.core;

import com.cupshe.gateway.config.properties.RestGatewayProperties;
import com.cupshe.gateway.core.lb.LoadBalanceType;
import com.cupshe.gateway.core.lb.LoadBalancer;
import com.cupshe.gateway.core.lb.RandomLoadBalancer;
import com.cupshe.gateway.core.lb.RoundRobinLoadBalancer;
import com.cupshe.gateway.exception.NotFoundException;
import com.google.common.collect.Maps;
import lombok.Data;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import java.util.List;
import java.util.Map;

/**
 * RequestCaller
 * <p>Maintain service list and provide load balancing service
 *
 * @author zxy
 */
@Data
@Component
public class RequestCaller {

    private List<Router> routers;

    private Map<String, LoadBalancer> routerMap;

    public RequestCaller(RestGatewayProperties properties) {
        this.routers = properties.getRouters();
        this.initial();
    }

    public void initial() {
        final int capacity = routers.size() << 1;
        routerMap = Maps.newHashMapWithExpectedSize(capacity);
        routers.forEach(t -> routerMap.put(t.getPrefix(), LoadBalanceType.R.equals(t.getLbType())
                ? new RandomLoadBalancer(t.getServices())
                : new RoundRobinLoadBalancer(t.getServices())));
    }

    public HostStatus next(@NonNull String prefix) {
        Assert.notNull(prefix, "'prefix' cannot be null.");
        for (Map.Entry<String, LoadBalancer> me : routerMap.entrySet()) {
            if (prefix.startsWith(me.getKey())) {
                return me.getValue().next();
            }
        }

        throw new NotFoundException();
    }
}
