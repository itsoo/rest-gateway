package com.cupshe.gateway.core.lb;

import com.cupshe.gateway.core.HostStatus;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Random (R)
 *
 * @author zxy
 */
public class RandomLoadBalancer implements LoadBalancer {

    private final List<HostStatus> services;

    public RandomLoadBalancer(List<String> services) {
        this.services = initial(services);
    }

    @Override
    public HostStatus next() {
        final List<HostStatus> list = aliveList(services);
        if (list.isEmpty()) {
            return HostStatus.NON_SUPPORT;
        }

        int next = ThreadLocalRandom.current().nextInt(0, list.size());

        return list.get(next);
    }

    @Override
    public List<HostStatus> getAll() {
        return services;
    }
}
